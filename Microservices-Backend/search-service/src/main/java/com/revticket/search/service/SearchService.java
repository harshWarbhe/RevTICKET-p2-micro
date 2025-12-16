package com.revticket.search.service;

import com.revticket.search.client.MovieServiceClient;
import com.revticket.search.client.ShowtimeServiceClient;
import com.revticket.search.client.TheaterServiceClient;
import com.revticket.search.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {

    @Autowired
    private MovieServiceClient movieServiceClient;

    @Autowired
    private TheaterServiceClient theaterServiceClient;

    @Autowired
    private ShowtimeServiceClient showtimeServiceClient;

    public SearchResponse searchAll(String query) {
        List<MovieSearchDTO> movies = new ArrayList<>();
        List<TheaterSearchDTO> theaters = new ArrayList<>();
        List<ShowtimeSearchDTO> showtimes = new ArrayList<>();

        try {
            movies = movieServiceClient.searchMovies(query);
        } catch (Exception e) {
            // Log error but continue
        }

        try {
            theaters = theaterServiceClient.searchTheaters(query);
        } catch (Exception e) {
            // Log error but continue
        }

        try {
            showtimes = showtimeServiceClient.searchShowtimes(query);
        } catch (Exception e) {
            // Log error but continue
        }

        return new SearchResponse(movies, theaters, showtimes);
    }

    public List<MovieSearchDTO> searchMovies(String query) {
        try {
            return movieServiceClient.searchMovies(query);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<TheaterSearchDTO> searchTheaters(String query) {
        try {
            return theaterServiceClient.searchTheaters(query);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<ShowtimeSearchDTO> searchShowtimes(String query) {
        try {
            return showtimeServiceClient.searchShowtimes(query);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
