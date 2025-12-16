package com.revticket.showtime.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "showtimes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Showtime {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "movie_id", nullable = false)
    private String movieId;

    @Column(name = "theater_id", nullable = false)
    private String theaterId;

    @Column(nullable = false)
    private String screen;

    @Column(name = "show_date_time", nullable = false)
    private LocalDateTime showDateTime;

    @Column(name = "ticket_price", nullable = false)
    private Double ticketPrice;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;

    @Enumerated(EnumType.STRING)
    private ShowStatus status = ShowStatus.ACTIVE;

    public enum ShowStatus {
        ACTIVE, COMPLETED, CANCELLED
    }
}
