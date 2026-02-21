package tn.esprit.exam.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.exam.entity.Role;
import tn.esprit.exam.entity.RoleName;
import tn.esprit.exam.entity.User;
import tn.esprit.exam.exception.BusinessLogicException;
import tn.esprit.exam.exception.ResourceNotFoundException;
import tn.esprit.exam.exception.UnauthorizedException;
import tn.esprit.exam.exception.ValidationException;
import tn.esprit.exam.repository.RoleRepository;
import tn.esprit.exam.repository.UserRepository;
import tn.esprit.exam.util.ValidationUtil;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public User register(User user) {
        if (!ValidationUtil.isValidEmail(user.getEmail())) {
            throw new ValidationException("Email invalide");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new BusinessLogicException("Un utilisateur avec cet email existe déjà");
        }

        // Stocker le mot de passe tel quel (sans encodage pour simplifier)
        // En production, il faudrait encoder le mot de passe

        // rôle par défaut: CAMPER
        Role defaultRole = roleRepository.findByName(RoleName.CAMPER)
                .orElseGet(() -> roleRepository.save(
                        Role.builder().name(RoleName.CAMPER).build()
                ));
        user.getRoles().add(defaultRole);

        return userRepository.save(user);
    }

    @Override
    public String login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Email ou mot de passe incorrect"));

        // Vérification simple du mot de passe (sans encodage)
        if (!rawPassword.equals(user.getPassword())) {
            throw new UnauthorizedException("Email ou mot de passe incorrect");
        }

        // Retourner un token simple (l'ID de l'utilisateur)
        return String.valueOf(user.getId());
    }

    @Override
    public User getCurrentUser(String token) {
        String cleanToken = token.replace("Bearer ", "").trim();
        try {
            Long userId = Long.parseLong(cleanToken);
            return userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        } catch (NumberFormatException e) {
            // Essayer de trouver par email
            return userRepository.findByEmail(cleanToken)
                    .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé pour le token fourni"));
        }
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id: " + id));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public User updateProfile(Long id, User updated) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setPhone(updated.getPhone());
        existing.setAddress(updated.getAddress());
        existing.setProfileImage(updated.getProfileImage());

        return userRepository.save(existing);
    }

    @Override
    @Transactional
    public void changePassword(Long id, String oldPassword, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        // Vérification simple du mot de passe (sans encodage)
        if (!oldPassword.equals(user.getPassword())) {
            throw new UnauthorizedException("Ancien mot de passe incorrect");
        }

        if (newPassword == null || newPassword.length() < 8) {
            throw new ValidationException("Le nouveau mot de passe doit contenir au moins 8 caractères");
        }

        user.setPassword(newPassword);
        userRepository.save(user);
    }

    @Override
    public void logout(String token) {
        // Pas de gestion de session nécessaire
    }
}

