package tn.esprit.exam.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tn.esprit.exam.dto.EquipmentDTO; // ← Import du DTO
import tn.esprit.exam.entity.Equipment;
import tn.esprit.exam.repository.EquipmentRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/equipment")
@Tag(
    name = "Equipment",
    description = "Gestion des équipements de camping - catalogue, disponibilité et CRUD"
)
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentRepository equipmentRepository;

    @GetMapping
    @Operation(
        summary = "Récupérer tous les équipements",
        description = "Retourne la liste complète de tous les équipements disponibles"
    )
    @ApiResponse(responseCode = "200", description = "Liste des équipements")
    @Transactional(readOnly = true)
    public List<EquipmentDTO> getAll() {
        return equipmentRepository.findAll().stream()
                .map(EquipmentDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/available")
    @Operation(
        summary = "Récupérer les équipements disponibles",
        description = "Retourne uniquement les équipements actifs avec une quantité disponible > 0"
    )
    @ApiResponse(responseCode = "200", description = "Liste des équipements en stock")
    @Transactional(readOnly = true)
    public List<EquipmentDTO> getAvailable() {
        return equipmentRepository.findAll().stream()
                .filter(e -> Boolean.TRUE.equals(e.getIsActive())
                        && e.getAvailableQuantity() != null
                        && e.getAvailableQuantity() > 0)
                .map(EquipmentDTO::fromEntity)  // ← Conversion vers DTO
                .collect(Collectors.toList());
    }

    @GetMapping("/search")
    @Operation(
        summary = "Rechercher des équipements avec filtres",
        description = "Effectue une recherche avancée sur les équipements par nom, catégorie et prix"
    )
    @ApiResponse(responseCode = "200", description = "Équipements correspondant aux critères")
    @Transactional(readOnly = true)
    public List<EquipmentDTO> search(
            @RequestParam(required = false)
            @Parameter(description = "Nom de l'équipement (recherche partielle)")
            String name,
            
            @RequestParam(required = false)
            @Parameter(description = "Catégorie de l'équipement")
            String category,
            
            @RequestParam(required = false)
            @Parameter(description = "Prix journalier minimum", example = "5.0")
            Double priceMin,
            
            @RequestParam(required = false)
            @Parameter(description = "Prix journalier maximum", example = "50.0")
            Double priceMax) {
        return equipmentRepository.findAll().stream()
                .filter(e -> name == null || (e.getName() != null && e.getName().toLowerCase().contains(name.toLowerCase())))
                .filter(e -> category == null || (e.getCategory() != null && e.getCategory().equalsIgnoreCase(category)))
                .filter(e -> priceMin == null || (e.getPricePerDay() != null && e.getPricePerDay().doubleValue() >= priceMin))
                .filter(e -> priceMax == null || (e.getPricePerDay() != null && e.getPricePerDay().doubleValue() <= priceMax))
                .map(EquipmentDTO::fromEntity)  // ← Conversion vers DTO
                .collect(Collectors.toList());
    }

    @GetMapping("/category/{category}")
    @Operation(
        summary = "Récupérer les équipements par catégorie",
        description = "Retourne tous les équipements d'une catégorie spécifique"
    )
    @ApiResponse(responseCode = "200", description = "Équipements de la catégorie")
    @Transactional(readOnly = true)
    public List<EquipmentDTO> getByCategory(
            @PathVariable
            @Parameter(description = "Catégorie (ex: Tent, Sleeping Bag)", example = "Tent")
            String category) {
        return equipmentRepository.findAll().stream()
                .filter(e -> category.equalsIgnoreCase(e.getCategory()))
                .map(EquipmentDTO::fromEntity)  // ← Conversion vers DTO
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Récupérer un équipement par ID",
        description = "Retourne les détails complets d'un équipement spécifique"
    )
    @ApiResponse(responseCode = "200", description = "Équipement trouvé")
    @ApiResponse(responseCode = "404", description = "Équipement non trouvé")
    @Transactional(readOnly = true)
    public EquipmentDTO getById(
            @PathVariable
            @Parameter(description = "ID unique de l'équipement", example = "1")
            Long id) {
        return equipmentRepository.findById(id)
                .map(EquipmentDTO::fromEntity)  // ← Conversion vers DTO
                .orElseThrow(() -> new IllegalArgumentException("Equipment not found: " + id));
    }

    // ⚠️ Pour les méthodes POST/PUT, vous pouvez garder Equipment en entrée
    // mais retourner DTO en sortie pour la cohérence

    @PostMapping
    @Operation(
        summary = "Créer un nouvel équipement",
        description = "Crée un nouvel équipement dans le système avec les informations fournies"
    )
    @ApiResponse(responseCode = "201", description = "Équipement créé avec succès")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    @Transactional
    public EquipmentDTO create(
            @RequestBody
            EquipmentDTO equipmentDTO) {
        Equipment equipment = new Equipment();
        equipment.setName(equipmentDTO.getName());
        equipment.setDescription(equipmentDTO.getDescription());
        equipment.setCategory(equipmentDTO.getCategory());
        equipment.setPricePerDay(equipmentDTO.getPricePerDay());
        equipment.setStockQuantity(equipmentDTO.getStockQuantity());
        equipment.setAvailableQuantity(equipmentDTO.getAvailableQuantity());
        equipment.setImageUrl(equipmentDTO.getImageUrl());
        equipment.setSpecifications(equipmentDTO.getSpecifications());
        equipment.setIsActive(equipmentDTO.getIsActive());
        equipment.setReservationEquipments(new java.util.HashSet<>());
        
        Equipment saved = equipmentRepository.save(equipment);
        return EquipmentDTO.fromEntity(saved);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Mettre à jour un équipement",
        description = "Met à jour les informations d'un équipement existant"
    )
    @ApiResponse(responseCode = "200", description = "Équipement mis à jour avec succès")
    @ApiResponse(responseCode = "404", description = "Équipement non trouvé")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    @Transactional
    public EquipmentDTO update(
            @PathVariable
            @Parameter(description = "ID de l'équipement à mettre à jour", example = "1")
            Long id,
            @RequestBody
            EquipmentDTO equipmentDTO) {
        Equipment existing = equipmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Equipment not found: " + id));
        
        if (equipmentDTO.getName() != null) existing.setName(equipmentDTO.getName());
        if (equipmentDTO.getDescription() != null) existing.setDescription(equipmentDTO.getDescription());
        if (equipmentDTO.getCategory() != null) existing.setCategory(equipmentDTO.getCategory());
        if (equipmentDTO.getPricePerDay() != null) existing.setPricePerDay(equipmentDTO.getPricePerDay());
        if (equipmentDTO.getStockQuantity() != null) existing.setStockQuantity(equipmentDTO.getStockQuantity());
        if (equipmentDTO.getAvailableQuantity() != null) existing.setAvailableQuantity(equipmentDTO.getAvailableQuantity());
        if (equipmentDTO.getImageUrl() != null) existing.setImageUrl(equipmentDTO.getImageUrl());
        if (equipmentDTO.getSpecifications() != null) existing.setSpecifications(equipmentDTO.getSpecifications());
        if (equipmentDTO.getIsActive() != null) existing.setIsActive(equipmentDTO.getIsActive());
        
        Equipment updated = equipmentRepository.save(existing);
        return EquipmentDTO.fromEntity(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Supprimer un équipement",
        description = "Supprime définitivement un équipement du système"
    )
    @ApiResponse(responseCode = "204", description = "Équipement supprimé avec succès")
    @ApiResponse(responseCode = "404", description = "Équipement non trouvé")
    @Transactional
    public ResponseEntity<Void> delete(
            @PathVariable
            @Parameter(description = "ID de l'équipement à supprimer", example = "1")
            Long id) {
        if (!equipmentRepository.existsById(id)) throw new IllegalArgumentException("Equipment not found: " + id);
        equipmentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}