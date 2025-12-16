package com.revticket.movie.repository;

import com.revticket.movie.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, String> {
    List<Movie> findByIsActiveTrue();
    
    Long countByIsActiveTrue();
    
    @Query("SELECT COUNT(m) FROM Movie m WHERE m.releaseDate > :today")
    Long countUpcomingMovies(@Param("today") LocalDate today);
}
