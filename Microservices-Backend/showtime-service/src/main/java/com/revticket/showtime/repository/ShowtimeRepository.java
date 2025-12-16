package com.revticket.showtime.repository;

import com.revticket.showtime.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, String> {

    List<Showtime> findAllByOrderByShowDateTimeAsc();

    List<Showtime> findByMovieId(String movieId);

    List<Showtime> findByTheaterId(String theaterId);

    @Query("SELECT s FROM Showtime s WHERE s.movieId = :movieId AND s.showDateTime BETWEEN :start AND :end")
    List<Showtime> findByMovieIdAndShowDateBetween(@Param("movieId") String movieId,
                                                   @Param("start") LocalDateTime start,
                                                   @Param("end") LocalDateTime end);

    @Query("SELECT s FROM Showtime s WHERE s.screen = :screen AND s.showDateTime BETWEEN :start AND :end")
    List<Showtime> findByScreenAndShowDateTimeBetween(@Param("screen") String screen,
                                                       @Param("start") LocalDateTime start,
                                                       @Param("end") LocalDateTime end);

    @Query("SELECT s FROM Showtime s WHERE s.theaterId = :theaterId AND s.showDateTime BETWEEN :start AND :end")
    List<Showtime> findByTheaterIdAndShowDateBetween(@Param("theaterId") String theaterId,
                                                      @Param("start") LocalDateTime start,
                                                      @Param("end") LocalDateTime end);

    @Query("SELECT s FROM Showtime s WHERE s.showDateTime BETWEEN :start AND :end ORDER BY s.showDateTime ASC")
    List<Showtime> findByShowDateTimeBetween(@Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);

    List<Showtime> findByScreen(String screen);
    
    @Query("SELECT COUNT(s) FROM Showtime s WHERE s.showDateTime > :now")
    Long countUpcomingShowtimes(@Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(s) FROM Showtime s WHERE s.showDateTime BETWEEN :start AND :end")
    Long countShowtimesBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
