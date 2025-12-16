package com.revticket.theater.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TheaterStatsResponse {
    private Long totalTheaters;
    private Long totalScreens;
    private Long totalSeats;
}
