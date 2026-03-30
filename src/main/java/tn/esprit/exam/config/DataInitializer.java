package tn.esprit.exam.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.exam.entity.Role;
import tn.esprit.exam.entity.RoleName;
import tn.esprit.exam.entity.User;
import tn.esprit.exam.repository.RoleRepository;
import tn.esprit.exam.repository.UserRepository;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Vérification et initialisation de l'utilisateur ADMIN...");
        
        // S'assurer que les rôles existent
        for (RoleName roleName : RoleName.values()) {
            if (!roleRepository.findByName(roleName).isPresent()) {
                roleRepository.save(Role.builder().name(roleName).build());
                log.info("Rôle créé : {}", roleName);
            }
        }

        // Créer ou mettre à jour l'admin pour garantir que le mot de passe est 'admin123'
        String adminEmail = "admin@campconnect.tn";
        User admin = userRepository.findByEmail(adminEmail).orElse(null);
        
        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseThrow(() -> new RuntimeException("Rôle ADMIN non trouvé"));

        if (admin == null) {
            admin = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode("admin123"))
                    .firstName("Admin")
                    .lastName("System")
                    .phone("+216 71 000 001")
                    .address("Tunis, Tunisie")
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .roles(new HashSet<>(Set.of(adminRole)))
                    .build();
            userRepository.save(admin);
            log.info("Utilisateur ADMIN créé avec succès : admin@campconnect.tn / admin123");
        } else {
            // Forcer la mise à jour du mot de passe pour être sûr qu'il correspond à admin123
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRoles(new HashSet<>(Set.of(adminRole)));
            userRepository.save(admin);
            log.info("Utilisateur ADMIN mis à jour avec le mot de passe : admin123");
        }
    }
}
