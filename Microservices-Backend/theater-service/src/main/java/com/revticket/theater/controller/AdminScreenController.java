package com.revticket.theater.controller;

import com.revticket.theater.dto.*;
import com.revticket.theater.entity.Screen;
import com.revticket.theater.entity.SeatCategory;
import com.revticket.theater.entity.SeatData;
import com.revticket.theater.entity.Theater;
import com.revticket.theater.repository.*;
import com.revticket.theater.service.ScreenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/screens")
@PreAuthorize("hasRole('ADMIN')")
public class AdminScreenController {

    @Autowired
    private ScreenService screenService;

    @Autowired
    private ScreenRepository screenRepository;

    @Autowired
    private SeatCategoryRepository seatCategoryRepository;

    @Autowired
    private SeatDataRepository seatDataRepository;

    @Autowired
    private TheaterRepository theaterRepository;

    @GetMapping
    public ResponseEntity<List<ScreenResponse>> getScreens(
            @RequestParam(name = "theatreId", required = false) String theatreId,
            @RequestParam(name = "activeOnly", required = false, defaultValue = "true") Boolean activeOnly) {
        if (theatreId != null && !theatreId.isBlank()) {
            return ResponseEntity.ok(screenService.getScreensByTheater(theatreId, activeOnly));
        }

        List<Screen> screens = screenRepository.findAll();
        List<ScreenResponse> response = screens.stream()
                .map(screen -> ScreenResponse.builder()
                        .id(screen.getId())
                        .name(screen.getName())
                        .totalSeats(screen.getTotalSeats())
                        .theaterId(screen.getTheater() != null ? screen.getTheater().getId() : null)
                        .isActive(screen.getIsActive())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScreenConfigDTO> getScreenById(@PathVariable String id) {
        Screen screen = screenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Screen not found"));

        List<SeatCategory> categories = seatCategoryRepository.findByScreenId(id);
        List<SeatData> seatData = seatDataRepository.findByScreenId(id);

        ScreenConfigDTO config = new ScreenConfigDTO();
        config.setId(screen.getId());
        config.setName(screen.getName());
        config.setTheatreId(screen.getTheater() != null ? screen.getTheater().getId() : null);
        config.setRows(screen.getRows() != null ? screen.getRows() : 10);
        config.setSeatsPerRow(screen.getSeatsPerRow() != null ? screen.getSeatsPerRow() : 15);
        config.setTotalSeats(screen.getTotalSeats());
        config.setCategories(categories != null ? categories.stream()
                .map(c -> new CategoryDTO(c.getId(), c.getName(), c.getPrice(), c.getColor()))
                .collect(Collectors.toList()) : new java.util.ArrayList<>());
        config.setSeatMap(seatData != null ? seatData.stream()
                .map(s -> new SeatDataDTO(s.getSeatId(), s.getLabel(), s.getRow(), s.getCol(), s.getCategoryId(), s.getStatus()))
                .collect(Collectors.toList()) : new java.util.ArrayList<>());

        return ResponseEntity.ok(config);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ScreenConfigDTO> createScreen(@RequestBody ScreenConfigDTO request) {
        try {
            if (request.getTheatreId() == null || request.getTheatreId().isEmpty()) {
                throw new RuntimeException("Theatre ID is required");
            }
            
            Theater theater = theaterRepository.findById(request.getTheatreId())
                    .orElseThrow(() -> new RuntimeException("Theater not found"));

            Screen screen = new Screen();
            screen.setName(request.getName());
            screen.setTotalSeats(request.getTotalSeats());
            screen.setRows(request.getRows());
            screen.setSeatsPerRow(request.getSeatsPerRow());
            screen.setTheater(theater);
            screen.setIsActive(true);
            Screen saved = screenRepository.save(screen);

            if (request.getCategories() != null) {
                for (CategoryDTO catDTO : request.getCategories()) {
                    SeatCategory cat = new SeatCategory();
                    cat.setId(catDTO.getId());
                    cat.setName(catDTO.getName());
                    cat.setPrice(catDTO.getPrice());
                    cat.setColor(catDTO.getColor());
                    cat.setScreen(saved);
                    seatCategoryRepository.save(cat);
                }
            }

            if (request.getSeatMap() != null) {
                for (SeatDataDTO seatDTO : request.getSeatMap()) {
                    SeatData seat = new SeatData();
                    seat.setSeatId(seatDTO.getSeatId());
                    seat.setLabel(seatDTO.getLabel());
                    seat.setRow(seatDTO.getRow());
                    seat.setCol(seatDTO.getCol());
                    seat.setCategoryId(seatDTO.getCategoryId());
                    seat.setStatus(seatDTO.getStatus());
                    seat.setScreen(saved);
                    seatDataRepository.save(seat);
                }
            }

            request.setId(saved.getId());
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create screen: " + e.getMessage(), e);
        }
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ScreenConfigDTO> updateScreen(
            @PathVariable String id,
            @RequestBody ScreenConfigDTO request) {
        try {
            Screen screen = screenRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Screen not found"));
            
            screen.setName(request.getName());
            screen.setTotalSeats(request.getTotalSeats());
            screen.setRows(request.getRows());
            screen.setSeatsPerRow(request.getSeatsPerRow());
            Screen updated = screenRepository.save(screen);

            seatCategoryRepository.deleteByScreenId(id);
            seatDataRepository.deleteByScreenId(id);

            if (request.getCategories() != null) {
                for (CategoryDTO catDTO : request.getCategories()) {
                    SeatCategory cat = new SeatCategory();
                    cat.setId(catDTO.getId());
                    cat.setName(catDTO.getName());
                    cat.setPrice(catDTO.getPrice());
                    cat.setColor(catDTO.getColor());
                    cat.setScreen(updated);
                    seatCategoryRepository.save(cat);
                }
            }

            if (request.getSeatMap() != null) {
                for (SeatDataDTO seatDTO : request.getSeatMap()) {
                    SeatData seat = new SeatData();
                    seat.setSeatId(seatDTO.getSeatId());
                    seat.setLabel(seatDTO.getLabel());
                    seat.setRow(seatDTO.getRow());
                    seat.setCol(seatDTO.getCol());
                    seat.setCategoryId(seatDTO.getCategoryId());
                    seat.setStatus(seatDTO.getStatus());
                    seat.setScreen(updated);
                    seatDataRepository.save(seat);
                }
            }

            request.setId(updated.getId());
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update screen: " + e.getMessage(), e);
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteScreen(@PathVariable String id) {
        seatCategoryRepository.deleteByScreenId(id);
        seatDataRepository.deleteByScreenId(id);
        screenRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
