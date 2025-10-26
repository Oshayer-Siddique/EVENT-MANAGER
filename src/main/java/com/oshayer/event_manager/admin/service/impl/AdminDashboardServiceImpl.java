package com.oshayer.event_manager.admin.service.impl;

import com.oshayer.event_manager.admin.dto.*;
import com.oshayer.event_manager.admin.repository.AdminDashboardQueryRepository;
import com.oshayer.event_manager.admin.repository.AdminDashboardQueryRepository.EventPerformanceAggregate;
import com.oshayer.event_manager.admin.repository.AdminDashboardQueryRepository.SalesTrendAggregate;
import com.oshayer.event_manager.admin.service.AdminDashboardService;
import com.oshayer.event_manager.events.entity.EventSeatEntity;
import com.oshayer.event_manager.events.repository.EventRepository;
import com.oshayer.event_manager.ticketing.entity.TicketEntity;
import com.oshayer.event_manager.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final AdminDashboardQueryRepository queryRepository;

    @Override
    public AdminOverviewResponse getOverview(DashboardFilter filter) {
        OffsetDateTime start = filter.resolveFrom();
        OffsetDateTime end = filter.resolveTo();
        UUID eventId = filter.getEventId();
        ZonedDateTime now = ZonedDateTime.now();

        long totalEvents = eventRepository.count();
        long liveEvents = eventRepository.countLive(now);
        long upcomingEvents = eventRepository.countUpcoming(now);
        long completedEvents = eventRepository.countCompleted(now);

        List<TicketStatusSummary> ticketSummaries = queryRepository.aggregateTicketStatus(start, end, eventId);
        Map<TicketEntity.TicketStatus, TicketStatusSummary> ticketMap = ticketSummaries.stream()
                .collect(Collectors.toMap(TicketStatusSummary::status, ts -> ts));

        BigDecimal gross = ticketSummaries.stream()
                .map(TicketStatusSummary::gross)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal refunded = ticketSummaries.stream()
                .map(TicketStatusSummary::refunded)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal net = gross.subtract(refunded);

        long newUsers = userRepository.countNewUsers(start, end);
        List<AdminOverviewResponse.RoleCount> roleCounts = userRepository.countByRole(start, end).stream()
                .map(rc -> new AdminOverviewResponse.RoleCount(rc.getRole().name(), rc.getCount()))
                .toList();

        AdminOverviewResponse.EventMetrics events = new AdminOverviewResponse.EventMetrics(
                totalEvents, liveEvents, upcomingEvents, completedEvents);

        AdminOverviewResponse.TicketMetrics tickets = new AdminOverviewResponse.TicketMetrics(
                countTickets(ticketMap, TicketEntity.TicketStatus.PENDING),
                countTickets(ticketMap, TicketEntity.TicketStatus.ISSUED),
                countTickets(ticketMap, TicketEntity.TicketStatus.USED),
                countTickets(ticketMap, TicketEntity.TicketStatus.REFUNDED),
                countTickets(ticketMap, TicketEntity.TicketStatus.CANCELED),
                countTickets(ticketMap, TicketEntity.TicketStatus.EXPIRED)
        );

        AdminOverviewResponse.RevenueMetrics revenue = new AdminOverviewResponse.RevenueMetrics(gross, refunded, net);
        AdminOverviewResponse.CustomerMetrics customers = new AdminOverviewResponse.CustomerMetrics(newUsers, roleCounts);

        return new AdminOverviewResponse(events, tickets, revenue, customers);
    }

    @Override
    public List<EventPerformanceRow> getEventPerformance(DashboardFilter filter, int limit) {
        List<EventPerformanceAggregate> aggregates = queryRepository.aggregateEventPerformance(
                filter.resolveFromZoned(), filter.resolveToZoned(), filter.getVenueId(), limit);

        return aggregates.stream()
                .map(this::toEventPerformanceRow)
                .toList();
    }

    @Override
    public List<SalesTrendPoint> getSalesTrend(DashboardFilter filter) {
        return queryRepository.aggregateSalesTrend(filter.resolveFrom(), filter.resolveTo(), filter.getEventId())
                .stream()
                .map(this::toSalesTrendPoint)
                .toList();
    }

    @Override
    public OperationsSummaryResponse getOperations(DashboardFilter filter) {
        UUID eventId = filter.getEventId();
        List<SeatStatusSummary> seatSummaries = queryRepository.aggregateSeatStatus(eventId);

        long sold = seatsByStatus(seatSummaries, EventSeatEntity.EventSeatStatus.SOLD);
        long reserved = seatsByStatus(seatSummaries, EventSeatEntity.EventSeatStatus.RESERVED);
        long available = seatsByStatus(seatSummaries, EventSeatEntity.EventSeatStatus.AVAILABLE);

        OffsetDateTime threshold = OffsetDateTime.now().plus(filter.resolveExpiringWithin());
        long activeHolds = queryRepository.countActiveHolds(eventId);
        long expiringSoon = queryRepository.countExpiringHolds(eventId, threshold);
        List<HoldAlert> alerts = queryRepository.findActiveHolds(eventId, threshold, 20);

        return new OperationsSummaryResponse(activeHolds, expiringSoon, sold, reserved, available, alerts);
    }

    private long countTickets(Map<TicketEntity.TicketStatus, TicketStatusSummary> map, TicketEntity.TicketStatus status) {
        TicketStatusSummary summary = map.get(status);
        return summary != null ? summary.count() : 0L;
    }

    private long seatsByStatus(List<SeatStatusSummary> summaries, EventSeatEntity.EventSeatStatus status) {
        return summaries.stream()
                .filter(s -> s.status() == status)
                .mapToLong(SeatStatusSummary::count)
                .findFirst()
                .orElse(0L);
    }

    private EventPerformanceRow toEventPerformanceRow(EventPerformanceAggregate aggregate) {
        long ticketsSold = aggregate.ticketsSold();
        long capacity = aggregate.seats();
        long available = Math.max(0, capacity - ticketsSold);
        BigDecimal netRevenue = aggregate.gross().subtract(aggregate.refunded());
        double sellThrough = capacity == 0 ? 0.0 : (ticketsSold * 100.0) / capacity;

        return new EventPerformanceRow(
                aggregate.eventId(),
                aggregate.eventName(),
                aggregate.eventStart(),
                ticketsSold,
                capacity,
                aggregate.gross(),
                aggregate.refunded(),
                netRevenue,
                sellThrough,
                available
        );
    }

    private SalesTrendPoint toSalesTrendPoint(SalesTrendAggregate aggregate) {
        BigDecimal net = aggregate.gross().subtract(aggregate.refunded());
        return new SalesTrendPoint(aggregate.bucketAsDate(), aggregate.tickets(), aggregate.gross(), aggregate.refunded(), net);
    }
}
