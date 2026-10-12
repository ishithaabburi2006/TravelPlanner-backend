package com.travelplanner.controller;

import com.travelplanner.entity.Attraction;
import com.travelplanner.repository.AttractionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/attractions")
public class AttractionController {
  private final AttractionRepository repo;
  public AttractionController(AttractionRepository repo){ this.repo = repo; }

  @GetMapping("/destination/{destinationId}")
  public List<Attraction> byDestination(@PathVariable Long destinationId){
    return repo.findByDestinationIdOrderByDistanceKmAsc(destinationId);
  }

  @GetMapping("/{id}")
  public Attraction one(@PathVariable Long id){
    return repo.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attraction not found"));
  }
}
