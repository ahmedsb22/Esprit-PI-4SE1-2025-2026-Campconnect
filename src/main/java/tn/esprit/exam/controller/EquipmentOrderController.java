package tn.esprit.exam.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.exam.dto.EquipmentOrderDTO;
import tn.esprit.exam.service.IEquipmentOrderService;

import java.util.List;

@RestController
@RequestMapping("/api/equipment-orders")
@Tag(name = "Equipment Orders", description = "Gestion des commandes d'équipement")
@RequiredArgsConstructor
public class EquipmentOrderController {

    private final IEquipmentOrderService equipmentOrderService;

    @GetMapping
    @Operation(summary = "Récupérer toutes les commandes")
    public ResponseEntity<List<EquipmentOrderDTO>> getAll() {
        return ResponseEntity.ok(equipmentOrderService.getAllOrders());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une commande par ID")
    @ApiResponse(responseCode = "200", description = "Commande trouvée")
    @ApiResponse(responseCode = "404", description = "Commande non trouvée")
    public ResponseEntity<EquipmentOrderDTO> getById(
            @PathVariable @Parameter(description = "ID de la commande") Long id) {
        return ResponseEntity.ok(equipmentOrderService.getOrderById(id));
    }

    @GetMapping("/reservation/{reservationId}")
    @Operation(summary = "Récupérer les commandes d'une réservation")
    public ResponseEntity<List<EquipmentOrderDTO>> getByReservation(
            @PathVariable @Parameter(description = "ID de la réservation") Long reservationId) {
        return ResponseEntity.ok(equipmentOrderService.getOrdersByReservation(reservationId));
    }

    @PostMapping
    @Operation(summary = "Créer une commande — liée à l'utilisateur et calcul automatique du prix")
    @ApiResponse(responseCode = "201", description = "Commande créée avec succès")
    @ApiResponse(responseCode = "400", description = "Équipement non disponible")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAMPER')")
    public ResponseEntity<EquipmentOrderDTO> create(
            @RequestBody EquipmentOrderDTO dto,
            @RequestParam Long userId) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(equipmentOrderService.createOrder(dto, userId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une commande")
    @ApiResponse(responseCode = "200", description = "Commande mise à jour")
    @ApiResponse(responseCode = "404", description = "Commande non trouvée")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAMPER')")
    public ResponseEntity<EquipmentOrderDTO> update(
            @PathVariable @Parameter(description = "ID de la commande") Long id,
            @RequestBody EquipmentOrderDTO dto) {
        return ResponseEntity.ok(equipmentOrderService.updateOrder(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Annuler une commande — remet la quantité disponible")
    @ApiResponse(responseCode = "204", description = "Commande annulée")
    @ApiResponse(responseCode = "404", description = "Commande non trouvée")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAMPER')")
    public ResponseEntity<Void> cancel(
            @PathVariable @Parameter(description = "ID de la commande") Long id) {
        equipmentOrderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }
}