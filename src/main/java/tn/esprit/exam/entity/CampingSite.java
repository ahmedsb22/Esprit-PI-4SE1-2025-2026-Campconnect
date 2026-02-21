package tn.esprit.exam.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "camping_sites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampingSite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true, length = 100)
    private String name;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = true)
    private String location;

    @Column(nullable = true)
    private String address;

    @Column(nullable = true, precision = 10, scale = 2)
    private BigDecimal pricePerNight;

    @Column(nullable = true)
    private Integer capacity;

    @Column(length = 50)
    private String category; // Beach, Mountain, Forest, Desert

    @Column(length = 500)
    private String imageUrl;

    // Amenities
    @Column(nullable = false)
    private Boolean hasWifi = false;

    @Column(nullable = false)
    private Boolean hasParking = false;

    @Column(nullable = false)
    private Boolean hasRestrooms = false;

    @Column(nullable = false)
    private Boolean hasShowers = false;

    @Column(nullable = false)
    private Boolean hasElectricity = false;

    @Column(nullable = false)
    private Boolean hasPetFriendly = false;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Boolean isVerified = false;

    @Column(precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer reviewCount = 0;

    // Owner relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "reservations", "password", "roles"})
    private User owner;

    // Reservations relationship
    @JsonIgnore
    @OneToMany(mappedBy = "campingSite", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Reservation> reservations = new HashSet<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
