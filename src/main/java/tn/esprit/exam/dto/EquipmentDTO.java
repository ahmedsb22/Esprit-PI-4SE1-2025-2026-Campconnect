package tn.esprit.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.exam.entity.Equipment; // ← Import de l'entité

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO pour les équipements de camping")
public class EquipmentDTO {
    @Schema(description = "ID unique de l'équipement", example = "1")
    private Long id;

    @NotBlank(message = "Equipment name is required")
    @Size(min = 3, max = 100)
    @Schema(description = "Nom de l'équipement", example = "Sleeping Bag")
    private String name;

    @NotBlank(message = "Description is required")
    @Schema(description = "Description de l'équipement", example = "Sac de couchage 4 saisons")
    private String description;

    @NotBlank(message = "Category is required")
    @Schema(description = "Catégorie de l'équipement", example = "Sleeping Gear")
    private String category;

    @NotNull(message = "Price per day is required")
    @DecimalMin(value = "0.0", inclusive = false)
    @Schema(description = "Prix journalier de location", example = "15.00")
    private BigDecimal pricePerDay;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0)
    @Schema(description = "Quantité totale en stock", example = "50")
    private Integer stockQuantity;

    @Schema(description = "Quantité disponible/à louer", example = "45")
    private Integer availableQuantity;

    @Schema(description = "URL de l'image de l'équipement")
    private String imageUrl;

    @Schema(description = "Spécifications techniques", example = "Température limite: -10°C")
    private String specifications;

    @Schema(description = "L'équipement est actif/disponible")
    private Boolean isActive;

    @Schema(description = "Note moyenne (0-5)", example = "4.7")
    private BigDecimal rating;

    @Schema(description = "Nombre d'avis", example = "25")
    private Integer reviewCount;

    // Champs du provider (sans charger l'objet User complet)
    @Schema(description = "ID du fournisseur", example = "1")
    private Long providerId;

    @Schema(description = "Nom du fournisseur")
    private String providerName;

    // ✅ MÉTHODE DE CONVERSION : Entity → DTO
    public static EquipmentDTO fromEntity(Equipment equipment) {
        if (equipment == null) return null;

        return EquipmentDTO.builder()
                .id(equipment.getId())
                .name(equipment.getName())
                .description(equipment.getDescription())
                .category(equipment.getCategory())
                .pricePerDay(equipment.getPricePerDay())
                .stockQuantity(equipment.getStockQuantity())
                .availableQuantity(equipment.getAvailableQuantity())
                .imageUrl(equipment.getImageUrl())
                .specifications(equipment.getSpecifications())
                .isActive(equipment.getIsActive())
                .rating(equipment.getRating())
                .reviewCount(equipment.getReviewCount())
                // ✅ Accès SÉCURISÉ au provider (vérification null + pas de proxy)
                .providerId(equipment.getProvider() != null ? equipment.getProvider().getId() : null)
                .providerName(equipment.getProvider() != null ? equipment.getProvider().getFullName() : null)
                .build();
    }
}