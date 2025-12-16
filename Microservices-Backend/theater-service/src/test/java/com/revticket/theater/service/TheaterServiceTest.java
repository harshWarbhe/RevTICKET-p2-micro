package com.revticket.theater.service;

import com.revticket.theater.dto.TheaterRequest;
import com.revticket.theater.dto.TheaterResponse;
import com.revticket.theater.entity.Theater;
import com.revticket.theater.repository.TheaterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TheaterServiceTest {

    @Mock
    private TheaterRepository theaterRepository;

    @InjectMocks
    private TheaterService theaterService;

    private Theater theater;

    @BeforeEach
    void setUp() {
        theater = new Theater();
        theater.setId("1");
        theater.setName("Grand Cinema");
        theater.setLocation("Downtown");
        theater.setTotalScreens(5);
        theater.setIsActive(true);
    }

    @Test
    void testGetAllTheaters_ActiveOnly() {
        when(theaterRepository.findByIsActiveTrue()).thenReturn(Collections.singletonList(theater));

        List<TheaterResponse> result = theaterService.getAllTheaters(true);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Grand Cinema", result.get(0).getName());
    }

    @Test
    void testGetTheaterById_Success() {
        when(theaterRepository.findById("1")).thenReturn(Optional.of(theater));

        Optional<TheaterResponse> result = theaterService.getTheaterById("1");

        assertTrue(result.isPresent());
        assertEquals("Grand Cinema", result.get().getName());
    }

    @Test
    void testCreateTheater() {
        TheaterRequest request = new TheaterRequest();
        request.setName("Grand Cinema");
        request.setLocation("Downtown");
        request.setTotalScreens(5);
        request.setIsActive(true);

        when(theaterRepository.save(any(Theater.class))).thenReturn(theater);

        TheaterResponse result = theaterService.createTheater(request);

        assertNotNull(result);
        assertEquals("Grand Cinema", result.getName());
        verify(theaterRepository).save(any(Theater.class));
    }
}
