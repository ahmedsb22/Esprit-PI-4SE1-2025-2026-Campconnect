package tn.esprit.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.exam.entity.ContractStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO pour les contrats de camping")
public class ContractDTO {
    @Schema(description = "ID unique du contrat", example = "1")
    private Long id;

    @Schema(description = "Numéro de contrat unique", example = "CNT-1234567890")
    private String contractNumber;

    @NotBlank(message = "Contract terms are required")
    @Schema(description = "Termes et conditions du contrat")
    private String terms;

    @Schema(description = "Le contrat est signé")
    private Boolean isSigned;

    @Schema(description = "Date de signature")
    private LocalDateTime signedAt;

    @Schema(description = "URL de la signature électronique")
    private String signatureUrl;

    @Schema(description = "Statut du contrat (DRAFT, ACTIVE, EXPIRED)", example = "ACTIVE")
    private ContractStatus status;

    @NotNull(message = "Reservation is required")
    @Schema(description = "ID de la réservation associée", example = "1")
    private Long reservationId;

    @Schema(description = "Numéro de la réservation")
    private String reservationNumber;
}
