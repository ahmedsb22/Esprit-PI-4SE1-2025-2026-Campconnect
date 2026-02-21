package tn.esprit.exam.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 50)
    private String invoiceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "equipments", "contract", "camper", "campingSite"})
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_order_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "reservation", "equipment"})
    private ReservationEquipment equipmentOrder;

    @Column(nullable = true, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime issuedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public enum InvoiceStatus {
        DRAFT, SENT, PAID, CANCELLED
    }
}
