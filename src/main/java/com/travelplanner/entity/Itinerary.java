package com.travelplanner.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity
@Getter @Setter @NoArgsConstructor
public class Itinerary {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(nullable=false) private String title;
  private String destination;
  private LocalDate startDate;
  private LocalDate endDate;
  private double budget;
  private int travelers=1;
  private String travelMode;
  private String accommodation;
  private String foodPreference;
  @Column(length=2200) private String activities;
  @Column(length=3500) private String dayPlan;
  @Column(length=3000) private String notes;
  private String status="PLANNED";
  @ManyToOne(optional=false) private User user;
}
