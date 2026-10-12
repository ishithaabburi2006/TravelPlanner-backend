package com.travelplanner.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Destination {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(nullable=false) private String name;
  private String location;
  private String state;
  private String type;
  private String interests;
  private double averageBudget;
  private int recommendedDays;
  private String bestTime;
  private String travelFromHyderabad;
  private String idealFor;
  @Column(length=1800) private String highlights;
  @Column(length=1800) private String description;
  private String imageUrl;
}
