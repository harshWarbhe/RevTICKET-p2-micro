package com.revticket.movie.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieDTO {
    private String id;
    private String title;
    private String description;
    private List<String> genre;
    private Integer duration;
    private Double rating;
    private String director;
    private List<String> crew;
    private LocalDate releaseDate;
    private String posterUrl;
    private String trailerUrl;
    private String language;
    private Boolean isActive;
    private Integer totalShows;
    private Integer totalBookings;
}
