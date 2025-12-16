package com.revticket.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShowtimeStatsDTO {
    private Long totalShowtimes;
    private Long upcomingShowtimes;
    private Long completedShowtimes;
    private Double averageOccupancy;
}
