package tn.esprit.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.exam.entity.ReservationEquipment;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO pour les commandes d'équipement")
public class EquipmentOrderDTO {
    
    @Schema(description = "ID unique de la commande", example = "1")
    private Long id;

    @NotNull(message = "Equipment ID is required")
    @Schema(description = "ID de l'équipement", example = "1")
    private Long equipmentId;

    @Schema(description = "Nom de l'équipement", example = "Sleeping Bag")
    private String equipmentName;

    @Schema(description = "Catégorie de l'équipement", example = "Sleeping Gear")
    private String equipmentCategory;

    @NotNull(message = "Reservation ID is required")
    @Schema(description = "ID de la réservation associée", example = "1")
    private Long reservationId;

    @Schema(description = "Numéro de la réservation")
    private String reservationNumber;

    @Schema(description = "Nom du client")
    private String customerName;

    @NotNull(message = "Quantity is required")
    @Min(value = 1)
    @Schema(description = "Quantité louée", example = "2")
    private Integer quantity;

    @NotNull(message = "Price per day is required")
    @DecimalMin(value = "0.0")
    @Schema(description = "Prix journalier", example = "15.00")
    private BigDecimal pricePerDay;

    @Schema(description = "Sous-total (quantité × prix)", example = "30.00")
    private BigDecimal subtotal;

    @Schema(description = "Statut (ACTIVE ou CANCELLED)", example = "ACTIVE")
    private String status;

    // ✅ Méthode de conversion Entity → DTO
    public static EquipmentOrderDTO fromEntity(ReservationEquipment re) {
        if (re == null) return null;

        String customerName = "Anonymous";
        String equipmentName = "N/A";
        String reservationNumber = "";

        if (re.getEquipment() != null) {
            equipmentName = re.getEquipment().getName() != null ? re.getEquipment().getName() : "N/A";
        }

        if (re.getReservation() != null) {
            reservationNumber = re.getReservation().getReservationNumber() != null 
                ? re.getReservation().getReservationNumber() : "";
            if (re.getReservation().getCamper() != null) {
                String fn = re.getReservation().getCamper().getFirstName() != null 
                    ? re.getReservation().getCamper().getFirstName() : "";
                String ln = re.getReservation().getCamper().getLastName() != null 
                    ? re.getReservation().getCamper().getLastName() : "";
                customerName = (fn + " " + ln).trim();
                if (customerName.isEmpty()) {
                    customerName = re.getReservation().getCamper().getEmail() != null 
                        ? re.getReservation().getCamper().getEmail() : "Anonymous";
                }
            }
        }

        return EquipmentOrderDTO.builder()
                .id(re.getId())
                .equipmentId(re.getEquipment() != null ? re.getEquipment().getId() : null)
                .equipmentName(equipmentName)
                .equipmentCategory(re.getEquipment() != null ? re.getEquipment().getCategory() : null)
                .reservationId(re.getReservation() != null ? re.getReservation().getId() : null)
                .reservationNumber(reservationNumber)
                .customerName(customerName)
                .quantity(re.getQuantity())
                .pricePerDay(re.getPricePerDay())
                .subtotal(re.getSubtotal())
                .status(re.getQuantity() != null && re.getQuantity() == 0 ? "CANCELLED" : "ACTIVE")
                .build();
    }
}
