package tn.esprit.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.exam.entity.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO pour les réservations de camping")
public class ReservationDTO {
    @Schema(description = "ID unique de la réservation", example = "1")
    private Long id;

    @Schema(description = "Numéro de réservation unique", example = "RES-2024-001")
    private String reservationNumber;

    @NotNull(message = "Check-in date is required")
    @Future(message = "Check-in date must be in the future")
    @Schema(description = "Date d'arrivée", example = "2024-06-15")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    @Future(message = "Check-out date must be in the future")
    @Schema(description = "Date de départ", example = "2024-06-20")
    private LocalDate checkOutDate;

    @NotNull(message = "Number of guests is required")
    @Min(value = 1)
    @Schema(description = "Nombre de personnes", example = "4")
    private Integer numberOfGuests;

    @Schema(description = "Prix total de la réservation", example = "500.00")
    private BigDecimal totalPrice;

    @Schema(description = "Statut de la réservation (PENDING, CONFIRMED, CANCELLED)", example = "CONFIRMED")
    private ReservationStatus status;

    @Schema(description = "Demandes spéciales du client", example = "Chambre avec vue")
    private String specialRequests;

    @NotNull(message = "Camping site is required")
    @Schema(description = "ID du site de camping", example = "1")
    private Long campingSiteId;

    @Schema(description = "Nom du site de camping")
    private String campingSiteName;

    @Schema(description = "Localité du site")
    private String campingSiteLocation;

    @Schema(description = "ID du client", example = "1")
    private Long camperId;

    @Schema(description = "Nom du client")
    private String camperName;

    @Schema(description = "Email du client")
    private String camperEmail;

    @Schema(description = "Liste des équipements loués")
    private List<ReservationEquipmentDTO> equipments;

    @Schema(description = "ID du contrat associé")
    private Long contractId;
}
