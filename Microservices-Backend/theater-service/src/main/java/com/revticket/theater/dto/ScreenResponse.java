package com.revticket.theater.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ScreenResponse {
    String id;
    String name;
    Integer totalSeats;
    String theaterId;
    Boolean isActive;
}
