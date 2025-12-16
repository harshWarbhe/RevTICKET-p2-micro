package com.revticket.movie.service;

import com.revticket.movie.dto.MovieDTO;
import com.revticket.movie.dto.MovieRequest;
import com.revticket.movie.dto.MovieStatsResponse;
import com.revticket.movie.entity.Movie;
import com.revticket.movie.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;

    public List<Movie> getAllMovies() {
        return movieRepository.findByIsActiveTrue();
    }

    public List<Movie> getMoviesByCity(String city) {
        // City filtering will be handled by showtime-service
        return movieRepository.findByIsActiveTrue();
    }

    public List<MovieDTO> getAllMoviesForAdmin() {
        return movieRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<MovieDTO> getActiveMovies() {
        return movieRepository.findByIsActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<MovieDTO> getMovieById(String id) {
        return movieRepository.findById(Objects.requireNonNullElse(id, ""))
                .map(this::convertToDTO);
    }

    public MovieDTO createMovie(MovieRequest request) {
        Movie movie = new Movie();
        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setGenre(request.getGenre());
        movie.setDuration(request.getDuration());
        movie.setDirector(request.getDirector());
        movie.setCrew(request.getCrew());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setPosterUrl(request.getPosterUrl());
        movie.setTrailerUrl(request.getTrailerUrl());
        movie.setLanguage(request.getLanguage());
        movie.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        Movie saved = movieRepository.save(movie);
        return convertToDTO(saved);
    }

    public MovieDTO updateMovie(String id, MovieRequest request) {
        Movie movie = movieRepository.findById(Objects.requireNonNullElse(id, ""))
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        
        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setGenre(request.getGenre());
        movie.setDuration(request.getDuration());
        movie.setDirector(request.getDirector());
        movie.setCrew(request.getCrew());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setPosterUrl(request.getPosterUrl());
        movie.setTrailerUrl(request.getTrailerUrl());
        movie.setLanguage(request.getLanguage());
        if (request.getIsActive() != null) {
            movie.setIsActive(request.getIsActive());
        }

        Movie saved = movieRepository.save(movie);
        return convertToDTO(saved);
    }

    public MovieDTO toggleMovieStatus(String id) {
        Movie movie = movieRepository.findById(Objects.requireNonNullElse(id, ""))
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        movie.setIsActive(!movie.getIsActive());
        Movie saved = movieRepository.save(movie);
        return convertToDTO(saved);
    }

    public void deleteMovie(String id) {
        Movie movie = movieRepository.findById(Objects.requireNonNullElse(id, ""))
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        movie.setIsActive(false);
        movieRepository.save(movie);
    }

    public List<String> getAllGenres() {
        return movieRepository.findAll().stream()
                .flatMap(movie -> movie.getGenre().stream())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<String> getAllLanguages() {
        return movieRepository.findAll().stream()
                .map(Movie::getLanguage)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public MovieStatsResponse getMovieStats() {
        Long totalMovies = movieRepository.count();
        Long activeMovies = movieRepository.countByIsActiveTrue();
        Long upcomingMovies = movieRepository.countUpcomingMovies(LocalDate.now());
        return new MovieStatsResponse(totalMovies, activeMovies, upcomingMovies);
    }

    private MovieDTO convertToDTO(Movie movie) {
        MovieDTO dto = new MovieDTO();
        dto.setId(movie.getId());
        dto.setTitle(movie.getTitle());
        dto.setDescription(movie.getDescription());
        dto.setGenre(movie.getGenre());
        dto.setDuration(movie.getDuration());
        dto.setRating(null); // Will be fetched from review-service
        dto.setDirector(movie.getDirector());
        dto.setCrew(movie.getCrew());
        dto.setReleaseDate(movie.getReleaseDate());
        dto.setPosterUrl(movie.getPosterUrl());
        dto.setTrailerUrl(movie.getTrailerUrl());
        dto.setLanguage(movie.getLanguage());
        dto.setIsActive(movie.getIsActive());
        dto.setTotalShows(0); // Will be fetched from showtime-service
        dto.setTotalBookings(0); // Will be fetched from booking-service
        return dto;
    }
}
