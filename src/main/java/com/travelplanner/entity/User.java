package com.travelplanner.entity;
import jakarta.persistence.*; import lombok.*; import com.fasterxml.jackson.annotation.JsonIgnore;
@Entity @Table(name="users") @Getter @Setter @NoArgsConstructor
public class User { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(nullable=false) private String name; @Column(unique=true,nullable=false) private String email; @JsonIgnore @Column(nullable=false) private String password; @Enumerated(EnumType.STRING) private Role role=Role.USER; }
