package com.revticket.movie.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieStatsResponse {
    private Long totalMovies;
    private Long activeMovies;
    private Long upcomingMovies;
}
