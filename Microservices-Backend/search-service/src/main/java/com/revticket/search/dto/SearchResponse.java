package com.revticket.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {
    private List<MovieSearchDTO> movies;
    private List<TheaterSearchDTO> theaters;
    private List<ShowtimeSearchDTO> showtimes;
}
