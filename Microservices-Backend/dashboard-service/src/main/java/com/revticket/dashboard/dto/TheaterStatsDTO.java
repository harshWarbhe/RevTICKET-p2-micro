package com.revticket.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TheaterStatsDTO {
    private Long totalTheaters;
    private Long activeTheaters;
    private Long totalScreens;
    private Long totalSeats;
}
