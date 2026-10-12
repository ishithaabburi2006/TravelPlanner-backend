package com.travelplanner.repository;

import com.travelplanner.entity.Attraction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface AttractionRepository extends JpaRepository<Attraction, Long> {
  List<Attraction> findByDestinationIdOrderByDistanceKmAsc(Long destinationId);
  List<Attraction> findByDestinationId(Long destinationId);
  Optional<Attraction> findByDestinationIdAndNameIgnoreCase(Long destinationId, String name);
  long countByDestinationId(Long destinationId);
  void deleteByDestinationId(Long destinationId);
}
