package tn.esprit.exam.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService - Tests d'intégration des rôles")
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private String testSecretKey;
    private long testExpiration;
    private UserDetails userDetailsWithRoles;

    @BeforeEach
    void setUp() {
        // Initialiser une clé secrète de test (256 bits = 32 octets pour HS256)
        byte[] secret = new byte[32];
        for (int i = 0; i < secret.length; i++) {
            secret[i] = (byte) (i + 48); // Simple seed
        }
        testSecretKey = new String(org.springframework.security.crypto.codec.Hex.encode(secret));
        testExpiration = 3600000; // 1 heure

        // Injecter les propriétés via reflection
        ReflectionTestUtils.setField(jwtService, "secretKey", testSecretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", testExpiration);

        // Créer un UserDetails avec des rôles
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        authorities.add(new SimpleGrantedAuthority("ROLE_OWNER"));
        userDetailsWithRoles = new User("test@example.com", "password", authorities);
    }

    @Test
    @DisplayName("Génère un token JWT avec les rôles inclus")
    void testGenerateTokenIncludesRoles() {
        // Act
        String token = jwtService.generateToken(userDetailsWithRoles);

        // Assert
        assertNotNull(token, "Le token ne doit pas être null");
        assertTrue(token.contains("."), "Le token doit être un JWT valide (3 parties séparées par points)");
        assertEquals(3, token.split("\\.").length, "Un JWT doit avoir 3 parties");
    }

    @Test
    @DisplayName("Extrait correctement les rôles du JWT")
    void testExtractRolesFromToken() {
        // Arrange
        String token = jwtService.generateToken(userDetailsWithRoles);

        // Act
        List<String> roles = jwtService.extractRoles(token);

        // Assert
        assertNotNull(roles, "Les rôles ne doivent pas être null");
        assertEquals(2, roles.size(), "Le token doit contenir 2 rôles");
        assertTrue(roles.contains("ADMIN"), "Les rôles doivent contenir ADMIN");
        assertTrue(roles.contains("OWNER"), "Les rôles doivent contenir OWNER");
    }

    @Test
    @DisplayName("Retourne une liste vide pour un token sans rôles")
    void testExtractRolesFromTokenWithoutRoles() {
        // Arrange
        Collection<SimpleGrantedAuthority> emptyAuthorities = new ArrayList<>();
        UserDetails userWithoutRoles = new User("user@example.com", "password", emptyAuthorities);
        String token = jwtService.generateToken(userWithoutRoles);

        // Act
        List<String> roles = jwtService.extractRoles(token);

        // Assert
        assertNotNull(roles, "Les rôles ne doivent pas être null");
        assertTrue(roles.isEmpty(), "Les rôles doivent être vides");
    }

    @Test
    @DisplayName("Extrait correctement le username du token")
    void testExtractUsername() {
        // Arrange
        String token = jwtService.generateToken(userDetailsWithRoles);

        // Act
        String username = jwtService.extractUsername(token);

        // Assert
        assertEquals("test@example.com", username, "Le username extrait doit correspondre");
    }

    @Test
    @DisplayName("Valide correctement un token valide")
    void testIsTokenValid() {
        // Arrange
        String token = jwtService.generateToken(userDetailsWithRoles);

        // Act
        boolean isValid = jwtService.isTokenValid(token, userDetailsWithRoles);

        // Assert
        assertTrue(isValid, "Le token valide doit être reconnu comme valide");
    }

    @Test
    @DisplayName("Rejette un token avec un username différent")
    void testIsTokenInvalidWithDifferentUsername() {
        // Arrange
        String token = jwtService.generateToken(userDetailsWithRoles);
        UserDetails differentUser = new User("different@example.com", "password", new ArrayList<>());

        // Act
        boolean isValid = jwtService.isTokenValid(token, differentUser);

        // Assert
        assertFalse(isValid, "Un token avec un username différent doit être invalide");
    }

    @Test
    @DisplayName("Gère correctement plusieurs rôles")
    void testMultipleRoles() {
        // Arrange
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        authorities.add(new SimpleGrantedAuthority("ROLE_OWNER"));
        authorities.add(new SimpleGrantedAuthority("ROLE_CAMPER"));
        authorities.add(new SimpleGrantedAuthority("ROLE_PROVIDER"));
        UserDetails userWithMultipleRoles = new User("multi@example.com", "password", authorities);

        // Act
        String token = jwtService.generateToken(userWithMultipleRoles);
        List<String> extractedRoles = jwtService.extractRoles(token);

        // Assert
        assertEquals(4, extractedRoles.size(), "Tous les rôles doivent être extraits");
        assertTrue(extractedRoles.contains("ADMIN"), "Doit contenir ADMIN");
        assertTrue(extractedRoles.contains("OWNER"), "Doit contenir OWNER");
        assertTrue(extractedRoles.contains("CAMPER"), "Doit contenir CAMPER");
        assertTrue(extractedRoles.contains("PROVIDER"), "Doit contenir PROVIDER");
    }

    @Test
    @DisplayName("Préfixe 'ROLE_' est supprimé des rôles extraits")
    void testRolePrefixRemoved() {
        // Arrange
        String token = jwtService.generateToken(userDetailsWithRoles);

        // Act
        List<String> roles = jwtService.extractRoles(token);

        // Assert
        for (String role : roles) {
            assertFalse(role.startsWith("ROLE_"), "Le préfixe ROLE_ ne doit pas être dans les rôles extraits");
        }
    }
}
