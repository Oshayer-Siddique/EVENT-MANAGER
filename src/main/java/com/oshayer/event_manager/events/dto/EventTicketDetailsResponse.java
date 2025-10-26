package com.oshayer.event_manager.events.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventTicketDetailsResponse {
    private UUID eventId;
    private List<EventTicketTierResponse> ticketTiers;
    private List<String> imageUrls;
    private SeatLayoutSummaryResponse seatLayout;
}
