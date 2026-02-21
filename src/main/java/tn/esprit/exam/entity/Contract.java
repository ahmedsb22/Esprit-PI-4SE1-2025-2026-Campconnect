package tn.esprit.exam.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "contracts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true, unique = true, length = 50)
    private String contractNumber;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String terms;

    @Column(nullable = false)
    private Boolean isSigned = false;

    @Column
    private LocalDateTime signedAt;

    @Column(length = 500)
    private String signatureUrl; // URL to digital signature image

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 20)
    private ContractStatus status;

    // Relationship
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = true, unique = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "equipments", "contract", "camper", "campingSite"})
    private Reservation reservation;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void generateContractNumber() {
        if (this.contractNumber == null) {
            this.contractNumber = "CONT-" + System.currentTimeMillis();
        }
    }

    public void sign() {
        this.isSigned = true;
        this.signedAt = LocalDateTime.now();
        this.status = ContractStatus.SIGNED;
    }
}
