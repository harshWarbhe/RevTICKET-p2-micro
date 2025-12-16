package com.revticket.showtime.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShowtimeStatsResponse {
    private Long totalShowtimes;
    private Long upcomingShowtimes;
    private Long showtimesLast7Days;
    private Long showtimesLast30Days;
}
