package com.oshayer.event_manager.ticketing.service;

import com.oshayer.event_manager.ticketing.dto.*;

import java.util.*;

public interface ReservationHoldService {
    HoldResponse create(HoldCreateRequest req);
    HoldResponse release(HoldReleaseRequest req);
    HoldResponse convert(HoldConvertRequest req);

    HoldResponse get(java.util.UUID holdId);
    List<HoldResponse> listActive(java.util.UUID eventId);
}
