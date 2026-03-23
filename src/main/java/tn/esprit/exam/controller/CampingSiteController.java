package tn.esprit.exam.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
import tn.esprit.exam.entity.CampingSite;
import tn.esprit.exam.repository.CampingSiteRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sites")
@Tag(
    name = "CampingSites",
    description = "Gestion complète des sites de camping - CRUD et fonctionnalités spéciales"
)
@RequiredArgsConstructor
public class CampingSiteController {

    private final CampingSiteRepository campingSiteRepository;

    @GetMapping
    @Operation(
        summary = "Récupérer tous les sites",
        description = "Retourne la liste complète de tous les sites de camping enregistrés"
    )
    @ApiResponse(responseCode = "200", description = "Liste des sites récupérée avec succès")
    @Transactional(readOnly = true)
    public List<CampingSite> getAll() {
        List<CampingSite> sites = campingSiteRepository.findAll();
        // detach owner to prevent lazy loading during serialization
        sites.forEach(s -> { if (s.getOwner() != null) s.getOwner().getRoles(); });
        return sites;
    }

    @GetMapping("/active")
    @Operation(
        summary = "Récupérer les sites actifs",
        description = "Retourne uniquement les sites de camping qui sont actifs et approuvés"
    )
    @ApiResponse(responseCode = "200", description = "Liste des sites actifs récupérée")
    @Transactional(readOnly = true)
    public List<CampingSite> getActive() {
        return campingSiteRepository.findByIsActiveTrue();
    }

    @GetMapping("/search")
    @Operation(
        summary = "Rechercher les sites avec filtres",
        description = "Effectue une recherche avancée des sites selon localisation, catégorie et plage de prix"
    )
    @ApiResponse(responseCode = "200", description = "Sites correspondant aux critères")
    @Transactional(readOnly = true)
    public List<CampingSite> search(
            @RequestParam(required = false)
            @Parameter(description = "Localisation du site (recherche partielle, insensible à la casse)")
            String location,
            
            @RequestParam(required = false)
            @Parameter(description = "Catégorie du site (ex: LUXURY, STANDARD, BUDGET)")
            String category,
            
            @RequestParam(required = false)
            @Parameter(description = "Prix minimum par nuit", example = "50.0")
            Double minPrice,
            
            @RequestParam(required = false)
            @Parameter(description = "Prix maximum par nuit", example = "200.0")
            Double maxPrice) {
        return campingSiteRepository.findAll().stream()
                .filter(s -> location == null || (s.getLocation() != null && s.getLocation().toLowerCase().contains(location.toLowerCase())))
                .filter(s -> category == null || (s.getCategory() != null && s.getCategory().equalsIgnoreCase(category)))
                .filter(s -> minPrice == null || (s.getPricePerNight() != null && s.getPricePerNight().doubleValue() >= minPrice))
                .filter(s -> maxPrice == null || (s.getPricePerNight() != null && s.getPricePerNight().doubleValue() <= maxPrice))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Récupérer un site par ID",
        description = "Retourne les détails complets d'un site de camping spécifique"
    )
    @ApiResponse(responseCode = "200", description = "Site trouvé et retourné")
    @ApiResponse(responseCode = "404", description = "Site non trouvé")
    @Transactional(readOnly = true)
    public CampingSite getById(
            @PathVariable
            @Parameter(description = "ID unique du site", example = "1")
            Long id) {
        return campingSiteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CampingSite not found: " + id));
    }

    @PostMapping
    @Operation(
        summary = "Créer un nouveau site",
        description = "Crée un nouveau site de camping dans le système"
    )
    @ApiResponse(responseCode = "201", description = "Site créé avec succès", content = @Content(schema = @Schema(implementation = CampingSite.class)))
    @ApiResponse(responseCode = "400", description = "Données invalides")
    @Transactional
    public ResponseEntity<CampingSite> create(@RequestBody CampingSite site) {
        site.setId(null);
        site.setReservations(new java.util.HashSet<>());
        CampingSite saved = campingSiteRepository.save(site);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Modifier un site",
        description = "Mets à jour les informations d'un site existing"
    )
    @ApiResponse(responseCode = "200", description = "Site mis à jour")
    @ApiResponse(responseCode = "404", description = "Site non trouvé")
    @Transactional
    public CampingSite update(
            @PathVariable
            @Parameter(description = "ID du site à modifier", example = "1")
            Long id,
            @RequestBody CampingSite site) {
        CampingSite existing = campingSiteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CampingSite not found: " + id));
        site.setId(id);
        site.setReservations(existing.getReservations());
        return campingSiteRepository.save(site);
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Supprimer un site",
        description = "Supprime définitivement un site de camping et toutes ses données associées"
    )
    @ApiResponse(responseCode = "204", description = "Site supprimé avec succès")
    @ApiResponse(responseCode = "404", description = "Site non trouvé")
    @Transactional
    public ResponseEntity<Void> delete(
            @PathVariable
            @Parameter(description = "ID du site à supprimer", example = "1")
            Long id) {
        if (!campingSiteRepository.existsById(id)) throw new IllegalArgumentException("CampingSite not found: " + id);
        campingSiteRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/approve")
    @Operation(
        summary = "Approuver un site",
        description = "Admin approuve et active un site en attente de vérification"
    )
    @ApiResponse(responseCode = "200", description = "Site approuvé et activé")
    @ApiResponse(responseCode = "404", description = "Site non trouvé")
    @Transactional
    public CampingSite approve(
            @PathVariable
            @Parameter(description = "ID du site à approuver", example = "1")
            Long id) {
        CampingSite site = campingSiteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CampingSite not found: " + id));
        site.setIsVerified(true);
        site.setIsActive(true);
        return campingSiteRepository.save(site);
    }

    @PutMapping("/{id}/reject")
    @Operation(
        summary = "Rejeter un site",
        description = "Admin rejette un site et le désactive"
    )
    @ApiResponse(responseCode = "200", description = "Site rejeté et désactivé")
    @ApiResponse(responseCode = "404", description = "Site non trouvé")
    @Transactional
    public CampingSite reject(
            @PathVariable
            @Parameter(description = "ID du site à rejeter", example = "1")
            Long id) {
        CampingSite site = campingSiteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CampingSite not found: " + id));
        site.setIsVerified(false);
        site.setIsActive(false);
        return campingSiteRepository.save(site);
    }
}
