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
import tn.esprit.exam.dto.UserDTO;
import tn.esprit.exam.entity.Role;
import tn.esprit.exam.entity.RoleName;
import tn.esprit.exam.entity.User;
import tn.esprit.exam.exception.BusinessLogicException;
import tn.esprit.exam.repository.RoleRepository;
import tn.esprit.exam.repository.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@Tag(
    name = "Users",
    description = "Gestion des utilisateurs - CRUD et gestion des rôles"
)
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @GetMapping
    @Operation(
        summary = "Récupérer tous les utilisateurs",
        description = "Retourne la liste complète de tous les utilisateurs du système"
    )
    @ApiResponse(responseCode = "200", description = "Liste des utilisateurs")
    public List<UserDTO> getAll() {
        return userRepository.findAll().stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Récupérer un utilisateur par ID",
        description = "Retourne les détails complets d'un utilisateur spécifique"
    )
    @ApiResponse(responseCode = "200", description = "Utilisateur trouvé")
    @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    public UserDTO getById(
            @PathVariable
            @Parameter(description = "ID de l'utilisateur", example = "1")
            Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        return UserDTO.fromEntity(user);
    }

    @PostMapping
    @Operation(
        summary = "Créer un nouvel utilisateur",
        description = "Crée un nouvel utilisateur dans le système avec les informations fournies"
    )
    @ApiResponse(responseCode = "201", description = "Utilisateur créé avec succès")
    @ApiResponse(responseCode = "400", description = "Données invalides (email requis ou déjà existant)")
    public UserDTO create(
            @RequestBody
            UserDTO userDTO) {
        String email = userDTO.getEmail();
        if (email == null || email.isBlank()) throw new BusinessLogicException("Email is required");
        if (userRepository.existsByEmail(email)) throw new BusinessLogicException("Email already exists");

        User user = new User();
        user.setEmail(email);
        user.setPassword(userDTO.getPassword() != null ? userDTO.getPassword() : "password123");
        user.setFirstName(userDTO.getFirstName() != null ? userDTO.getFirstName() : "");
        user.setLastName(userDTO.getLastName() != null ? userDTO.getLastName() : "");
        user.setPhone(userDTO.getPhone() != null ? userDTO.getPhone() : "");
        user.setAddress(userDTO.getAddress() != null ? userDTO.getAddress() : "");
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        // Assign role
        String roleName = userDTO.getRole() != null ? userDTO.getRole() : "CAMPER";
        if (userDTO.getRoles() != null && !userDTO.getRoles().isEmpty()) {
            roleName = userDTO.getRoles().get(0);
        }

        try {
            RoleName rn = RoleName.valueOf(roleName.toUpperCase());
            Role role = roleRepository.findByName(rn)
                    .orElseGet(() -> roleRepository.save(Role.builder().name(rn).build()));
            user.getRoles().add(role);
        } catch (Exception e) {
            Role defaultRole = roleRepository.findByName(RoleName.CAMPER)
                    .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.CAMPER).build()));
            user.getRoles().add(defaultRole);
        }

        User saved = userRepository.save(user);
        return UserDTO.fromEntity(saved);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Mettre à jour un utilisateur",
        description = "Met à jour les informations d'un utilisateur existant (profil et rôles)"
    )
    @ApiResponse(responseCode = "200", description = "Utilisateur mis à jour")
    @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    @Transactional
    public UserDTO update(
            @PathVariable
            @Parameter(description = "ID de l'utilisateur à mettre à jour", example = "1")
            Long id,
            @RequestBody
            UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        if (userDTO.getFirstName() != null) user.setFirstName(userDTO.getFirstName());
        if (userDTO.getLastName() != null) user.setLastName(userDTO.getLastName());
        if (userDTO.getEmail() != null) user.setEmail(userDTO.getEmail());
        if (userDTO.getPhone() != null) user.setPhone(userDTO.getPhone());
        if (userDTO.getAddress() != null) user.setAddress(userDTO.getAddress());
        
        // Handle role update
        if (userDTO.getRoles() != null && !userDTO.getRoles().isEmpty()) {
            String roleName = userDTO.getRoles().get(0);
            try {
                RoleName rn = RoleName.valueOf(roleName.toUpperCase());
                Role role = roleRepository.findByName(rn)
                        .orElseGet(() -> roleRepository.save(Role.builder().name(rn).build()));
                user.getRoles().clear();
                user.getRoles().add(role);
            } catch (Exception e) {
                // Ignore invalid role
            }
        } else if (userDTO.getRole() != null) {
            String roleName = userDTO.getRole();
            try {
                RoleName rn = RoleName.valueOf(roleName.toUpperCase());
                Role role = roleRepository.findByName(rn)
                        .orElseGet(() -> roleRepository.save(Role.builder().name(rn).build()));
                user.getRoles().clear();
                user.getRoles().add(role);
            } catch (Exception e) {
                // Ignore invalid role
            }
        }
        
        user.setUpdatedAt(Instant.now());

        return UserDTO.fromEntity(userRepository.save(user));
    }

    @PutMapping("/{id}/status")
    @Operation(
        summary = "Changer le statut d'un utilisateur",
        description = "Active ou désactive un utilisateur"
    )
    @ApiResponse(responseCode = "200", description = "Statut mis à jour")
    @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    public UserDTO updateStatus(
            @PathVariable
            @Parameter(description = "ID de l'utilisateur", example = "1")
            Long id,
            @RequestParam
            @Parameter(description = "Nouvel état d'activation (true/false)", example = "true")
            boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        user.setUpdatedAt(Instant.now());
        return UserDTO.fromEntity(userRepository.save(user));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Supprimer un utilisateur",
        description = "Supprime définitivement un utilisateur du système"
    )
    @ApiResponse(responseCode = "204", description = "Utilisateur supprimé")
    @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    public ResponseEntity<Void> delete(
            @PathVariable
            @Parameter(description = "ID de l'utilisateur à supprimer", example = "1")
            Long id) {
        if (!userRepository.existsById(id))
            throw new IllegalArgumentException("User not found: " + id);
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
