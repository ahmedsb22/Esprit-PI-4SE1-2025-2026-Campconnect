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
import tn.esprit.exam.dto.ReviewDTO;
import tn.esprit.exam.service.IReviewService;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Reviews", description = "Gestion des avis et notations des sites de camping")
@RequiredArgsConstructor
public class ReviewController {

    private final IReviewService reviewService;

    @GetMapping
    @Operation(summary = "Récupérer tous les avis")
    public ResponseEntity<List<ReviewDTO>> getAll() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un avis par ID")
    @ApiResponse(responseCode = "200", description = "Avis trouvé")
    @ApiResponse(responseCode = "404", description = "Avis non trouvé")
    public ResponseEntity<ReviewDTO> getById(
            @PathVariable @Parameter(description = "ID de l'avis") Long id) {
        return ResponseEntity.ok(reviewService.getReviewById(id));
    }

    @GetMapping("/site/{campingSiteId}")
    @Operation(summary = "Récupérer tous les avis d'un site")
    public ResponseEntity<List<ReviewDTO>> getBySite(
            @PathVariable @Parameter(description = "ID du site") Long campingSiteId) {
        return ResponseEntity.ok(reviewService.getReviewsBySite(campingSiteId));
    }

    @GetMapping("/site/{campingSiteId}/rating")
    @Operation(summary = "Récupérer la note moyenne d'un site")
    public ResponseEntity<Double> getAverageRating(
            @PathVariable @Parameter(description = "ID du site") Long campingSiteId) {
        return ResponseEntity.ok(reviewService.getAverageRatingBySite(campingSiteId));
    }

    @GetMapping("/author/{authorId}")
    @Operation(summary = "Récupérer les avis d'un utilisateur")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAMPER')")
    public ResponseEntity<List<ReviewDTO>> getByAuthor(
            @PathVariable @Parameter(description = "ID de l'auteur") Long authorId) {
        return ResponseEntity.ok(reviewService.getReviewsByAuthor(authorId));
    }

    @PostMapping
    @Operation(summary = "Créer un avis — lié à une réservation vérifiée")
    @ApiResponse(responseCode = "201", description = "Avis créé avec succès")
    @ApiResponse(responseCode = "400", description = "Note invalide ou avis déjà existant")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAMPER')")
    public ResponseEntity<ReviewDTO> create(
            @RequestBody ReviewDTO dto,
            @RequestParam Long authorId) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reviewService.createReview(dto, authorId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un avis")
    @ApiResponse(responseCode = "200", description = "Avis modifié")
    @ApiResponse(responseCode = "403", description = "Non autorisé")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAMPER')")
    public ResponseEntity<ReviewDTO> update(
            @PathVariable @Parameter(description = "ID de l'avis") Long id,
            @RequestBody ReviewDTO dto,
            @RequestParam Long authorId) {
        return ResponseEntity.ok(reviewService.updateReview(id, dto, authorId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un avis")
    @ApiResponse(responseCode = "204", description = "Avis supprimé")
    @ApiResponse(responseCode = "403", description = "Non autorisé")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAMPER')")
    public ResponseEntity<Void> delete(
            @PathVariable @Parameter(description = "ID de l'avis") Long id,
            @RequestParam Long authorId) {
        reviewService.deleteReview(id, authorId);
        return ResponseEntity.noContent().build();
    }
}
