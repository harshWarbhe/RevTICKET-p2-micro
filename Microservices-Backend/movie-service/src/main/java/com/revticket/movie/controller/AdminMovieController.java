package com.revticket.movie.controller;

import com.revticket.movie.dto.ApiResponse;
import com.revticket.movie.dto.MovieDTO;
import com.revticket.movie.dto.MovieRequest;
import com.revticket.movie.dto.MovieStatsResponse;
import com.revticket.movie.service.MovieService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/movies")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMovieController {

    @Autowired
    private MovieService movieService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MovieDTO>>> getAllMovies(
            @RequestParam(name = "activeOnly", required = false, defaultValue = "false") Boolean activeOnly) {
        List<MovieDTO> movies = activeOnly ? movieService.getActiveMovies() : movieService.getAllMoviesForAdmin();
        return ResponseEntity.ok(ApiResponse.<List<MovieDTO>>builder()
                .success(true)
                .data(movies)
                .message("Movies retrieved successfully")
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MovieDTO>> getMovieById(@PathVariable String id) {
        return movieService.getMovieById(id)
                .map(movie -> ResponseEntity.ok(ApiResponse.<MovieDTO>builder()
                        .success(true)
                        .data(movie)
                        .message("Movie retrieved successfully")
                        .build()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MovieDTO>> createMovie(@Valid @RequestBody MovieRequest request) {
        MovieDTO movie = movieService.createMovie(request);
        return ResponseEntity.ok(ApiResponse.<MovieDTO>builder()
                .success(true)
                .data(movie)
                .message("Movie created successfully")
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MovieDTO>> updateMovie(
            @PathVariable String id,
            @Valid @RequestBody MovieRequest request) {
        MovieDTO movie = movieService.updateMovie(id, request);
        return ResponseEntity.ok(ApiResponse.<MovieDTO>builder()
                .success(true)
                .data(movie)
                .message("Movie updated successfully")
                .build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<MovieDTO>> toggleMovieStatus(@PathVariable String id) {
        MovieDTO movie = movieService.toggleMovieStatus(id);
        return ResponseEntity.ok(ApiResponse.<MovieDTO>builder()
                .success(true)
                .data(movie)
                .message("Movie status updated successfully")
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMovie(@PathVariable String id) {
        movieService.deleteMovie(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Movie deleted successfully")
                .build());
    }

    @GetMapping("/stats")
    public ResponseEntity<java.util.Map<String, Object>> getMovieStats() {
        MovieStatsResponse stats = movieService.getMovieStats();
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("totalMovies", stats.getTotalMovies());
        response.put("activeMovies", stats.getActiveMovies());
        response.put("upcomingMovies", stats.getUpcomingMovies());
        return ResponseEntity.ok(response);
    }
}
