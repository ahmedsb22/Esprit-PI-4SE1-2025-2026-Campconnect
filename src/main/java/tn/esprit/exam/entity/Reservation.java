package tn.esprit.exam.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true, unique = true, length = 50)
    private String reservationNumber;

    @Column(nullable = true)
    private LocalDate checkInDate;

    @Column(nullable = true)
    private LocalDate checkOutDate;

    @Column(nullable = true)
    private Integer numberOfGuests;

    @Column(nullable = true, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 20)
    private ReservationStatus status;

    @Column(columnDefinition = "TEXT")
    private String specialRequests;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camper_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "roles", "reservations"})
    private User camper;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camping_site_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "reservations", "owner"})
    private CampingSite campingSite;

    @JsonIgnore
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ReservationEquipment> equipments = new HashSet<>();

    @JsonIgnore
    @OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private Contract contract;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void generateReservationNumber() {
        if (this.reservationNumber == null) {
            this.reservationNumber = "RES-" + System.currentTimeMillis();
        }
    }


}
