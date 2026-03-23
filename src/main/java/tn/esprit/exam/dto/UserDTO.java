package tn.esprit.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.exam.entity.User;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO pour les utilisateurs (clients et propriétaires)")
public class UserDTO {
    
    @Schema(description = "ID unique de l'utilisateur", example = "1")
    private Long id;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Schema(description = "Email unique de l'utilisateur", example = "user@example.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Schema(description = "Mot de passe (minimum 6 caractères)", example = "password123")
    private String password;

    @Schema(description = "Prénom", example = "Jean")
    private String firstName;

    @Schema(description = "Nom de famille", example = "Dupont")
    private String lastName;

    @Schema(description = "Numéro de téléphone")
    private String phone;

    @Schema(description = "Adresse complète")
    private String address;

    @Schema(description = "URL de la photo de profil")
    private String profileImageUrl;

    @Schema(description = "L'utilisateur est actif")
    private Boolean isActive;

    @Schema(description = "Liste des rôles (CAMPER, OWNER, ADMIN)")
    private List<String> roles;

    @Schema(description = "Rôle principal")
    private String role;

    @Schema(description = "Date de création du compte")
    private Instant createdAt;

    @Schema(description = "Date de dernière mise à jour")
    private Instant updatedAt;

    // ✅ Méthode de conversion Entity → DTO
    public static UserDTO fromEntity(User user) {
        if (user == null) return null;

        List<String> roleNames = user.getRoles() != null
                ? user.getRoles().stream()
                    .filter(r -> r.getName() != null)
                    .map(r -> r.getName().name())
                    .toList()
                : List.of();

        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .profileImageUrl(user.getProfileImage())
                .isActive(true) // Par défaut actif
                .roles(roleNames)
                .role(roleNames.isEmpty() ? "CAMPER" : roleNames.get(0))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
