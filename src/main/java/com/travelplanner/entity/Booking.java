package com.travelplanner.entity;
import jakarta.persistence.*; import lombok.*; import java.time.*;
@Entity @Getter @Setter @NoArgsConstructor
@Table(indexes = {
  @Index(name = "idx_booking_user_date", columnList = "user_id, booking_date"),
  @Index(name = "idx_booking_itinerary", columnList = "itinerary_id"),
  @Index(name = "idx_booking_status", columnList = "status")
})
public class Booking {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  private String serviceType;
  private String provider;
  private String bookingReference;
  private String details;
  private LocalDate bookingDate;
  private double amount;
  @Column(nullable=false) private String status="RECORDED";
  private Integer travelers=1;
  @ManyToOne(optional=false) private User user;
  @ManyToOne private Itinerary itinerary;
}
