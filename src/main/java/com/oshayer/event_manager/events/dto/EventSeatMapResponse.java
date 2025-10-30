package com.oshayer.event_manager.events.dto;

import java.util.List;
import java.util.UUID;

public record EventSeatMapResponse(
        UUID eventId,
        UUID seatLayoutId,
        SeatLayoutSummaryResponse layout,
        List<EventTicketTierResponse> ticketTiers,
        List<EventSeatMapSeat> seats
) {
}
