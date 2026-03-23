package tn.esprit.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.exam.entity.CampingSite; // ← Import de l'entité CORRECTE

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO pour les sites de camping")
public class CampingSiteDTO {
    @Schema(description = "ID unique du site", example = "1")
    private Long id;

    @NotBlank(message = "Site name is required")
    @Schema(description = "Nom du site de camping", example = "Beach Paradise Camp")
    private String name;

    @Schema(description = "Description détaillée du site", example = "Un magnifique site en bord de mer avec toutes les commodités")
    private String description;

    @Schema(description = "Localité/ville du site", example = "Sousse")
    private String location;

    @Schema(description = "Adresse complète", example = "Boulevard de la Côte, 4000 Sousse")
    private String address;

    @NotNull(message = "Price per night is required")
    @DecimalMin(value = "0.0", inclusive = false)
    @Schema(description = "Prix par nuit", example = "50.00")
    private BigDecimal pricePerNight;

    @NotNull(message = "Capacity is required")
    @Min(value = 1)
    @Schema(description = "Capacité d'accueil (nombre de personnes)", example = "20")
    private Integer capacity;

    @NotBlank(message = "Category is required")
    @Schema(description = "Catégorie du site", example = "Beachside")
    private String category;

    // ✅ Trim pour éviter les espaces dans les URLs (problème d'image Firefox)
    @Schema(description = "URL de l'image du site")
    private String imageUrl;

    @Schema(description = "Dispose du WiFi")
    private Boolean hasWifi;

    @Schema(description = "Dispose de parking")
    private Boolean hasParking;

    @Schema(description = "Dispose de toilettes")
    private Boolean hasRestrooms;

    @Schema(description = "Dispose de douches")
    private Boolean hasShowers;

    @Schema(description = "Dispose de l'électricité")
    private Boolean hasElectricity;

    @Schema(description = "Accepte les animaux domestiques")
    private Boolean hasPetFriendly;

    @Schema(description = "Site actif/disponible")
    private Boolean isActive;

    @Schema(description = "Site vérifié par l'admin")
    private Boolean isVerified;

    @DecimalMin(value = "0.0")
    @Schema(description = "Note moyenne (0-5)", example = "4.5")
    private BigDecimal rating;

    @Min(value = 0)
    @Schema(description = "Nombre d'avis", example = "12")
    private Integer reviewCount;

    // ✅ CHAMPS DU PROPRIÉTAIRE (sans charger l'objet User complet)
    @Schema(description = "ID du propriétaire du site", example = "1")
    private Long ownerId;

    @Schema(description = "Nom du propriétaire")
    private String ownerName;

    @Schema(description = "Date de création")
    private LocalDateTime createdAt;

    @Schema(description = "Date de dernière mise à jour")
    private LocalDateTime updatedAt;

    // ✅ MÉTHODE DE CONVERSION : CampingSite Entity → DTO
    public static CampingSiteDTO fromEntity(CampingSite campingSite) {
        if (campingSite == null) return null;

        return CampingSiteDTO.builder()
                .id(campingSite.getId())
                .name(campingSite.getName())
                .description(campingSite.getDescription())
                .location(campingSite.getLocation())
                .address(campingSite.getAddress())
                .pricePerNight(campingSite.getPricePerNight())
                .capacity(campingSite.getCapacity())
                .category(campingSite.getCategory())
                // ✅ Trim de l'URL pour éviter les erreurs de chargement d'image
                .imageUrl(campingSite.getImageUrl() != null ? campingSite.getImageUrl().trim() : null)
                .hasWifi(campingSite.getHasWifi())
                .hasParking(campingSite.getHasParking())
                .hasRestrooms(campingSite.getHasRestrooms())
                .hasShowers(campingSite.getHasShowers())
                .hasElectricity(campingSite.getHasElectricity())
                .hasPetFriendly(campingSite.getHasPetFriendly())
                .isActive(campingSite.getIsActive())
                .isVerified(campingSite.getIsVerified())
                .rating(campingSite.getRating())
                .reviewCount(campingSite.getReviewCount())
                // ✅ Accès SÉCURISÉ à owner (évite LazyInitializationException)
                .ownerId(campingSite.getOwner() != null ? campingSite.getOwner().getId() : null)
                // ⚠️ Remplacez getFullName() par la méthode réelle de votre entité User
                .ownerName(campingSite.getOwner() != null ? campingSite.getOwner().getFullName() : null)
                .createdAt(campingSite.getCreatedAt())
                .updatedAt(campingSite.getUpdatedAt())
                .build();
    }
}