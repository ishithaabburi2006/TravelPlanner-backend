package com.travelplanner.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Table(name = "attractions", indexes = {
  @Index(name = "idx_attraction_destination", columnList = "destinationId"),
  @Index(name = "idx_attraction_name", columnList = "name")
})
public class Attraction {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @Column(nullable=false) private Long destinationId;
  @Column(nullable=false) private String name;
  private String category;
  private double distanceKm;
  private int estimatedMinutes;
  private String bestFor;
  private String visitDuration;
  @Column(length=1600) private String description;
  private String imageUrl;
  private String mapUrl;
}
