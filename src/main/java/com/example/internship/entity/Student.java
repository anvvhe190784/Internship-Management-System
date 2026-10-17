package com.example.internship.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

  @Id
  @Column(name = "student_id")
  private Integer studentId;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "student_id")
  private User user;

  @Column(name = "student_code", nullable = false, unique = true, length = 20)
  private String studentCode;

  @Column(name = "major", length = 100)
  private String major;

  @Column(name = "class", length = 50)
  private String className;

  @Column(name = "date_of_birth")
  private LocalDate dateOfBirth;

  @Column(name = "address", length = 255)
  private String address;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;
}
