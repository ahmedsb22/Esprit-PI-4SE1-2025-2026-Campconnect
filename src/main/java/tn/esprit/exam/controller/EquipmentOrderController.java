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
import tn.esprit.exam.dto.EquipmentOrderDTO;
import tn.esprit.exam.entity.Equipment;
import tn.esprit.exam.entity.Reservation;
import tn.esprit.exam.entity.ReservationEquipment;
import tn.esprit.exam.exception.ResourceNotFoundException;
import tn.esprit.exam.repository.EquipmentRepository;
import tn.esprit.exam.repository.ReservationEquipmentRepository;
import tn.esprit.exam.repository.ReservationRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/equipment-orders")
@Tag(
    name = "Equipment Orders",
    description = "Gestion des commandes d'équipement - location et CRUD"
)
@RequiredArgsConstructor
public class EquipmentOrderController {

    private final ReservationEquipmentRepository reservationEquipmentRepository;
    private final EquipmentRepository equipmentRepository;
    private final ReservationRepository reservationRepository;

    @GetMapping
    @Operation(
        summary = "Récupérer toutes les commandes d'équipement",
        description = "Retourne la liste complète de toutes les commandes d'équipement"
    )
    @ApiResponse(responseCode = "200", description = "Liste des commandes")
    @Transactional(readOnly = true)
    public List<EquipmentOrderDTO> getAll() {
        return reservationEquipmentRepository.findAll().stream()
                .map(EquipmentOrderDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Récupérer une commande d'équipement par ID",
        description = "Retourne les détails complets d'une commande spécifique"
    )
    @ApiResponse(responseCode = "200", description = "Commande trouvée")
    @ApiResponse(responseCode = "404", description = "Commande non trouvée")
    @Transactional(readOnly = true)
    public EquipmentOrderDTO getById(
            @PathVariable
            @Parameter(description = "ID de la commande", example = "1")
            Long id) {
        return EquipmentOrderDTO.fromEntity(reservationEquipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id)));
    }

    @PostMapping
    @Operation(
        summary = "Créer une commande d'équipement",
        description = "Crée une nouvelle commande d'équipement pour une réservation"
    )
    @ApiResponse(responseCode = "201", description = "Commande créée avec succès")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    @ApiResponse(responseCode = "404", description = "Équipement ou réservation non trouvé")
    @Transactional
    public EquipmentOrderDTO create(
            @RequestBody

            EquipmentOrderDTO orderDTO) {
        ReservationEquipment re = new ReservationEquipment();
        
        if (orderDTO.getEquipmentId() != null) {
            Equipment equipment = equipmentRepository.findById(orderDTO.getEquipmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Equipment not found: " + orderDTO.getEquipmentId()));
            re.setEquipment(equipment);
        }

        if (orderDTO.getReservationId() != null) {
            Reservation reservation = reservationRepository.findById(orderDTO.getReservationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + orderDTO.getReservationId()));
            re.setReservation(reservation);
        }

        int qty = orderDTO.getQuantity() != null ? orderDTO.getQuantity() : 1;
        re.setQuantity(qty);

        BigDecimal ppd = orderDTO.getPricePerDay() != null 
            ? orderDTO.getPricePerDay()
            : (re.getEquipment() != null && re.getEquipment().getPricePerDay() != null 
                ? re.getEquipment().getPricePerDay() 
                : BigDecimal.ZERO);
        re.setPricePerDay(ppd);
        re.setSubtotal(ppd.multiply(BigDecimal.valueOf(qty)));

        return EquipmentOrderDTO.fromEntity(reservationEquipmentRepository.save(re));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Mettre à jour une commande d'équipement",
        description = "Met à jour la quantité et le prix d'une commande"
    )
    @ApiResponse(responseCode = "200", description = "Commande mise à jour")
    @ApiResponse(responseCode = "404", description = "Commande non trouvée")
    @Transactional
    public EquipmentOrderDTO update(
            @PathVariable
            @Parameter(description = "ID de la commande à mettre à jour", example = "1")
            Long id,
            @RequestBody
            EquipmentOrderDTO orderDTO) {
        ReservationEquipment re = reservationEquipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        
        if (orderDTO.getQuantity() != null) {
            int qty = orderDTO.getQuantity();
            re.setQuantity(qty);
            if (re.getPricePerDay() != null) {
                re.setSubtotal(re.getPricePerDay().multiply(BigDecimal.valueOf(qty)));
            }
        }
        
        if (orderDTO.getPricePerDay() != null) {
            BigDecimal ppd = orderDTO.getPricePerDay();
            re.setPricePerDay(ppd);
            if (re.getQuantity() != null) {
                re.setSubtotal(ppd.multiply(BigDecimal.valueOf(re.getQuantity())));
            }
        }
        
        return EquipmentOrderDTO.fromEntity(reservationEquipmentRepository.save(re));
    }

    @PutMapping("/{id}/status")
    @Operation(
        summary = "Mettre à jour le statut d'une commande",
        description = "Change le statut d'une commande (ACTIVE, CANCELLED)"
    )
    @ApiResponse(responseCode = "200", description = "Statut mis à jour")
    @ApiResponse(responseCode = "404", description = "Commande non trouvée")
    @Transactional
    public EquipmentOrderDTO updateStatus(
            @PathVariable
            @Parameter(description = "ID de la commande", example = "1")
            Long id,
            @RequestParam
            @Parameter(description = "Nouveau statut (ACTIVE ou CANCELLED)", example = "CANCELLED")
            String status) {
        ReservationEquipment re = reservationEquipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        if ("CANCELLED".equalsIgnoreCase(status)) {
            re.setQuantity(0);
        }
        return EquipmentOrderDTO.fromEntity(reservationEquipmentRepository.save(re));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Supprimer une commande d'équipement",
        description = "Supprime définitivement une commande du système"
    )
    @ApiResponse(responseCode = "204", description = "Commande supprimée")
    @ApiResponse(responseCode = "404", description = "Commande non trouvée")
    @Transactional
    public ResponseEntity<Void> delete(
            @PathVariable
            @Parameter(description = "ID de la commande à supprimer", example = "1")
            Long id) {
        if (!reservationEquipmentRepository.existsById(id))
            throw new ResourceNotFoundException("Order not found: " + id);
        reservationEquipmentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
