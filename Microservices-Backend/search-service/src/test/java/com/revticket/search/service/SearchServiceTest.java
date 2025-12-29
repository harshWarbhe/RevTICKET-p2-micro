package com.revticket.search.service;

import com.revticket.search.client.MovieServiceClient;
import com.revticket.search.client.ShowtimeServiceClient;
import com.revticket.search.client.TheaterServiceClient;
import com.revticket.search.dto.MovieSearchDTO;
import com.revticket.search.dto.SearchResponse;
import com.revticket.search.dto.ShowtimeSearchDTO;
import com.revticket.search.dto.TheaterSearchDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
// import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private MovieServiceClient movieServiceClient;

    @Mock
    private TheaterServiceClient theaterServiceClient;

    @Mock
    private ShowtimeServiceClient showtimeServiceClient;

    @InjectMocks
    private SearchService searchService;

    @Test
    void testSearchAll_Success() {
        MovieSearchDTO movie = new MovieSearchDTO();
        movie.setId("1");
        movie.setTitle("Inception");

        TheaterSearchDTO theater = new TheaterSearchDTO();
        theater.setId("1");
        theater.setName("Grand Cinema");

        ShowtimeSearchDTO showtime = new ShowtimeSearchDTO();
        showtime.setId("1");

        when(movieServiceClient.searchMovies("query")).thenReturn(Collections.singletonList(movie));
        when(theaterServiceClient.searchTheaters("query")).thenReturn(Collections.singletonList(theater));
        when(showtimeServiceClient.searchShowtimes("query")).thenReturn(Collections.singletonList(showtime));

        SearchResponse response = searchService.searchAll("query");

        assertNotNull(response);
        assertEquals(1, response.getMovies().size());
        assertEquals("Inception", response.getMovies().get(0).getTitle());
        assertEquals(1, response.getTheaters().size());
        assertEquals(1, response.getShowtimes().size());
    }

    @Test
    void testSearchAll_PartialFailure() {
        when(movieServiceClient.searchMovies("query")).thenThrow(new RuntimeException("Service down"));
        when(theaterServiceClient.searchTheaters("query")).thenReturn(Collections.emptyList());
        when(showtimeServiceClient.searchShowtimes("query")).thenReturn(Collections.emptyList());

        SearchResponse response = searchService.searchAll("query");

        assertNotNull(response);
        assertEquals(0, response.getMovies().size()); // Fallback to empty list
        assertEquals(0, response.getTheaters().size());
    }
}
