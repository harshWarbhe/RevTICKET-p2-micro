package com.revticket.theater.repository;

import com.revticket.theater.entity.Screen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScreenRepository extends JpaRepository<Screen, String> {
    List<Screen> findByTheaterId(String theaterId);
    List<Screen> findByTheaterIdAndIsActive(String theaterId, Boolean isActive);
}
