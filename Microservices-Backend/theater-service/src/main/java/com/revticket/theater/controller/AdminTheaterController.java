package com.revticket.theater.controller;

import com.revticket.theater.dto.ScreenResponse;
import com.revticket.theater.dto.TheaterResponse;
import com.revticket.theater.dto.TheaterStatsResponse;
import com.revticket.theater.entity.Screen;
import com.revticket.theater.repository.ScreenRepository;
import com.revticket.theater.service.TheaterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/theatres")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTheaterController {

    @Autowired
    private TheaterService theaterService;

    @Autowired
    private ScreenRepository screenRepository;

    @GetMapping
    public ResponseEntity<List<TheaterResponse>> getAllTheaters(
            @RequestParam(name = "activeOnly", required = false, defaultValue = "false") Boolean activeOnly) {
        return ResponseEntity.ok(theaterService.getAllTheaters(activeOnly));
    }

    @PostMapping
    public ResponseEntity<TheaterResponse> createTheater(@RequestBody com.revticket.theater.dto.TheaterRequest theaterRequest) {
        return ResponseEntity.ok(theaterService.createTheater(theaterRequest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TheaterResponse> updateTheater(@PathVariable String id, @RequestBody com.revticket.theater.dto.TheaterRequest theaterRequest) {
        return ResponseEntity.ok(theaterService.updateTheater(id, theaterRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheater(@PathVariable String id) {
        theaterService.deleteTheater(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/pause")
    public ResponseEntity<TheaterResponse> pauseTheater(@PathVariable String id) {
        return ResponseEntity.ok(theaterService.updateTheaterStatus(id, false));
    }

    @PutMapping("/{id}/resume")
    public ResponseEntity<TheaterResponse> resumeTheater(@PathVariable String id) {
        return ResponseEntity.ok(theaterService.updateTheaterStatus(id, true));
    }

    @GetMapping("/{id}/screens")
    public ResponseEntity<List<ScreenResponse>> getTheaterScreens(@PathVariable String id) {
        List<Screen> screens = screenRepository.findByTheaterIdAndIsActive(id, true);
        List<ScreenResponse> response = screens.stream()
                .map(screen -> ScreenResponse.builder()
                        .id(screen.getId())
                        .name(screen.getName())
                        .totalSeats(screen.getTotalSeats())
                        .theaterId(screen.getTheater().getId())
                        .isActive(screen.getIsActive())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<java.util.Map<String, Object>> getTheaterStats() {
        TheaterStatsResponse stats = theaterService.getTheaterStats();
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("totalTheaters", stats.getTotalTheaters());
        response.put("activeTheaters", stats.getTotalTheaters()); // Assuming all are active for now
        response.put("totalScreens", stats.getTotalScreens());
        response.put("totalSeats", stats.getTotalSeats());
        return ResponseEntity.ok(response);
    }
}
