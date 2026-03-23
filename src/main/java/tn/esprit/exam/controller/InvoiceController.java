package tn.esprit.exam.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tn.esprit.exam.dto.InvoiceDTO;
import tn.esprit.exam.entity.Invoice;
import tn.esprit.exam.entity.Reservation;
import tn.esprit.exam.entity.ReservationEquipment;
import tn.esprit.exam.exception.ResourceNotFoundException;
import tn.esprit.exam.repository.InvoiceRepository;
import tn.esprit.exam.repository.ReservationEquipmentRepository;
import tn.esprit.exam.repository.ReservationRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/invoices")
@Tag(
    name = "Invoices",
    description = "Gestion des factures - CRUD et suivi des paiements"
)
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceRepository invoiceRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationEquipmentRepository reservationEquipmentRepository;

    @GetMapping
    @Operation(
        summary = "Récupérer toutes les factures",
        description = "Retourne la liste complète de toutes les factures du système"
    )
    @ApiResponse(responseCode = "200", description = "Liste des factures")
    @Transactional(readOnly = true)
    public List<InvoiceDTO> getAll() {
        return invoiceRepository.findAll().stream()
                .map(InvoiceDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Récupérer une facture par ID",
        description = "Retourne le détail complet d'une facture spécifique"
    )
    @ApiResponse(responseCode = "200", description = "Facture trouvée")
    @ApiResponse(responseCode = "404", description = "Facture non trouvée")
    @Transactional(readOnly = true)
    public InvoiceDTO getById(
            @PathVariable
            @Parameter(description = "ID de la facture", example = "1")
            Long id) {
        return InvoiceDTO.fromEntity(invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id)));
    }

    @PostMapping
    @Operation(
        summary = "Créer une nouvelle facture",
        description = "Crée une nouvelle facture associée à une réservation ou à une commande d'équipement"
    )
    @ApiResponse(responseCode = "201", description = "Facture créée avec succès")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    @ApiResponse(responseCode = "404", description = "Réservation ou commande non trouvée")
    @Transactional
    public InvoiceDTO create(
            @RequestBody

            InvoiceDTO invoiceDTO) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invoiceDTO.getInvoiceNumber() != null ? invoiceDTO.getInvoiceNumber() : "INV-" + System.currentTimeMillis());
        invoice.setStatus(invoiceDTO.getStatus() != null 
            ? Invoice.InvoiceStatus.valueOf(invoiceDTO.getStatus().toUpperCase())
            : Invoice.InvoiceStatus.DRAFT);
        invoice.setIssuedAt(LocalDateTime.now());
        invoice.setTotalAmount(invoiceDTO.getTotalAmount() != null ? invoiceDTO.getTotalAmount() : BigDecimal.ZERO);
        invoice.setNotes(invoiceDTO.getNotes());

        if (invoiceDTO.getReservationId() != null) {
            Reservation res = reservationRepository.findById(invoiceDTO.getReservationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + invoiceDTO.getReservationId()));
            invoice.setReservation(res);
            if (invoice.getTotalAmount().compareTo(BigDecimal.ZERO) == 0 && res.getTotalPrice() != null) {
                invoice.setTotalAmount(res.getTotalPrice());
            }
        }

        if (invoiceDTO.getEquipmentOrderId() != null) {
            ReservationEquipment re = reservationEquipmentRepository.findById(invoiceDTO.getEquipmentOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + invoiceDTO.getEquipmentOrderId()));
            invoice.setEquipmentOrder(re);
            if (invoice.getTotalAmount().compareTo(BigDecimal.ZERO) == 0 && re.getSubtotal() != null) {
                invoice.setTotalAmount(re.getSubtotal());
            }
        }
        
        return InvoiceDTO.fromEntity(invoiceRepository.save(invoice));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Mettre à jour une facture",
        description = "Met à jour le statut, le montant ou les notes d'une facture"
    )
    @ApiResponse(responseCode = "200", description = "Facture mise à jour")
    @ApiResponse(responseCode = "404", description = "Facture non trouvée")
    @Transactional
    public InvoiceDTO update(
            @PathVariable
            @Parameter(description = "ID de la facture à mettre à jour", example = "1")
            Long id,
            @RequestBody
            InvoiceDTO invoiceDTO) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id));
        
        if (invoiceDTO.getStatus() != null) {
            try {
                invoice.setStatus(Invoice.InvoiceStatus.valueOf(invoiceDTO.getStatus().toUpperCase()));
            } catch (Exception ignored) {}
        }
        if (invoiceDTO.getTotalAmount() != null) {
            invoice.setTotalAmount(invoiceDTO.getTotalAmount());
        }
        if (invoiceDTO.getNotes() != null) {
            invoice.setNotes(invoiceDTO.getNotes());
        }
        
        return InvoiceDTO.fromEntity(invoiceRepository.save(invoice));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Supprimer une facture",
        description = "Supprime définitivement une facture du système"
    )
    @ApiResponse(responseCode = "204", description = "Facture supprimée")
    @ApiResponse(responseCode = "404", description = "Facture non trouvée")
    @Transactional
    public ResponseEntity<Void> delete(
            @PathVariable
            @Parameter(description = "ID de la facture à supprimer", example = "1")
            Long id) {
        if (!invoiceRepository.existsById(id))
            throw new ResourceNotFoundException("Invoice not found: " + id);
        invoiceRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
