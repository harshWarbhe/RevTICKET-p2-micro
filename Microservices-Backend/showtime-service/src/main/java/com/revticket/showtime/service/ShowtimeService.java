package com.revticket.showtime.service;

import com.revticket.showtime.client.BookingServiceClient;
import com.revticket.showtime.client.MovieServiceClient;
import com.revticket.showtime.client.TheaterServiceClient;
import com.revticket.showtime.dto.ShowtimeRequest;
import com.revticket.showtime.dto.ShowtimeResponse;
import com.revticket.showtime.dto.ShowtimeStatsResponse;
import com.revticket.showtime.entity.Showtime;
import com.revticket.showtime.repository.ShowtimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ShowtimeService {

    @Autowired
    private ShowtimeRepository showtimeRepository;
    
    @Autowired
    private TheaterServiceClient theaterServiceClient;
    
    @Autowired
    private MovieServiceClient movieServiceClient;
    
    @Autowired
    private BookingServiceClient bookingServiceClient;

    @Transactional(readOnly = true)
    public List<ShowtimeResponse> getAllShowtimes() {
        return showtimeRepository.findAllByOrderByShowDateTimeAsc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ShowtimeResponse> getShowtimesByMovie(String movieId) {
        return showtimeRepository.findByMovieId(movieId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ShowtimeResponse> getShowtimesByTheater(String theaterId) {
        return showtimeRepository.findByTheaterId(theaterId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ShowtimeResponse> getShowtimesByMovieAndDate(String movieId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return showtimeRepository.findByMovieIdAndShowDateBetween(movieId, start, end)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ShowtimeResponse> getShowtimesWithFilters(String movieId, String theaterId, LocalDate date, String search) {
        List<Showtime> showtimes;
        
        if (date != null) {
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);
            if (movieId != null) {
                showtimes = showtimeRepository.findByMovieIdAndShowDateBetween(movieId, start, end);
            } else if (theaterId != null) {
                showtimes = showtimeRepository.findByTheaterIdAndShowDateBetween(theaterId, start, end);
            } else {
                showtimes = showtimeRepository.findByShowDateTimeBetween(start, end);
            }
        } else if (movieId != null) {
            showtimes = showtimeRepository.findByMovieId(movieId);
        } else if (theaterId != null) {
            showtimes = showtimeRepository.findByTheaterId(theaterId);
        } else {
            showtimes = showtimeRepository.findAllByOrderByShowDateTimeAsc();
        }
        
        return showtimes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<ShowtimeResponse> getShowtimeById(String id) {
        return showtimeRepository.findById(id).map(this::mapToResponse);
    }

    @Transactional
    public ShowtimeResponse createShowtime(ShowtimeRequest request) {
        Showtime showtime = new Showtime();
        applyRequest(showtime, request, true);
        Showtime saved = showtimeRepository.save(showtime);
        
        // Initialize seats for the new showtime
        try {
            Map<String, String> seatRequest = Map.of(
                "showtimeId", saved.getId(),
                "screenId", saved.getScreen()
            );
            bookingServiceClient.initializeSeats(seatRequest);
        } catch (Exception e) {
            System.err.println("Failed to initialize seats for showtime " + saved.getId() + ": " + e.getMessage());
        }
        
        return mapToResponse(saved);
    }

    @Transactional
    public ShowtimeResponse updateShowtime(String id, ShowtimeRequest request) {
        Showtime showtime = showtimeRepository.findById(Objects.requireNonNullElse(id, ""))
                .orElseThrow(() -> new RuntimeException("Showtime not found"));
        applyRequest(showtime, request, false);
        return mapToResponse(showtimeRepository.save(showtime));
    }

    @Transactional
    public void deleteShowtime(String id) {
        Showtime showtime = showtimeRepository.findById(Objects.requireNonNullElse(id, ""))
                .orElseThrow(() -> new RuntimeException("Showtime not found"));
        showtimeRepository.delete(showtime);
    }

    @Transactional
    public ShowtimeResponse toggleShowtimeStatus(String id) {
        Showtime showtime = showtimeRepository.findById(Objects.requireNonNullElse(id, ""))
                .orElseThrow(() -> new RuntimeException("Showtime not found"));
        
        if (showtime.getStatus() == Showtime.ShowStatus.ACTIVE) {
            showtime.setStatus(Showtime.ShowStatus.CANCELLED);
        } else if (showtime.getStatus() == Showtime.ShowStatus.CANCELLED) {
            showtime.setStatus(Showtime.ShowStatus.ACTIVE);
        }
        
        return mapToResponse(showtimeRepository.save(showtime));
    }

    private void applyRequest(Showtime showtime, ShowtimeRequest request, boolean isCreate) {
        showtime.setMovieId(request.getMovieId());
        showtime.setTheaterId(request.getTheaterId());
        showtime.setScreen(request.getScreen());
        showtime.setShowDateTime(request.getShowDateTime());
        showtime.setTicketPrice(request.getTicketPrice());
        showtime.setTotalSeats(request.getTotalSeats());

        int availableSeats;
        if (request.getAvailableSeats() != null) {
            availableSeats = request.getAvailableSeats();
        } else {
            availableSeats = isCreate ? request.getTotalSeats() : showtime.getAvailableSeats();
        }

        if (availableSeats > request.getTotalSeats()) {
            throw new IllegalArgumentException("Available seats cannot exceed total seats");
        }

        showtime.setAvailableSeats(availableSeats);
        
        if (request.getStatus() != null) {
            showtime.setStatus(request.getStatus());
        } else if (isCreate) {
            showtime.setStatus(Showtime.ShowStatus.ACTIVE);
        }
    }

    public ShowtimeStatsResponse getShowtimeStats() {
        Long totalShowtimes = showtimeRepository.count();
        LocalDateTime now = LocalDateTime.now();
        Long upcomingShowtimes = showtimeRepository.countUpcomingShowtimes(now);
        Long showtimesLast7Days = showtimeRepository.countShowtimesBetween(now.minusDays(7), now);
        Long showtimesLast30Days = showtimeRepository.countShowtimesBetween(now.minusDays(30), now);
        return new ShowtimeStatsResponse(totalShowtimes, upcomingShowtimes, showtimesLast7Days, showtimesLast30Days);
    }

    public boolean checkShowtimeConflict(String screenId, LocalDateTime showDateTime, String excludeShowId) {
        LocalDateTime startWindow = showDateTime.minusHours(3);
        LocalDateTime endWindow = showDateTime.plusHours(3);
        
        List<Showtime> conflictingShows = showtimeRepository.findByScreenAndShowDateTimeBetween(
                screenId, startWindow, endWindow);
        
        if (excludeShowId != null) {
            conflictingShows = conflictingShows.stream()
                    .filter(show -> !show.getId().equals(excludeShowId))
                    .collect(Collectors.toList());
        }
        
        return !conflictingShows.isEmpty();
    }

    private ShowtimeResponse mapToResponse(Showtime showtime) {
        ShowtimeResponse.MovieSummary movie = null;
        ShowtimeResponse.TheaterSummary theater = null;
        ShowtimeResponse.ScreenSummary screenInfo = null;
        
        try {
            var movieData = movieServiceClient.getMovieById(showtime.getMovieId());
            movie = ShowtimeResponse.MovieSummary.builder()
                    .id((String) movieData.get("id"))
                    .title((String) movieData.get("title"))
                    .language((String) movieData.get("language"))
                    .posterUrl((String) movieData.get("posterUrl"))
                    .build();
        } catch (Exception e) {
            // Movie service unavailable
        }
        
        try {
            var theaterData = theaterServiceClient.getTheaterById(showtime.getTheaterId());
            theater = ShowtimeResponse.TheaterSummary.builder()
                    .id((String) theaterData.get("id"))
                    .name((String) theaterData.get("name"))
                    .location((String) theaterData.get("location"))
                    .build();
        } catch (Exception e) {
            // Theater service unavailable
        }
        
        try {
            if (showtime.getScreen() != null) {
                var screenData = theaterServiceClient.getScreenById(showtime.getScreen());
                screenInfo = ShowtimeResponse.ScreenSummary.builder()
                        .id((String) screenData.get("id"))
                        .name((String) screenData.get("name"))
                        .totalSeats((Integer) screenData.get("totalSeats"))
                        .build();
            }
        } catch (Exception e) {
            // Screen service unavailable
        }
        
        return ShowtimeResponse.builder()
                .id(showtime.getId())
                .movieId(showtime.getMovieId())
                .theaterId(showtime.getTheaterId())
                .screen(showtime.getScreen())
                .showDateTime(showtime.getShowDateTime())
                .ticketPrice(showtime.getTicketPrice())
                .totalSeats(showtime.getTotalSeats())
                .availableSeats(showtime.getAvailableSeats())
                .status(showtime.getStatus())
                .movie(movie)
                .theater(theater)
                .screenInfo(screenInfo)
                .build();
    }
}
