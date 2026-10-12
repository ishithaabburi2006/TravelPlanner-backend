package com.travelplanner.repository;

import com.travelplanner.entity.Itinerary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItineraryRepository extends JpaRepository<Itinerary, Long> {
  List<Itinerary> findByUserIdOrderByStartDateDesc(Long userId);
  long countByUserId(Long userId);
}
