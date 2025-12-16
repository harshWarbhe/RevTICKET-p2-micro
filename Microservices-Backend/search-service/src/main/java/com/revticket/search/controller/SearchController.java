package com.revticket.search.controller;

import com.revticket.search.dto.*;
import com.revticket.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping
    public ResponseEntity<SearchResponse> searchAll(@RequestParam String query) {
        SearchResponse response = searchService.searchAll(query);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/movies")
    public ResponseEntity<List<MovieSearchDTO>> searchMovies(@RequestParam String query) {
        List<MovieSearchDTO> movies = searchService.searchMovies(query);
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/theaters")
    public ResponseEntity<List<TheaterSearchDTO>> searchTheaters(@RequestParam String query) {
        List<TheaterSearchDTO> theaters = searchService.searchTheaters(query);
        return ResponseEntity.ok(theaters);
    }

    @GetMapping("/showtimes")
    public ResponseEntity<List<ShowtimeSearchDTO>> searchShowtimes(@RequestParam String query) {
        List<ShowtimeSearchDTO> showtimes = searchService.searchShowtimes(query);
        return ResponseEntity.ok(showtimes);
    }
}
