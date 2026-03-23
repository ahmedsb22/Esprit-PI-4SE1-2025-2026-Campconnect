package tn.esprit.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO pour les équipements dans une réservation")
public class ReservationEquipmentDTO {
    @Schema(description = "ID unique", example = "1")
    private Long id;

    @NotNull(message = "Equipment is required")
    @Schema(description = "ID de l'équipement", example = "1")
    private Long equipmentId;
    
    @Schema(description = "Nom de l'équipement", example = "Sleeping Bag")
    private String equipmentName;

    @Schema(description = "Catégorie de l'équipement", example = "Sleeping Gear")
    private String equipmentCategory;

    @NotNull(message = "Quantity is required")
    @Min(value = 1)
    @Schema(description = "Quantité louée", example = "2")
    private Integer quantity;

    @Schema(description = "Prix journalier", example = "15.00")
    private BigDecimal pricePerDay;

    @Schema(description = "Sous-total (quantité × prix)", example = "30.00")
    private BigDecimal subtotal;
}
