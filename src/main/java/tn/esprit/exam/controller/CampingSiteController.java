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
import tn.esprit.exam.dto.CampingSiteDTO;
import tn.esprit.exam.service.ICampingSiteService;

import java.util.List;

@RestController
@RequestMapping("/api/sites")
@Tag(name = "CampingSites", description = "Gestion complète des sites de camping")
@RequiredArgsConstructor
public class CampingSiteController {

    private final ICampingSiteService campingSiteService;

    @GetMapping
    @Operation(summary = "Récupérer tous les sites")
    public ResponseEntity<List<CampingSiteDTO>> getAll() {
        return ResponseEntity.ok(campingSiteService.getAllCampingSites());
    }

    @GetMapping("/active")
    @Operation(summary = "Récupérer les sites actifs et vérifiés")
    public ResponseEntity<List<CampingSiteDTO>> getActive() {
        return ResponseEntity.ok(campingSiteService.getActiveCampingSites());
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher les sites avec filtres")
    public ResponseEntity<List<CampingSiteDTO>> search(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {
        return ResponseEntity.ok(
            campingSiteService.getAllCampingSites().stream()
                .filter(s -> location == null || (s.getLocation() != null &&
                        s.getLocation().toLowerCase().contains(location.toLowerCase())))
                .filter(s -> category == null || (s.getCategory() != null &&
                        s.getCategory().equalsIgnoreCase(category)))
                .filter(s -> minPrice == null || (s.getPricePerNight() != null &&
                        s.getPricePerNight().doubleValue() >= minPrice))
                .filter(s -> maxPrice == null || (s.getPricePerNight() != null &&
                        s.getPricePerNight().doubleValue() <= maxPrice))
                .toList()
        );
    }

    @GetMapping("/location/{location}")
    @Operation(summary = "Rechercher les sites par localisation")
    public ResponseEntity<List<CampingSiteDTO>> getByLocation(@PathVariable String location) {
        return ResponseEntity.ok(campingSiteService.getCampingSitesByLocation(location));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Rechercher les sites par catégorie")
    public ResponseEntity<List<CampingSiteDTO>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(campingSiteService.getCampingSitesByCategory(category));
    }

    @GetMapping("/top-rated")
    @Operation(summary = "Récupérer les sites les mieux notés")
    public ResponseEntity<List<CampingSiteDTO>> getTopRated() {
        return ResponseEntity.ok(campingSiteService.getTopRatedCampingSites());
    }

    @GetMapping("/owner/{ownerId}")
    @Operation(summary = "Récupérer les sites d'un propriétaire")
    @PreAuthorize("hasAnyRole('ADMIN', 'SITE_OWNER')")
    public ResponseEntity<List<CampingSiteDTO>> getByOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(campingSiteService.getCampingSitesByOwner(ownerId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un site par ID")
    public ResponseEntity<CampingSiteDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(campingSiteService.getCampingSiteById(id));
    }

    @PostMapping
    @Operation(summary = "Créer un nouveau site — lié à l'owner connecté")
    @PreAuthorize("hasAnyRole('ADMIN', 'SITE_OWNER')")
    public ResponseEntity<CampingSiteDTO> create(
            @RequestBody CampingSiteDTO dto,
            @RequestParam Long ownerId) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(campingSiteService.createCampingSite(dto, ownerId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un site")
    @PreAuthorize("hasAnyRole('ADMIN', 'SITE_OWNER')")
    public ResponseEntity<CampingSiteDTO> update(
            @PathVariable Long id,
            @RequestBody CampingSiteDTO dto,
            @RequestParam Long ownerId) {
        return ResponseEntity.ok(campingSiteService.updateCampingSite(id, dto, ownerId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un site et ses réservations associées")
    @PreAuthorize("hasAnyRole('ADMIN', 'SITE_OWNER')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam Long ownerId) {
        campingSiteService.deleteCampingSite(id, ownerId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approuver un site — Admin uniquement")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CampingSiteDTO> approve(@PathVariable Long id) {
        return ResponseEntity.ok(campingSiteService.verifyCampingSite(id));
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Rejeter un site — Admin uniquement")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CampingSiteDTO> reject(@PathVariable Long id) {
        return ResponseEntity.ok(campingSiteService.rejectCampingSite(id));
    }

    @PutMapping("/{id}/toggle-status")
    @Operation(summary = "Activer ou désactiver un site")
    @PreAuthorize("hasAnyRole('ADMIN', 'SITE_OWNER')")
    public ResponseEntity<CampingSiteDTO> toggleStatus(
            @PathVariable Long id,
            @RequestParam Long ownerId) {
        return ResponseEntity.ok(campingSiteService.toggleActiveStatus(id, ownerId));
    }
}