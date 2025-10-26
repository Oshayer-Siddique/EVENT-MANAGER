package com.oshayer.event_manager.admin.repository;

import com.oshayer.event_manager.admin.dto.HoldAlert;
import com.oshayer.event_manager.admin.dto.SeatStatusSummary;
import com.oshayer.event_manager.admin.dto.TicketStatusSummary;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AdminDashboardQueryRepository {

    private final EntityManager entityManager;

    public List<TicketStatusSummary> aggregateTicketStatus(OffsetDateTime start, OffsetDateTime end, UUID eventId) {
        TypedQuery<TicketStatusSummary> query = entityManager.createQuery(
                """
                select new com.oshayer.event_manager.admin.dto.TicketStatusSummary(
                    t.status,
                    count(t),
                    coalesce(sum(es.price), :zero),
                    coalesce(sum(t.refundAmount), :zero)
                )
                from TicketEntity t
                join t.eventSeat es
                where (:start is null or t.createdAt >= :start)
                  and (:end is null or t.createdAt <= :end)
                  and (:eventId is null or es.event.id = :eventId)
                group by t.status
                """,
                TicketStatusSummary.class
        );
        query.setParameter("start", start);
        query.setParameter("end", end);
        query.setParameter("eventId", eventId);
        query.setParameter("zero", BigDecimal.ZERO);
        return query.getResultList();
    }

    public List<SeatStatusSummary> aggregateSeatStatus(UUID eventId) {
        TypedQuery<SeatStatusSummary> query = entityManager.createQuery(
                """
                select new com.oshayer.event_manager.admin.dto.SeatStatusSummary(
                    es.status,
                    count(es)
                )
                from EventSeatEntity es
                where (:eventId is null or es.event.id = :eventId)
                group by es.status
                """,
                SeatStatusSummary.class
        );
        query.setParameter("eventId", eventId);
        return query.getResultList();
    }

    public List<EventPerformanceAggregate> aggregateEventPerformance(ZonedDateTime start, ZonedDateTime end, UUID venueId, int limit) {
        TypedQuery<EventPerformanceAggregate> query = entityManager.createQuery(
                """
                select new com.oshayer.event_manager.admin.repository.AdminDashboardQueryRepository.EventPerformanceAggregate(
                    e.id,
                    e.eventName,
                    e.eventStart,
                    coalesce(sum(case when t.status in ('ISSUED','USED') then 1 else 0 end), 0),
                    count(es),
                    coalesce(sum(case when t.status in ('ISSUED','USED') then es.price else :zero end), :zero),
                    coalesce(sum(t.refundAmount), :zero)
                )
                from EventEntity e
                left join EventSeatEntity es on es.event = e
                left join TicketEntity t on t.eventSeat = es
                where (:start is null or e.eventStart >= :start)
                  and (:end is null or e.eventStart <= :end)
                  and (:venueId is null or e.venueId = :venueId)
                group by e.id, e.eventName, e.eventStart
                order by coalesce(sum(case when t.status in ('ISSUED','USED') then es.price else :zero end), :zero) desc
                """,
                EventPerformanceAggregate.class
        );
        query.setParameter("start", start);
        query.setParameter("end", end);
        query.setParameter("venueId", venueId);
        query.setParameter("zero", BigDecimal.ZERO);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    public List<SalesTrendAggregate> aggregateSalesTrend(OffsetDateTime start, OffsetDateTime end, UUID eventId) {
        TypedQuery<SalesTrendAggregate> query = entityManager.createQuery(
                """
                select new com.oshayer.event_manager.admin.repository.AdminDashboardQueryRepository.SalesTrendAggregate(
                    function('date', t.issuedAt),
                    count(t),
                    coalesce(sum(es.price), :zero),
                    coalesce(sum(t.refundAmount), :zero)
                )
                from TicketEntity t
                join t.eventSeat es
                where t.status in ('ISSUED','USED')
                  and t.issuedAt is not null
                  and (:start is null or t.issuedAt >= :start)
                  and (:end is null or t.issuedAt <= :end)
                  and (:eventId is null or es.event.id = :eventId)
                group by function('date', t.issuedAt)
                order by function('date', t.issuedAt)
                """,
                SalesTrendAggregate.class
        );
        query.setParameter("start", start);
        query.setParameter("end", end);
        query.setParameter("eventId", eventId);
        query.setParameter("zero", BigDecimal.ZERO);
        return query.getResultList();
    }

    public long countActiveHolds(UUID eventId) {
        TypedQuery<Long> query = entityManager.createQuery(
                """
                select count(h) from ReservationHoldEntity h
                where h.status = 'ACTIVE'
                  and (:eventId is null or h.event.id = :eventId)
                """,
                Long.class
        );
        query.setParameter("eventId", eventId);
        return query.getSingleResult();
    }

    public long countExpiringHolds(UUID eventId, OffsetDateTime threshold) {
        TypedQuery<Long> query = entityManager.createQuery(
                """
                select count(h) from ReservationHoldEntity h
                where h.status = 'ACTIVE'
                  and (:eventId is null or h.event.id = :eventId)
                  and h.expiresAt <= :threshold
                """,
                Long.class
        );
        query.setParameter("eventId", eventId);
        query.setParameter("threshold", threshold);
        return query.getSingleResult();
    }

    public List<HoldAlert> findActiveHolds(UUID eventId, OffsetDateTime threshold, int limit) {
        TypedQuery<HoldAlert> query = entityManager.createQuery(
                """
                select new com.oshayer.event_manager.admin.dto.HoldAlert(
                    h.id,
                    h.event.id,
                    h.expiresAt,
                    size(h.heldSeats)
                )
                from ReservationHoldEntity h
                where h.status = 'ACTIVE'
                  and (:eventId is null or h.event.id = :eventId)
                  and (:threshold is null or h.expiresAt <= :threshold)
                order by h.expiresAt asc
                """,
                HoldAlert.class
        );
        query.setParameter("eventId", eventId);
        query.setParameter("threshold", threshold);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    public record EventPerformanceAggregate(
            UUID eventId,
            String eventName,
            ZonedDateTime eventStart,
            long ticketsSold,
            long seats,
            BigDecimal gross,
            BigDecimal refunded
    ) {}

    public record SalesTrendAggregate(
            Object bucket,
            long tickets,
            BigDecimal gross,
            BigDecimal refunded
    ) {
        public LocalDate bucketAsDate() {
            if (bucket instanceof LocalDate localDate) {
                return localDate;
            }
            if (bucket instanceof Date sqlDate) {
                return sqlDate.toLocalDate();
            }
            if (bucket instanceof OffsetDateTime offsetDateTime) {
                return offsetDateTime.toLocalDate();
            }
            throw new IllegalArgumentException("Unsupported date bucket: " + bucket);
        }
    }
}
