package tn.esprit.exam.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "reservation_equipment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationEquipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private Integer quantity;

    @Column(nullable = true, precision = 10, scale = 2)
    private BigDecimal pricePerDay;

    @Column(nullable = true, precision = 10, scale = 2)
    private BigDecimal subtotal;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "equipments", "contract"})
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "reservationEquipments", "provider"})
    private Equipment equipment;
}
