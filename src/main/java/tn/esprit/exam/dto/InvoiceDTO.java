package tn.esprit.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.exam.entity.Invoice;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO pour les factures")
public class InvoiceDTO {
    
    @Schema(description = "ID unique de la facture", example = "1")
    private Long id;

    @NotBlank(message = "Invoice number is required")
    @Schema(description = "Numéro de facture unique", example = "INV-2024-001")
    private String invoiceNumber;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0")
    @Schema(description = "Montant total TTC", example = "250.00")
    private BigDecimal totalAmount;

    @Schema(description = "Statut de la facture (DRAFT, SENT, PAID, CANCELLED)", example = "PAID")
    private String status;

    @Schema(description = "Date d'émission")
    private LocalDateTime issuedAt;

    @Schema(description = "Notes supplémentaires")
    private String notes;

    @Schema(description = "Nom du client")
    private String camperName;

    @Schema(description = "Email du client")
    private String camperEmail;

    @Schema(description = "Nom du site de camping")
    private String siteName;

    @Schema(description = "ID de la réservation associée")
    private Long reservationId;

    @Schema(description = "ID de la commande d'équipement associée")
    private Long equipmentOrderId;

    // ✅ Méthode de conversion Entity → DTO
    public static InvoiceDTO fromEntity(Invoice invoice) {
        if (invoice == null) return null;

        String camperName = "N/A";
        String camperEmail = "";
        String siteName = "N/A";

        if (invoice.getReservation() != null) {
            if (invoice.getReservation().getCamper() != null) {
                camperName = (invoice.getReservation().getCamper().getFirstName() != null 
                    ? invoice.getReservation().getCamper().getFirstName() : "") + " " +
                    (invoice.getReservation().getCamper().getLastName() != null 
                    ? invoice.getReservation().getCamper().getLastName() : "");
                camperEmail = invoice.getReservation().getCamper().getEmail() != null 
                    ? invoice.getReservation().getCamper().getEmail() : "";
            }
            if (invoice.getReservation().getCampingSite() != null) {
                siteName = invoice.getReservation().getCampingSite().getName() != null 
                    ? invoice.getReservation().getCampingSite().getName() : "N/A";
            }
        }

        return InvoiceDTO.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .totalAmount(invoice.getTotalAmount())
                .status(invoice.getStatus() != null ? invoice.getStatus().name() : "DRAFT")
                .issuedAt(invoice.getIssuedAt())
                .notes(invoice.getNotes())
                .camperName(camperName.trim())
                .camperEmail(camperEmail)
                .siteName(siteName)
                .reservationId(invoice.getReservation() != null ? invoice.getReservation().getId() : null)
                .equipmentOrderId(invoice.getEquipmentOrder() != null ? invoice.getEquipmentOrder().getId() : null)
                .build();
    }
}
