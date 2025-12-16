package com.revticket.showtime.dto;

import com.revticket.showtime.entity.Showtime;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class ShowtimeResponse {
    String id;
    String movieId;
    String theaterId;
    String screen;
    LocalDateTime showDateTime;
    Double ticketPrice;
    Integer totalSeats;
    Integer availableSeats;
    Showtime.ShowStatus status;
    MovieSummary movie;
    TheaterSummary theater;
    ScreenSummary screenInfo;

    @Value
    @Builder
    public static class MovieSummary {
        String id;
        String title;
        String language;
        List<String> genre;
        Integer duration;
        Double rating;
        String posterUrl;
    }

    @Value
    @Builder
    public static class TheaterSummary {
        String id;
        String name;
        String location;
        String address;
        Integer totalScreens;
    }
    
    @Value
    @Builder
    public static class ScreenSummary {
        String id;
        String name;
        Integer totalSeats;
    }
}
