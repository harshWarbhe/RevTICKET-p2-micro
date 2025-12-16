package com.revticket.theater.repository;

import com.revticket.theater.entity.SeatCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatCategoryRepository extends JpaRepository<SeatCategory, String> {
    List<SeatCategory> findByScreenId(String screenId);
    void deleteByScreenId(String screenId);
}
