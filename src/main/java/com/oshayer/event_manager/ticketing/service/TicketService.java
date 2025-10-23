package com.oshayer.event_manager.ticketing.service;

import com.oshayer.event_manager.ticketing.dto.*;

import java.util.*;

public interface TicketService {
    TicketResponse createPending(TicketCreateRequest req);
    TicketResponse issue(java.util.UUID ticketId);
    TicketResponse checkIn(java.util.UUID ticketId, TicketCheckInRequest req);
    TicketResponse refund(java.util.UUID ticketId, TicketRefundRequest req);

    TicketResponse get(java.util.UUID ticketId);
    List<TicketResponse> listByEvent(java.util.UUID eventId);
    List<TicketResponse> listByBuyer(java.util.UUID buyerId);
}
