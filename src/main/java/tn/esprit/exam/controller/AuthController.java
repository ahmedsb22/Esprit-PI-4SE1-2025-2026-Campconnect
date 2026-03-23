package tn.esprit.exam.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.exam.dto.auth.*;
import tn.esprit.exam.entity.User;
import tn.esprit.exam.service.IAuthService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(
    name = "Authentication",
    description = "Gestion de l'authentification, enregistrement, et profil utilisateur"
)
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/login")
    @Operation(
        summary = "Connexion utilisateur",
        description = "Authentifie un utilisateur avec email et mot de passe, retourne un JWT token"
    )
    @ApiResponse(responseCode = "200", description = "Connexion réussie", content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    @ApiResponse(responseCode = "400", description = "Email ou mot de passe invalide")
    @ApiResponse(responseCode = "500", description = "Erreur serveur")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    @Operation(
        summary = "Enregistrement utilisateur",
        description = "Crée un nouveau compte utilisateur avec email et mot de passe"
    )
    @ApiResponse(responseCode = "200", description = "Enregistrement réussi", content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    @ApiResponse(responseCode = "400", description = "Données invalides ou email déjà existant")
    @ApiResponse(responseCode = "500", description = "Erreur serveur")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        log.info("Register attempt for email: {}", request.getEmail());
        return ResponseEntity.ok(authService.register(request));
    }

    @GetMapping("/profile")
    @Operation(
        summary = "Récupérer le profil courant",
        description = "Retourne les informations du profil de l'utilisateur authentifié"
    )
    @ApiResponse(responseCode = "200", description = "Profil récupéré avec succès", content = @Content(schema = @Schema(implementation = User.class)))
    @ApiResponse(responseCode = "401", description = "Non authentifié")
    public ResponseEntity<User> profile() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }

    @PutMapping("/profile")
    @Operation(
        summary = "Mettre à jour le profil",
        description = "Met à jour les informations du profil de l'utilisateur authentifié"
    )
    @ApiResponse(responseCode = "200", description = "Profil mis à jour", content = @Content(schema = @Schema(implementation = User.class)))
    @ApiResponse(responseCode = "400", description = "Données invalides")
    @ApiResponse(responseCode = "401", description = "Non authentifié")
    public ResponseEntity<User> updateProfile(@RequestBody UpdateProfileRequest request) {
        User currentUser = authService.getCurrentUser();
        return ResponseEntity.ok(authService.updateProfile(currentUser.getId(), request));
    }

    @PostMapping("/forgot-password")
    @Operation(
        summary = "Demande de réinitialisation de mot de passe",
        description = "Envoie un email avec un lien de réinitialisation du mot de passe"
    )
    @ApiResponse(responseCode = "200", description = "Email de réinitialisation envoyé")
    @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> payload) {
        authService.forgotPassword(payload.get("email"));
        return ResponseEntity.ok(Map.of("message", "Email de réinitialisation envoyé"));
    }

    @PostMapping("/reset-password")
    @Operation(
        summary = "Réinitialiser le mot de passe",
        description = "Réinitialise le mot de passe avec le token reçu par email"
    )
    @ApiResponse(responseCode = "200", description = "Mot de passe réinitialisé avec succès")
    @ApiResponse(responseCode = "400", description = "Token invalide ou expiré")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> payload) {
        authService.resetPassword(payload.get("token"), payload.get("password"));
        return ResponseEntity.ok(Map.of("message", "Mot de passe réinitialisé avec succès"));
    }
}
