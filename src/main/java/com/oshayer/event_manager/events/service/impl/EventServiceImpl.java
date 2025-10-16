package com.oshayer.event_manager.events.service.impl;

import com.oshayer.event_manager.artists.repository.ArtistRepository;
import com.oshayer.event_manager.business_organizations.repository.BusinessOrganizationRepository;
import com.oshayer.event_manager.events.dto.CreateEventRequest;
import com.oshayer.event_manager.events.dto.EventResponse;
import com.oshayer.event_manager.events.entity.EventArtistLink;
import com.oshayer.event_manager.events.entity.EventEntity;
import com.oshayer.event_manager.events.entity.EventOrganizerLink;
import com.oshayer.event_manager.events.entity.EventSponsorLink;
import com.oshayer.event_manager.events.repository.EventArtistLinkRepository;
import com.oshayer.event_manager.events.repository.EventOrganizerLinkRepository;
import com.oshayer.event_manager.events.repository.EventRepository;
import com.oshayer.event_manager.events.repository.EventSponsorLinkRepository;
import com.oshayer.event_manager.events.service.EventService;
import com.oshayer.event_manager.seat.entity.SeatLayout;
import com.oshayer.event_manager.seat.repository.SeatLayoutRepository;
import com.oshayer.event_manager.sponsors.repository.SponsorRepository;
import com.oshayer.event_manager.users.entity.EnumUserRole;
import com.oshayer.event_manager.users.entity.UserEntity;
import com.oshayer.event_manager.users.repository.UserRepository;
import com.oshayer.event_manager.venues.entity.EventVenue;
import com.oshayer.event_manager.venues.repository.EventVenueRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepo;
    private final EventVenueRepository venueRepo;
    private final SeatLayoutRepository seatLayoutRepo;

    private final UserRepository userRepo;

    private final ArtistRepository artistRepo;
    private final SponsorRepository sponsorRepo;
    private final BusinessOrganizationRepository orgRepo;

    private final EventArtistLinkRepository eventArtistLinkRepo;
    private final EventSponsorLinkRepository eventSponsorLinkRepo;
    private final EventOrganizerLinkRepository eventOrganizerLinkRepo;

    @Override
    @Transactional
    public EventResponse create(CreateEventRequest req) {
        // 1) Unique eventCode
        if (eventRepo.existsByEventCode(req.getEventCode())) {
            throw new IllegalArgumentException("eventCode already exists: " + req.getEventCode());
        }

        // 2) Time sanity
        if (req.getEventStart() == null || req.getEventEnd() == null || !req.getEventEnd().isAfter(req.getEventStart())) {
            throw new IllegalArgumentException("eventEnd must be after eventStart");
        }

        // 3) Venue exists
        EventVenue venue = venueRepo.findById(req.getVenueId())
                .orElseThrow(() -> new IllegalArgumentException("Venue not found: " + req.getVenueId()));

        // 4) Optional seatLayout belongs to venue + capacity
        Integer capacity = null;
        UUID seatLayoutId = req.getSeatLayoutId();
        if (seatLayoutId != null) {
            // Prefer a targeted method if you added it; otherwise fetch & compare
            SeatLayout layout = seatLayoutRepo.findById(seatLayoutId)
                    .orElseThrow(() -> new IllegalArgumentException("Seat layout not found: " + seatLayoutId));
            if (layout.getVenue() == null || layout.getVenue().getId() == null ||
                    !layout.getVenue().getId().equals(venue.getId())) {
                throw new IllegalArgumentException("Seat layout does not belong to the given venue");
            }
            capacity = layout.getTotalCapacity();
        }

        // 5) Staff existence + role checks
        ensureRole(req.getEventManager(), EnumUserRole.ROLE_EVENT_MANAGER);
        ensureOneOfRoles(req.getEventOperator1(), EnumUserRole.ROLE_OPERATOR);
        if (req.getEventOperator2() != null) ensureOneOfRoles(req.getEventOperator2(), EnumUserRole.ROLE_OPERATOR);

        ensureOneOfRoles(req.getEventChecker1(), EnumUserRole.ROLE_OPERATOR, EnumUserRole.ROLE_EVENT_CHECKER);
        if (req.getEventChecker2() != null) ensureOneOfRoles(req.getEventChecker2(), EnumUserRole.ROLE_OPERATOR, EnumUserRole.ROLE_EVENT_CHECKER);

        // 6) Ticket totals & price sanity
        int totalTickets = sumNZ(req.getVipTickets(), req.getPlatTickets(), req.getGoldTickets(), req.getSilverTickets());
        if (capacity != null && totalTickets > capacity) {
            throw new IllegalArgumentException("Total tickets (" + totalTickets + ") exceed seat layout capacity (" + capacity + ")");
        }
        requirePriceIfQty(req.getVipTickets(), req.getVipTicketPrice(), "VIP");
        requirePriceIfQty(req.getPlatTickets(), req.getPlatTicketPrice(), "PLAT");
        requirePriceIfQty(req.getGoldTickets(), req.getGoldTicketPrice(), "GOLD");
        requirePriceIfQty(req.getSilverTickets(), req.getSilverTicketPrice(), "SILVER");

        // 7) Optional associations existence (de-dup + check)
        List<UUID> artistIds    = ensureExistence(req.getArtistIds(),    artistRepo::existsById, "Artist");
        List<UUID> sponsorIds   = ensureExistence(req.getSponsorIds(),   sponsorRepo::existsById, "Sponsor");
        List<UUID> organizerIds = ensureExistence(req.getOrganizerIds(), orgRepo::existsById,     "BusinessOrganization");

        // 8) Map DTO -> Entity (prices: BigDecimal DTO → Double entity OR BigDecimal entity if you changed it)
        EventEntity e = EventEntity.builder()
                .typeCode(req.getTypeCode())
                .typeName(req.getTypeName())
                .eventCode(req.getEventCode())
                .eventName(req.getEventName())
                .eventStart(req.getEventStart())
                .eventEnd(req.getEventEnd())
                .venueId(venue.getId())
                .eventManager(req.getEventManager())
                .eventOperator1(req.getEventOperator1())
                .eventOperator2(req.getEventOperator2())
                .eventChecker1(req.getEventChecker1())
                .eventChecker2(req.getEventChecker2())
                .vipTickets(req.getVipTickets())           .vipTicketPrice((req.getVipTicketPrice()))
                .vipTicketsSold(0)                         .vipTicketsUsed(0)
                .platTickets(req.getPlatTickets())         .platTicketPrice((req.getPlatTicketPrice()))
                .platTicketsSold(0)                        .platTicketsUsed(0)
                .goldTickets(req.getGoldTickets())         .goldTicketPrice((req.getGoldTicketPrice()))
                .goldTicketsSold(0)                        .goldTicketsUsed(0)
                .silverTickets(req.getSilverTickets())     .silverTicketPrice((req.getSilverTicketPrice()))
                .silverTicketsSold(0)                      .silverTicketsUsed(0)
                .build();

        // If you added seatLayoutId to EventEntity, set it directly; otherwise ignore
        setIfPresent(e, "seatLayoutId", seatLayoutId);

        // 9) Save event
        e = eventRepo.save(e);

// capture once for lambdas
        final var eventId = e.getId();

// (optional) ensure lists are effectively final
        final var finalArtistIds    = artistIds;
        final var finalSponsorIds   = sponsorIds;
        final var finalOrganizerIds = organizerIds;

// 10) Save optional association links
        if (!finalArtistIds.isEmpty()) {
            var links = finalArtistIds.stream()
                    .map(id -> EventArtistLink.builder().eventId(eventId).artistId(id).build())
                    .toList();
            eventArtistLinkRepo.saveAll(links);
        }

        if (!finalSponsorIds.isEmpty()) {
            var links = finalSponsorIds.stream()
                    .map(id -> EventSponsorLink.builder().eventId(eventId).sponsorId(id).build())
                    .toList();
            eventSponsorLinkRepo.saveAll(links);
        }

        if (!finalOrganizerIds.isEmpty()) {
            var links = finalOrganizerIds.stream()
                    .map(id -> EventOrganizerLink.builder().eventId(eventId).orgId(id).build())
                    .toList();
            eventOrganizerLinkRepo.saveAll(links);
        }


        // 11) Build response
        return EventResponse.builder()
                .id(e.getId())
                .typeCode(e.getTypeCode())
                .typeName(e.getTypeName())
                .eventCode(e.getEventCode())
                .eventName(e.getEventName())
                .eventStart(e.getEventStart())
                .eventEnd(e.getEventEnd())
                .venueId(e.getVenueId())
                .seatLayoutId(seatLayoutId)
                .eventManager(e.getEventManager())
                .eventOperator1(e.getEventOperator1())
                .eventOperator2(e.getEventOperator2())
                .eventChecker1(e.getEventChecker1())
                .eventChecker2(e.getEventChecker2())
                .vipTickets(e.getVipTickets())     .vipTicketPrice((e.getVipTicketPrice()))
                .platTickets(e.getPlatTickets())   .platTicketPrice((e.getPlatTicketPrice()))
                .goldTickets(e.getGoldTickets())   .goldTicketPrice((e.getGoldTicketPrice()))
                .silverTickets(e.getSilverTickets()).silverTicketPrice((e.getSilverTicketPrice()))
                .artistIds(artistIds)
                .sponsorIds(sponsorIds)
                .organizerIds(organizerIds)
                .build();
    }

    // ---------- helpers ----------

    private void ensureRole(UUID userId, EnumUserRole expected) {
        UserEntity u = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        if (u.getRole() != expected) {
            throw new IllegalArgumentException("User " + userId + " must have role " + expected.name());
        }
    }

    private void ensureOneOfRoles(UUID userId, EnumUserRole... allowed) {
        UserEntity u = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        boolean ok = Arrays.stream(allowed).anyMatch(r -> r == u.getRole());
        if (!ok) {
            String allowedStr = Arrays.stream(allowed).map(Enum::name).collect(Collectors.joining(", "));
            throw new IllegalArgumentException("User " + userId + " must have role in [" + allowedStr + "]");
        }
    }

    private int sumNZ(Integer... vals) {
        int s = 0; for (Integer v : vals) s += (v == null ? 0 : v); return s;
    }

    private void requirePriceIfQty(Integer qty, BigDecimal price, String label) {
        if (qty != null && qty > 0 && (price == null || price.signum() < 0)) {
            throw new IllegalArgumentException(label + " price must be provided and >= 0 when quantity > 0");
        }
    }

    // If your EventEntity uses BigDecimal for prices, remove these converters and assign directly.
    private Double toDouble(BigDecimal bd) { return bd == null ? null : bd.doubleValue(); }
    private BigDecimal toBig(Double d) { return d == null ? null : BigDecimal.valueOf(d); }

    // Allows the same service to run even if seatLayoutId is not yet in EventEntity
    private void setIfPresent(Object target, String fieldName, Object value) {
        if (value == null) return;
        try {
            var f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (NoSuchFieldException ignored) {
        } catch (Exception ex) {
            throw new RuntimeException("Failed to set " + fieldName + ": " + ex.getMessage(), ex);
        }
    }

    private <T> List<UUID> ensureExistence(List<UUID> ids, java.util.function.Function<UUID, Boolean> existsFn, String label) {
        if (ids == null || ids.isEmpty()) return List.of();
        var unique = new LinkedHashSet<>(ids);
        var missing = unique.stream().filter(id -> !Boolean.TRUE.equals(existsFn.apply(id))).toList();
        if (!missing.isEmpty()) throw new IllegalArgumentException(label + " ids not found: " + missing);
        return new ArrayList<>(unique);
    }
}
