package com.revticket.search.dto;

import lombok.Data;

@Data
public class SearchRequest {
    private String query;
    private String type; // "all", "movies", "theaters", "showtimes"
}
