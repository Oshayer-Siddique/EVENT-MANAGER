package com.oshayer.event_manager.events.service.impl;

import com.oshayer.event_manager.artists.repository.ArtistRepository;
import com.oshayer.event_manager.business_organizations.repository.BusinessOrganizationRepository;
import com.oshayer.event_manager.events.dto.CreateEventRequest;
import com.oshayer.event_manager.events.dto.EventResponse;
import com.oshayer.event_manager.events.dto.UpdateEventRequest;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // ===========================
    // CREATE
    // ===========================
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

        // 8) Map DTO -> Entity (BigDecimal in DTO and Entity)
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
                .vipTickets(req.getVipTickets())           .vipTicketPrice(req.getVipTicketPrice())
                .vipTicketsSold(0)                         .vipTicketsUsed(0)
                .platTickets(req.getPlatTickets())         .platTicketPrice(req.getPlatTicketPrice())
                .platTicketsSold(0)                        .platTicketsUsed(0)
                .goldTickets(req.getGoldTickets())         .goldTicketPrice(req.getGoldTicketPrice())
                .goldTicketsSold(0)                        .goldTicketsUsed(0)
                .silverTickets(req.getSilverTickets())     .silverTicketPrice(req.getSilverTicketPrice())
                .silverTicketsSold(0)                      .silverTicketsUsed(0)
                .build();

        // set seatLayoutId if the field exists (keeps compatibility if not yet added)
        setIfPresent(e, "seatLayoutId", seatLayoutId);

        // 9) Save event
        e = eventRepo.save(e);

        // 10) Save optional association links (capture id once for lambdas)
        final var eventId = e.getId();

        if (!artistIds.isEmpty()) {
            var links = artistIds.stream()
                    .map(id -> EventArtistLink.builder().eventId(eventId).artistId(id).build())
                    .toList();
            eventArtistLinkRepo.saveAll(links);
        }
        if (!sponsorIds.isEmpty()) {
            var links = sponsorIds.stream()
                    .map(id -> EventSponsorLink.builder().eventId(eventId).sponsorId(id).build())
                    .toList();
            eventSponsorLinkRepo.saveAll(links);
        }
        if (!organizerIds.isEmpty()) {
            var links = organizerIds.stream()
                    .map(id -> EventOrganizerLink.builder().eventId(eventId).orgId(id).build())
                    .toList();
            eventOrganizerLinkRepo.saveAll(links);
        }

        // 11) Build response
        return toResponse(e, artistIds, sponsorIds, organizerIds, seatLayoutId);
    }

    // ===========================
    // GET (by id)
    // ===========================
    @Override
    @Transactional
    public EventResponse get(UUID id) {
        var e = eventRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));

        var artistIds = eventArtistLinkRepo.findAll().stream()
                .filter(l -> l.getEventId().equals(id))
                .map(EventArtistLink::getArtistId)
                .toList();

        var sponsorIds = eventSponsorLinkRepo.findAll().stream()
                .filter(l -> l.getEventId().equals(id))
                .map(EventSponsorLink::getSponsorId)
                .toList();

        var organizerIds = eventOrganizerLinkRepo.findAll().stream()
                .filter(l -> l.getEventId().equals(id))
                .map(EventOrganizerLink::getOrgId)
                .toList();

        UUID seatLayoutId = getSeatLayoutIdIfPresent(e);
        return toResponse(e, artistIds, sponsorIds, organizerIds, seatLayoutId);
    }

    // ===========================
    // LIST (paged)
    // ===========================
    @Override
    @Transactional
    public Page<EventResponse> list(Pageable pageable) {
        return eventRepo.findAll(pageable).map(ev ->
                toResponse(ev,
                        // lazy-load link ids only when needed (simple approach)
                        eventArtistLinkRepo.findAll().stream().filter(l -> l.getEventId().equals(ev.getId())).map(EventArtistLink::getArtistId).toList(),
                        eventSponsorLinkRepo.findAll().stream().filter(l -> l.getEventId().equals(ev.getId())).map(EventSponsorLink::getSponsorId).toList(),
                        eventOrganizerLinkRepo.findAll().stream().filter(l -> l.getEventId().equals(ev.getId())).map(EventOrganizerLink::getOrgId).toList(),
                        getSeatLayoutIdIfPresent(ev)
                )
        );
    }

    // ===========================
    // UPDATE (partial)
    // ===========================
    @Override
    @Transactional
    public EventResponse update(UUID id, UpdateEventRequest req) {
        var e = eventRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));

        // eventCode uniqueness if provided
        if (req.getEventCode() != null) {
            eventRepo.findByEventCode(req.getEventCode()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new IllegalArgumentException("eventCode already exists: " + req.getEventCode());
                }
            });
            e.setEventCode(req.getEventCode());
        }

        // schedule
        if (req.getEventStart() != null) e.setEventStart(req.getEventStart());
        if (req.getEventEnd() != null)   e.setEventEnd(req.getEventEnd());
        if (e.getEventStart() != null && e.getEventEnd() != null && !e.getEventEnd().isAfter(e.getEventStart())) {
            throw new IllegalArgumentException("eventEnd must be after eventStart");
        }

        // identity
        if (req.getTypeCode() != null) e.setTypeCode(req.getTypeCode());
        if (req.getTypeName() != null) e.setTypeName(req.getTypeName());
        if (req.getEventName() != null) e.setEventName(req.getEventName());

        // venue & seat layout
        if (req.getVenueId() != null) {
            venueRepo.findById(req.getVenueId())
                    .orElseThrow(() -> new IllegalArgumentException("Venue not found: " + req.getVenueId()));
            e.setVenueId(req.getVenueId());
        }
        Integer capacity = null;
        if (req.getSeatLayoutId() != null) {
            var layout = seatLayoutRepo.findById(req.getSeatLayoutId())
                    .orElseThrow(() -> new IllegalArgumentException("Seat layout not found: " + req.getSeatLayoutId()));
            if (!layout.getVenue().getId().equals(e.getVenueId())) {
                throw new IllegalArgumentException("Seat layout does not belong to current venue");
            }
            capacity = layout.getTotalCapacity();
            setIfPresent(e, "seatLayoutId", req.getSeatLayoutId());
        }

        // staffing (validate roles if changing)
        if (req.getEventManager() != null)    { ensureRole(req.getEventManager(), EnumUserRole.ROLE_EVENT_MANAGER); e.setEventManager(req.getEventManager()); }
        if (req.getEventOperator1() != null)  { ensureOneOfRoles(req.getEventOperator1(), EnumUserRole.ROLE_OPERATOR); e.setEventOperator1(req.getEventOperator1()); }
        if (req.getEventOperator2() != null)  { ensureOneOfRoles(req.getEventOperator2(), EnumUserRole.ROLE_OPERATOR); e.setEventOperator2(req.getEventOperator2()); }
        if (req.getEventChecker1() != null)   { ensureOneOfRoles(req.getEventChecker1(), EnumUserRole.ROLE_OPERATOR, EnumUserRole.ROLE_EVENT_CHECKER); e.setEventChecker1(req.getEventChecker1()); }
        if (req.getEventChecker2() != null)   { ensureOneOfRoles(req.getEventChecker2(), EnumUserRole.ROLE_OPERATOR, EnumUserRole.ROLE_EVENT_CHECKER); e.setEventChecker2(req.getEventChecker2()); }

        // tickets/prices (replace only if provided)
        if (req.getVipTickets() != null)        e.setVipTickets(req.getVipTickets());
        if (req.getVipTicketPrice() != null)    e.setVipTicketPrice(req.getVipTicketPrice());
        if (req.getPlatTickets() != null)       e.setPlatTickets(req.getPlatTickets());
        if (req.getPlatTicketPrice() != null)   e.setPlatTicketPrice(req.getPlatTicketPrice());
        if (req.getGoldTickets() != null)       e.setGoldTickets(req.getGoldTickets());
        if (req.getGoldTicketPrice() != null)   e.setGoldTicketPrice(req.getGoldTicketPrice());
        if (req.getSilverTickets() != null)     e.setSilverTickets(req.getSilverTickets());
        if (req.getSilverTicketPrice() != null) e.setSilverTicketPrice(req.getSilverTicketPrice());

        // capacity check if known
        if (capacity != null) {
            int total = nz(e.getVipTickets()) + nz(e.getPlatTickets()) + nz(e.getGoldTickets()) + nz(e.getSilverTickets());
            if (total > capacity) throw new IllegalArgumentException("Total tickets (" + total + ") exceed capacity (" + capacity + ")");
        }

        // price sanity
        requirePriceIfQty(e.getVipTickets(),    e.getVipTicketPrice(),    "VIP");
        requirePriceIfQty(e.getPlatTickets(),   e.getPlatTicketPrice(),   "PLAT");
        requirePriceIfQty(e.getGoldTickets(),   e.getGoldTicketPrice(),   "GOLD");
        requirePriceIfQty(e.getSilverTickets(), e.getSilverTicketPrice(), "SILVER");

        // Save event first (for id/version)
        e = eventRepo.save(e);

        // Replace associations only if present in request (null = no change, empty = clear)
        final var eventId = e.getId();

        if (req.getArtistIds() != null) {
            var ids = ensureExistence(req.getArtistIds(), artistRepo::existsById, "Artist");
            var existing = eventArtistLinkRepo.findAll().stream().filter(l -> l.getEventId().equals(eventId)).toList();
            eventArtistLinkRepo.deleteAll(existing);
            if (!ids.isEmpty()) {
                eventArtistLinkRepo.saveAll(ids.stream().map(aid -> EventArtistLink.builder().eventId(eventId).artistId(aid).build()).toList());
            }
        }
        if (req.getSponsorIds() != null) {
            var ids = ensureExistence(req.getSponsorIds(), sponsorRepo::existsById, "Sponsor");
            var existing = eventSponsorLinkRepo.findAll().stream().filter(l -> l.getEventId().equals(eventId)).toList();
            eventSponsorLinkRepo.deleteAll(existing);
            if (!ids.isEmpty()) {
                eventSponsorLinkRepo.saveAll(ids.stream().map(sid -> EventSponsorLink.builder().eventId(eventId).sponsorId(sid).build()).toList());
            }
        }
        if (req.getOrganizerIds() != null) {
            var ids = ensureExistence(req.getOrganizerIds(), orgRepo::existsById, "BusinessOrganization");
            var existing = eventOrganizerLinkRepo.findAll().stream().filter(l -> l.getEventId().equals(eventId)).toList();
            eventOrganizerLinkRepo.deleteAll(existing);
            if (!ids.isEmpty()) {
                eventOrganizerLinkRepo.saveAll(ids.stream().map(oid -> EventOrganizerLink.builder().eventId(eventId).orgId(oid).build()).toList());
            }
        }

        // Build final response
        var artistIds = eventArtistLinkRepo.findAll().stream().filter(l -> l.getEventId().equals(eventId)).map(EventArtistLink::getArtistId).toList();
        var sponsorIds = eventSponsorLinkRepo.findAll().stream().filter(l -> l.getEventId().equals(eventId)).map(EventSponsorLink::getSponsorId).toList();
        var organizerIds = eventOrganizerLinkRepo.findAll().stream().filter(l -> l.getEventId().equals(eventId)).map(EventOrganizerLink::getOrgId).toList();
        UUID seatLayoutId = getSeatLayoutIdIfPresent(e);

        return toResponse(e, artistIds, sponsorIds, organizerIds, seatLayoutId);
    }

    // ===========================
    // DELETE
    // ===========================
    @Override
    @Transactional
    public void delete(UUID id) {
        if (!eventRepo.existsById(id)) throw new IllegalArgumentException("Event not found: " + id);

        // delete link rows first (simple approach without custom repo methods)
        var a = eventArtistLinkRepo.findAll().stream().filter(l -> l.getEventId().equals(id)).toList();
        var s = eventSponsorLinkRepo.findAll().stream().filter(l -> l.getEventId().equals(id)).toList();
        var o = eventOrganizerLinkRepo.findAll().stream().filter(l -> l.getEventId().equals(id)).toList();
        eventArtistLinkRepo.deleteAll(a);
        eventSponsorLinkRepo.deleteAll(s);
        eventOrganizerLinkRepo.deleteAll(o);

        eventRepo.deleteById(id);
    }

    // ===========================
    // Helpers
    // ===========================

    // De-dup the list, verify each id exists via the provided existsFn, or throw.
// Returns the unique ids in insertion order.
    private List<UUID> ensureExistence(List<UUID> ids,
                                       java.util.function.Function<UUID, Boolean> existsFn,
                                       String label) {
        if (ids == null || ids.isEmpty()) return List.of();
        var unique = new LinkedHashSet<>(ids);
        var missing = unique.stream()
                .filter(id -> !Boolean.TRUE.equals(existsFn.apply(id)))
                .toList();
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException(label + " ids not found: " + missing);
        }
        return new ArrayList<>(unique);
    }

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

    private int nz(Integer v){ return v==null?0:v; }

    private void requirePriceIfQty(Integer qty, BigDecimal price, String label) {
        if (qty != null && qty > 0 && (price == null || price.signum() < 0)) {
            throw new IllegalArgumentException(label + " price must be provided and >= 0 when quantity > 0");
        }
    }

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

    private UUID getSeatLayoutIdIfPresent(EventEntity e) {
        try {
            var f = EventEntity.class.getDeclaredField("seatLayoutId");
            f.setAccessible(true);
            return (UUID) f.get(e);
        } catch (Exception ignore) { return null; }
    }

    private EventResponse toResponse(EventEntity e,
                                     List<UUID> artistIds,
                                     List<UUID> sponsorIds,
                                     List<UUID> organizerIds,
                                     UUID seatLayoutId) {
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
                .vipTickets(e.getVipTickets())     .vipTicketPrice(e.getVipTicketPrice())
                .platTickets(e.getPlatTickets())   .platTicketPrice(e.getPlatTicketPrice())
                .goldTickets(e.getGoldTickets())   .goldTicketPrice(e.getGoldTicketPrice())
                .silverTickets(e.getSilverTickets()).silverTicketPrice(e.getSilverTicketPrice())
                .artistIds(artistIds)
                .sponsorIds(sponsorIds)
                .organizerIds(organizerIds)
                .build();
    }
}
