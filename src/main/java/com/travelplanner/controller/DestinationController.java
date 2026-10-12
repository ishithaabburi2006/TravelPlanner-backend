package com.travelplanner.controller;

import com.travelplanner.entity.Destination;
import com.travelplanner.repository.DestinationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/destinations")
public class DestinationController {
  private final DestinationRepository repo;

  public DestinationController(DestinationRepository repo) {
    this.repo = repo;
  }

  @GetMapping
  public List<Destination> all(
      @RequestParam(defaultValue = "") String q,
      @RequestParam(defaultValue = "") String type
  ) {
    List<Destination> list = q.isBlank() ? repo.findAll() : repo.search(q.trim());
    if (type.isBlank()) return list;
    return list.stream().filter(destination -> type.equalsIgnoreCase(destination.getType())).toList();
  }

  @GetMapping("/{id}")
  public Destination one(@PathVariable Long id) {
    return repo.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Destination not found"));
  }
}
