package com.oshayer.event_manager.events.service.impl;

import com.oshayer.event_manager.artists.repository.ArtistRepository;
import com.oshayer.event_manager.business_organizations.repository.BusinessOrganizationRepository;
import com.oshayer.event_manager.events.dto.*;
import com.oshayer.event_manager.events.entity.*;
import com.oshayer.event_manager.events.repository.*;
import com.oshayer.event_manager.events.service.EventService;
import com.oshayer.event_manager.seat.entity.SeatLayout;
import com.oshayer.event_manager.seat.entity.SeatEntity;
import com.oshayer.event_manager.seat.repository.SeatLayoutRepository;
import com.oshayer.event_manager.seat.repository.SeatRepository;
import com.oshayer.event_manager.sponsors.repository.SponsorRepository;
import com.oshayer.event_manager.ticketing.repository.ReservationHoldRepository;
import com.oshayer.event_manager.ticketing.repository.TicketRepository;
import com.oshayer.event_manager.users.entity.EnumUserRole;
import com.oshayer.event_manager.users.entity.UserEntity;
import com.oshayer.event_manager.users.repository.UserRepository;
import com.oshayer.event_manager.venues.entity.EventVenue;
import com.oshayer.event_manager.venues.repository.EventVenueRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepo;
    private final EventVenueRepository venueRepo;
    private final SeatLayoutRepository seatLayoutRepo;
    private final SeatRepository seatRepository;

    private final UserRepository userRepo;

    private final ArtistRepository artistRepo;
    private final SponsorRepository sponsorRepo;
    private final BusinessOrganizationRepository orgRepo;

    private final EventArtistLinkRepository eventArtistLinkRepo;
    private final EventSponsorLinkRepository eventSponsorLinkRepo;
    private final EventTicketTierRepository eventTicketTierRepo;
    private final EventOrganizerLinkRepository eventOrganizerLinkRepo;
    private final EventSeatRepository eventSeatRepo;
    private final TicketRepository ticketRepository;
    private final ReservationHoldRepository reservationHoldRepository;

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

        // 6) Ticket totals & price sanity (using new EventTicketTier)
        if (req.getTicketTiers() == null || req.getTicketTiers().isEmpty()) {
            throw new IllegalArgumentException("At least one ticket tier must be provided.");
        }
        int totalTickets = req.getTicketTiers().stream()
                .mapToInt(CreateEventTicketTierRequest::getTotalQuantity)
                .sum();
        if (capacity != null && totalTickets > capacity) {
            throw new IllegalArgumentException("Total tickets (" + totalTickets + ") exceed seat layout capacity (" + capacity + ")");
        }
        req.getTicketTiers().forEach(tier -> {
            if (tier.getTotalQuantity() <= 0) throw new IllegalArgumentException("Ticket tier quantity must be positive.");
            if (tier.getPrice().compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Ticket tier price must be non-negative.");
            if (tier.getCost().compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Ticket tier cost must be non-negative.");
        });

        // 7) Optional associations existence (de-dup + check)
        List<UUID> artistIds    = ensureExistence(req.getArtistIds(),    artistRepo::existsById, "Artist");
        List<UUID> sponsorIds   = ensureExistence(req.getSponsorIds(),   sponsorRepo::existsById, "Sponsor");
        List<UUID> organizerIds = ensureExistence(req.getOrganizerIds(), orgRepo::existsById,     "BusinessOrganization");

        // 8) Map DTO -> Entity
        EventEntity e = EventEntity.builder()
                .typeCode(req.getTypeCode())
                .typeName(req.getTypeName())
                .eventCode(req.getEventCode())
                .eventName(req.getEventName())
                .eventDescription(req.getEventDescription())
                .privacyPolicy(req.getPrivacyPolicy())
                .eventStart(req.getEventStart())
                .eventEnd(req.getEventEnd())
                .venueId(venue.getId())
                .eventManager(req.getEventManager())
                .eventOperator1(req.getEventOperator1())
                .eventOperator2(req.getEventOperator2())
                .eventChecker1(req.getEventChecker1())
                .eventChecker2(req.getEventChecker2())
                .imageUrls(req.getImageUrls())
                .build();

        // set seatLayoutId if the field exists (keeps compatibility if not yet added)
        setIfPresent(e, "seatLayoutId", seatLayoutId);

        // 9) Save event
        e = eventRepo.save(e);

        // 10) Save EventTicketTiers
        final var eventId = e.getId();
        List<EventTicketTier> tiers = req.getTicketTiers().stream()
                .map(tierReq -> EventTicketTier.builder()
                        .eventId(eventId)
                        .tierCode(tierReq.getTierCode())
                        .tierName(tierReq.getTierName())
                        .totalQuantity(tierReq.getTotalQuantity())
                        .price(tierReq.getPrice())
                        .cost(tierReq.getCost())
                        .visible(Boolean.TRUE.equals(tierReq.getVisible()) || tierReq.getVisible() == null)
                        .build())
                .toList();
        eventTicketTierRepo.saveAll(tiers);

        // 11) Save optional association links (capture id once for lambdas)
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

        // 12) Build response
        refreshVenueStats(venue.getId());
        return toResponse(e, artistIds, sponsorIds, organizerIds, seatLayoutId, tiers, venue.getVenueName());
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

        var ticketTiers = eventTicketTierRepo.findByEventId(id);

        UUID seatLayoutId = getSeatLayoutIdIfPresent(e);
        String venueName = venueRepo.findById(e.getVenueId())
                .map(EventVenue::getVenueName)
                .orElse(null);
        return toResponse(e, artistIds, sponsorIds, organizerIds, seatLayoutId, ticketTiers, venueName);
    }

    @Override
    @Transactional
    public EventTicketDetailsResponse getTicketDetails(UUID id) {
        var event = eventRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));

        var ticketTierEntities = eventTicketTierRepo.findByEventId(id);
        var ticketTiers = ticketTierEntities.stream()
                .map(this::toTicketTierResponse)
                .toList();

        var seatLayout = Optional.ofNullable(getSeatLayoutIdIfPresent(event))
                .map(seatLayoutId -> seatLayoutRepo.findById(seatLayoutId)
                        .orElseThrow(() -> new IllegalArgumentException("Seat layout not found: " + seatLayoutId)))
                .map(this::toSeatLayoutSummary)
                .orElse(null);

        return EventTicketDetailsResponse.builder()
                .eventId(event.getId())
                .ticketTiers(ticketTiers)
                .imageUrls(event.getImageUrls())
                .seatLayout(seatLayout)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventSeatResponse> listSeats(UUID id) {
        eventRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));

        return eventSeatRepo.findByEventId(id).stream()
                .map(this::toSeatResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EventSeatMapResponse getSeatMap(UUID id) {
        var event = eventRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));

        UUID layoutId = getSeatLayoutIdIfPresent(event);
        if (layoutId == null) {
            throw new IllegalStateException("Event does not have a seat layout assigned.");
        }

        var layout = seatLayoutRepo.findById(layoutId)
                .orElseThrow(() -> new IllegalArgumentException("Seat layout not found: " + layoutId));

        var layoutSeats = seatRepository.findBySeatLayout_IdOrderByRowAscNumberAsc(layoutId);
        var eventSeats = eventSeatRepo.findByEventId(id);

        var eventSeatsBySeatId = eventSeats.stream()
                .collect(Collectors.toMap(es -> es.getSeat().getId(), Function.identity()));

        List<EventSeatMapSeat> seats = layoutSeats.stream()
                .map(seat -> {
                    var eventSeat = eventSeatsBySeatId.get(seat.getId());
                    return new EventSeatMapSeat(
                            seat.getId(),
                            eventSeat != null ? eventSeat.getId() : null,
                            seat.getRow(),
                            seat.getNumber(),
                            seat.getLabel(),
                            seat.getType(),
                            eventSeat != null ? eventSeat.getTierCode() : null,
                            eventSeat != null ? eventSeat.getPrice() : null,
                            eventSeat != null ? eventSeat.getStatus() : EventSeatEntity.EventSeatStatus.AVAILABLE
                    );
                })
                .toList();

        List<EventTicketTierResponse> tiers = eventTicketTierRepo.findByEventId(id).stream()
                .map(this::toTicketTierResponse)
                .toList();

        return new EventSeatMapResponse(
                event.getId(),
                layout.getId(),
                toSeatLayoutSummary(layout),
                tiers,
                seats
        );
    }

    @Override
    @Transactional
    public List<EventSeatResponse> syncSeatInventory(UUID id, SeatInventorySyncRequest request) {
        SeatInventorySyncRequest options = request != null ? request : SeatInventorySyncRequest.builder().build();

        var event = eventRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));

        UUID layoutId = getSeatLayoutIdIfPresent(event);
        if (layoutId == null) {
            throw new IllegalStateException("Cannot sync seats because event has no seat layout assigned.");
        }

        seatLayoutRepo.findById(layoutId)
                .orElseThrow(() -> new IllegalArgumentException("Seat layout not found: " + layoutId));

        var layoutSeats = seatRepository.findBySeatLayout_IdOrderByRowAscNumberAsc(layoutId);
        if (layoutSeats.isEmpty()) {
            throw new IllegalStateException("Seat layout has no seats defined. Create seats before syncing.");
        }

        var existingSeats = eventSeatRepo.findByEventId(id);
        var existingBySeatId = existingSeats.stream()
                .collect(Collectors.toMap(es -> es.getSeat().getId(), Function.identity()));

        var eventTiers = eventTicketTierRepo.findByEventId(id);
        String tierCode = resolveTierCode(options.getTierCode(), eventTiers);
        BigDecimal price = resolvePrice(options.getPrice(), tierCode, eventTiers);

        boolean overwrite = Boolean.TRUE.equals(options.getOverwriteExisting());
        boolean removeMissing = Boolean.TRUE.equals(options.getRemoveMissing());

        for (var seatEntity : layoutSeats) {
            var eventSeat = existingBySeatId.remove(seatEntity.getId());
            if (eventSeat == null) {
                var newSeat = EventSeatEntity.builder()
                        .event(event)
                        .seat(seatEntity)
                        .status(EventSeatEntity.EventSeatStatus.AVAILABLE)
                        .tierCode(tierCode)
                        .price(price)
                        .build();
                eventSeatRepo.save(newSeat);
            } else if (overwrite) {
                eventSeat.setTierCode(tierCode);
                eventSeat.setPrice(price);
            }
        }

        if (removeMissing && !existingBySeatId.isEmpty()) {
            eventSeatRepo.deleteAll(existingBySeatId.values());
        }

        return eventSeatRepo.findByEventId(id).stream()
                .map(this::toSeatResponse)
                .toList();
    }

    @Override
    @Transactional
    public List<EventSeatResponse> updateSeatAssignments(UUID id, SeatAssignmentUpdateRequest request) {
        if (request == null || request.seats() == null || request.seats().isEmpty()) {
            return listSeats(id);
        }

        var event = eventRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));

        UUID layoutId = getSeatLayoutIdIfPresent(event);
        if (layoutId == null) {
            throw new IllegalStateException("Cannot assign seats because event has no seat layout.");
        }

        seatLayoutRepo.findById(layoutId)
                .orElseThrow(() -> new IllegalArgumentException("Seat layout not found: " + layoutId));

        var seatsById = seatRepository.findBySeatLayout_IdOrderByRowAscNumberAsc(layoutId).stream()
                .collect(Collectors.toMap(SeatEntity::getId, Function.identity()));

        var tiersByCode = eventTicketTierRepo.findByEventId(id).stream()
                .collect(Collectors.toMap(EventTicketTier::getTierCode, Function.identity()));

        for (SeatAssignmentUpdateRequest.SeatAssignment assignment : request.seats()) {
            EventSeatEntity eventSeat = null;
            UUID seatId = assignment.seatId();

            if (assignment.eventSeatId() != null) {
                eventSeat = eventSeatRepo.findById(assignment.eventSeatId())
                        .orElseThrow(() -> new IllegalArgumentException("Event seat not found: " + assignment.eventSeatId()));

                if (!eventSeat.getEvent().getId().equals(id)) {
                    throw new IllegalArgumentException("Seat does not belong to this event: " + assignment.eventSeatId());
                }

                seatId = eventSeat.getSeat().getId();
            }

            if (seatId == null) {
                throw new IllegalArgumentException("seatId must be provided when eventSeatId is not supplied.");
            }

            SeatEntity seatEntity = seatsById.get(seatId);
            if (seatEntity == null) {
                throw new IllegalArgumentException("Seat does not belong to the event layout: " + seatId);
            }

            var tier = tiersByCode.get(assignment.tierCode());
            if (tier == null) {
                throw new IllegalArgumentException("Unknown ticket tier for this event: " + assignment.tierCode());
            }

            BigDecimal price = assignment.price() != null ? assignment.price() : tier.getPrice();

            if (eventSeat == null) {
                eventSeat = eventSeatRepo.findByEventIdAndSeatId(id, seatId)
                        .orElseGet(() -> EventSeatEntity.builder()
                                .event(event)
                                .seat(seatEntity)
                                .status(EventSeatEntity.EventSeatStatus.AVAILABLE)
                                .build());
            }

            eventSeat.setTierCode(tier.getTierCode());
            eventSeat.setPrice(price);
            eventSeatRepo.save(eventSeat);
        }

        return eventSeatRepo.findByEventId(id).stream()
                .map(this::toSeatResponse)
                .toList();
    }

    // ===========================
    // LIST (paged)
    // ===========================
    @Override
    @Transactional
    public Page<EventResponse> list(Pageable pageable) {
        return eventRepo.findAll(pageable).map(ev -> {
            var artistIds = eventArtistLinkRepo.findAll().stream().filter(l -> l.getEventId().equals(ev.getId())).map(EventArtistLink::getArtistId).toList();
            var sponsorIds = eventSponsorLinkRepo.findAll().stream().filter(l -> l.getEventId().equals(ev.getId())).map(EventSponsorLink::getSponsorId).toList();
            var organizerIds = eventOrganizerLinkRepo.findAll().stream().filter(l -> l.getEventId().equals(ev.getId())).map(EventOrganizerLink::getOrgId).toList();
            var ticketTiers = eventTicketTierRepo.findByEventId(ev.getId());
            String venueName = venueRepo.findById(ev.getVenueId())
                    .map(EventVenue::getVenueName)
                    .orElse(null);
            return toResponse(ev, artistIds, sponsorIds, organizerIds, getSeatLayoutIdIfPresent(ev), ticketTiers, venueName);
        });
    }

    // ===========================
    // UPDATE (partial)
    // ===========================
    @Override
    @Transactional
    public EventResponse update(UUID id, UpdateEventRequest req) {
        var e = eventRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));

        UUID previousVenueId = e.getVenueId();

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
        if (req.getEventDescription() != null) e.setEventDescription(req.getEventDescription());
        if (req.getPrivacyPolicy() != null) e.setPrivacyPolicy(req.getPrivacyPolicy());

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
        if (req.getTicketTiers() != null) {
            // Fetch existing tiers
            List<EventTicketTier> existingTiers = eventTicketTierRepo.findByEventId(id);
            Map<String, EventTicketTier> existingTiersMap = existingTiers.stream()
                    .collect(Collectors.toMap(EventTicketTier::getTierCode, Function.identity()));

            List<EventTicketTier> tiersToSave = new ArrayList<>();
            List<EventTicketTier> tiersToDelete = new ArrayList<>();

            // Process incoming tiers
            for (UpdateEventTicketTierRequest tierReq : req.getTicketTiers()) {
                if (tierReq.getPrice() != null && tierReq.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("Ticket tier price must be non-negative.");
                }
                if (tierReq.getCost() != null && tierReq.getCost().compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("Ticket tier cost must be non-negative.");
                }
                EventTicketTier existingTier = existingTiersMap.remove(tierReq.getTierCode());
                if (existingTier != null) {
                    // Update existing tier
                    if (tierReq.getTierName() != null) existingTier.setTierName(tierReq.getTierName());
                    if (tierReq.getTotalQuantity() != null) existingTier.setTotalQuantity(tierReq.getTotalQuantity());
                    if (tierReq.getPrice() != null) existingTier.setPrice(tierReq.getPrice());
                    if (tierReq.getCost() != null) existingTier.setCost(tierReq.getCost());
                    if (tierReq.getVisible() != null) existingTier.setVisible(tierReq.getVisible());
                    tiersToSave.add(existingTier);
                } else {
                    // Create new tier
                    tiersToSave.add(EventTicketTier.builder()
                            .eventId(id)
                            .tierCode(tierReq.getTierCode())
                            .tierName(tierReq.getTierName())
                            .totalQuantity(tierReq.getTotalQuantity())
                            .price(tierReq.getPrice())
                            .cost(tierReq.getCost() != null ? tierReq.getCost() : BigDecimal.ZERO)
                            .visible(tierReq.getVisible() == null || tierReq.getVisible())
                            .build());
                }
            }
            // Remaining tiers in existingTiersMap are to be deleted
            tiersToDelete.addAll(existingTiersMap.values());

            eventTicketTierRepo.deleteAll(tiersToDelete);
            eventTicketTierRepo.saveAll(tiersToSave);

            // Re-calculate capacity check if tiers were updated
            int total = tiersToSave.stream().mapToInt(EventTicketTier::getTotalQuantity).sum();
            if (capacity != null && total > capacity) {
                throw new IllegalArgumentException("Total tickets (" + total + ") exceed capacity (" + capacity + ")");
            }
        }

        // Save event first (for id/version)
        e = eventRepo.save(e);

        // Replace associations only if present in request (null = no change, empty = clear)
        final var eventId = e.getId();

        if (req.getArtistIds() != null) {
            var requested = new LinkedHashSet<>(ensureExistence(
                    req.getArtistIds(),
                    artistRepo::existsById,
                    "Artist"));

            var existingLinks = eventArtistLinkRepo.findByEventId(eventId);
            var existingIds = existingLinks.stream()
                    .map(EventArtistLink::getArtistId)
                    .collect(Collectors.toSet());

            var toInsert = requested.stream()
                    .filter(artistId -> !existingIds.contains(artistId))
                    .map(artistId -> EventArtistLink.builder()
                            .eventId(eventId)
                            .artistId(artistId)
                            .build())
                    .toList();

            var toDelete = existingLinks.stream()
                    .filter(link -> !requested.contains(link.getArtistId()))
                    .toList();

            if (!toDelete.isEmpty()) {
                eventArtistLinkRepo.deleteAllInBatch(toDelete);
            }
            if (!toInsert.isEmpty()) {
                eventArtistLinkRepo.saveAll(toInsert);
            }
        }
        if (req.getSponsorIds() != null) {
            var requested = new LinkedHashSet<>(ensureExistence(
                    req.getSponsorIds(),
                    sponsorRepo::existsById,
                    "Sponsor"));

            var existingLinks = eventSponsorLinkRepo.findByEventId(eventId);
            var existingIds = existingLinks.stream()
                    .map(EventSponsorLink::getSponsorId)
                    .collect(Collectors.toSet());

            var toInsert = requested.stream()
                    .filter(sponsorId -> !existingIds.contains(sponsorId))
                    .map(sponsorId -> EventSponsorLink.builder()
                            .eventId(eventId)
                            .sponsorId(sponsorId)
                            .build())
                    .toList();

            var toDelete = existingLinks.stream()
                    .filter(link -> !requested.contains(link.getSponsorId()))
                    .toList();

            if (!toDelete.isEmpty()) {
                eventSponsorLinkRepo.deleteAllInBatch(toDelete);
            }
            if (!toInsert.isEmpty()) {
                eventSponsorLinkRepo.saveAll(toInsert);
            }
        }
        if (req.getOrganizerIds() != null) {
            var requested = new LinkedHashSet<>(ensureExistence(
                    req.getOrganizerIds(),
                    orgRepo::existsById,
                    "BusinessOrganization"));

            var existingLinks = eventOrganizerLinkRepo.findByEventId(eventId);
            var existingIds = existingLinks.stream()
                    .map(EventOrganizerLink::getOrgId)
                    .collect(Collectors.toSet());

            var toInsert = requested.stream()
                    .filter(orgId -> !existingIds.contains(orgId))
                    .map(orgId -> EventOrganizerLink.builder()
                            .eventId(eventId)
                            .orgId(orgId)
                            .build())
                    .toList();

            var toDelete = existingLinks.stream()
                    .filter(link -> !requested.contains(link.getOrgId()))
                    .toList();

            if (!toDelete.isEmpty()) {
                eventOrganizerLinkRepo.deleteAllInBatch(toDelete);
            }
            if (!toInsert.isEmpty()) {
                eventOrganizerLinkRepo.saveAll(toInsert);
            }
        }

        // Update image URLs if provided
        if (req.getImageUrls() != null) {
            e.setImageUrls(req.getImageUrls());
        }

        if (!Objects.equals(previousVenueId, e.getVenueId())) {
            refreshVenueStats(previousVenueId);
        }
        refreshVenueStats(e.getVenueId());

        // Build final response
        var artistIds = eventArtistLinkRepo.findAll().stream().filter(l -> l.getEventId().equals(eventId)).map(EventArtistLink::getArtistId).toList();
        var sponsorIds = eventSponsorLinkRepo.findAll().stream().filter(l -> l.getEventId().equals(eventId)).map(EventSponsorLink::getSponsorId).toList();
        var organizerIds = eventOrganizerLinkRepo.findAll().stream().filter(l -> l.getEventId().equals(eventId)).map(EventOrganizerLink::getOrgId).toList();
        var ticketTiers = eventTicketTierRepo.findByEventId(eventId);
        UUID seatLayoutId = getSeatLayoutIdIfPresent(e);
        String venueName = venueRepo.findById(e.getVenueId())
                .map(EventVenue::getVenueName)
                .orElse(null);

        return toResponse(e, artistIds, sponsorIds, organizerIds, seatLayoutId, ticketTiers, venueName);
    }

    // ===========================
    // DELETE
    // ===========================
    @Override
    @Transactional
    public void delete(UUID id) {
        var event = eventRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));
        UUID venueId = event.getVenueId();

        // delete link rows first (simple approach without custom repo methods)
        var a = eventArtistLinkRepo.findAll().stream().filter(l -> l.getEventId().equals(id)).toList();
        var s = eventSponsorLinkRepo.findAll().stream().filter(l -> l.getEventId().equals(id)).toList();
        var o = eventOrganizerLinkRepo.findAll().stream().filter(l -> l.getEventId().equals(id)).toList();
        eventArtistLinkRepo.deleteAll(a);
        eventSponsorLinkRepo.deleteAll(s);
        eventOrganizerLinkRepo.deleteAll(o);
        eventTicketTierRepo.deleteAllByEventId(id);

        ticketRepository.deleteAllByEventSeat_Event_Id(id);
        reservationHoldRepository.deleteAllByEvent_Id(id);
        eventSeatRepo.deleteAllByEvent_Id(id);

        eventRepo.delete(event);
        refreshVenueStats(venueId);
    }

    // ===========================
    // Helpers
    // ===========================

    // De-dup the list, verify each id exists via the provided existsFn, or throw.
// Returns the unique ids in insertion order.
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
                                     UUID seatLayoutId,
                                     List<EventTicketTier> ticketTiers,
                                     String venueName) {
        return EventResponse.builder()
                .id(e.getId())
                .typeCode(e.getTypeCode())
                .typeName(e.getTypeName())
                .eventCode(e.getEventCode())
                .eventName(e.getEventName())
                .eventDescription(e.getEventDescription())
                .privacyPolicy(e.getPrivacyPolicy())
                .eventStart(e.getEventStart())
                .eventEnd(e.getEventEnd())
                .venueId(e.getVenueId())
                .venueName(venueName)
                .seatLayoutId(seatLayoutId)
                .eventManager(e.getEventManager())
                .eventOperator1(e.getEventOperator1())
                .eventOperator2(e.getEventOperator2())
                .eventChecker1(e.getEventChecker1())
                .eventChecker2(e.getEventChecker2())
                .artistIds(artistIds)
                .sponsorIds(sponsorIds)
                .organizerIds(organizerIds)
                .imageUrls(e.getImageUrls())
                .ticketTiers(ticketTiers.stream()
                        .map(this::toTicketTierResponse)
                        .toList())
                .build();
    }

    private EventTicketTierResponse toTicketTierResponse(EventTicketTier tier) {
        return EventTicketTierResponse.builder()
                .id(tier.getId())
                .tierCode(tier.getTierCode())
                .tierName(tier.getTierName())
                .totalQuantity(tier.getTotalQuantity())
                .price(tier.getPrice())
                .cost(tier.getCost())
                .visible(tier.getVisible())
                .soldQuantity(tier.getSoldQuantity())
                .usedQuantity(tier.getUsedQuantity())
                .build();
    }

    private EventSeatResponse toSeatResponse(EventSeatEntity seat) {
        var seatEntity = seat.getSeat();
        return new EventSeatResponse(
                seat.getId(),
                seatEntity.getId(),
                seatEntity.getLabel(),
                seatEntity.getRow(),
                seatEntity.getNumber(),
                seatEntity.getType(),
                seat.getTierCode(),
                seat.getPrice(),
                seat.getStatus()
        );
    }

    private String resolveTierCode(String requestedTierCode, List<EventTicketTier> tiers) {
        if (requestedTierCode != null && !requestedTierCode.isBlank()) {
            return tiers.stream()
                    .filter(t -> t.getTierCode().equals(requestedTierCode))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Tier code not found for this event: " + requestedTierCode))
                    .getTierCode();
        }

        if (tiers.isEmpty()) {
            throw new IllegalStateException("Event has no ticket tiers defined.");
        }

        if (tiers.size() == 1) {
            return tiers.get(0).getTierCode();
        }

        throw new IllegalArgumentException("Multiple ticket tiers exist; please specify tierCode when syncing seats.");
    }

    private BigDecimal resolvePrice(BigDecimal requestedPrice, String tierCode, List<EventTicketTier> tiers) {
        if (requestedPrice != null) {
            return requestedPrice;
        }

        return tiers.stream()
                .filter(t -> t.getTierCode().equals(tierCode))
                .map(EventTicketTier::getPrice)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    private SeatLayoutSummaryResponse toSeatLayoutSummary(SeatLayout layout) {
        return SeatLayoutSummaryResponse.builder()
                .id(layout.getId())
                .typeCode(layout.getTypeCode())
                .typeName(layout.getTypeName())
                .layoutName(layout.getLayoutName())
                .totalRows(layout.getTotalRows())
                .totalCols(layout.getTotalCols())
                .totalTables(layout.getTotalTables())
                .chairsPerTable(layout.getChairsPerTable())
                .standingCapacity(layout.getStandingCapacity())
                .totalCapacity(layout.getTotalCapacity())
                .active(layout.getIsActive())
                .build();
    }

    private void refreshVenueStats(UUID venueId) {
        if (venueId == null) {
            return;
        }

        venueRepo.findById(venueId).ifPresent(venue -> {
            ZonedDateTime now = ZonedDateTime.now();
            long live = eventRepo.countLiveByVenue(venueId, now);
            long upcoming = eventRepo.countUpcomingByVenue(venueId, now);
            long completed = eventRepo.countCompletedByVenue(venueId, now);

            venue.setLiveEvents(Math.toIntExact(live));
            venue.setEventsUpcoming(Math.toIntExact(upcoming));
            venue.setTotalEvents(Math.toIntExact(completed));

            venueRepo.save(venue);
        });
    }
}
