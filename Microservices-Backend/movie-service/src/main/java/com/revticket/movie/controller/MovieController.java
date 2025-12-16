package com.revticket.movie.controller;

import com.revticket.movie.dto.MovieDTO;
import com.revticket.movie.entity.Movie;
import com.revticket.movie.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MovieController {

    @Autowired
    private MovieService movieService;

    @GetMapping("/movies")
    public ResponseEntity<List<Movie>> getAllMovies(@RequestParam(required = false) String city) {
        if (city != null && !city.trim().isEmpty()) {
            return ResponseEntity.ok(movieService.getMoviesByCity(city));
        }
        return ResponseEntity.ok(movieService.getAllMovies());
    }

    @GetMapping("/movies/{id}")
    public ResponseEntity<MovieDTO> getMovieById(@PathVariable("id") String id) {
        return movieService.getMovieById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
