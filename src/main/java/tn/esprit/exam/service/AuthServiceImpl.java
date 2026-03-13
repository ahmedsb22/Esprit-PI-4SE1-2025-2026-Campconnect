package tn.esprit.exam.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.esprit.exam.dto.auth.*;
import tn.esprit.exam.entity.*;
import tn.esprit.exam.exception.BusinessLogicException;
import tn.esprit.exam.exception.ResourceNotFoundException;
import tn.esprit.exam.exception.UnauthorizedException;
import tn.esprit.exam.repository.PasswordResetTokenRepository;
import tn.esprit.exam.repository.RoleRepository;
import tn.esprit.exam.repository.UserRepository;
import tn.esprit.exam.security.CustomUserDetailsService;
import tn.esprit.exam.security.JwtService;
import tn.esprit.exam.util.ValidationUtil;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements IAuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final PasswordResetTokenRepository tokenRepository;
    private final JavaMailSender mailSender;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (!ValidationUtil.isValidEmail(request.getEmail())) {
            throw new BusinessLogicException("Email invalide");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessLogicException("Un utilisateur avec cet email existe déjà");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .roles(new HashSet<>())
                .build();

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            for (String roleName : request.getRoles()) {
                Role role = roleRepository.findByName(RoleName.valueOf(roleName.toUpperCase()))
                        .orElseThrow(() -> new BusinessLogicException("Rôle non trouvé: " + roleName));
                user.getRoles().add(role);
            }
        } else {
            Role defaultRole = roleRepository.findByName(RoleName.CAMPER)
                    .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.CAMPER).build()));
            user.getRoles().add(defaultRole);
        }

        userRepository.save(user);
        
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);

        return buildAuthResponse(user, token);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);

        return buildAuthResponse(user, token);
    }

    @Override
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
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
    public User updateProfile(Long id, UpdateProfileRequest updated) {
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

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessLogicException("Ancien mot de passe incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public void logout() {
        SecurityContextHolder.clearContext();
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec cet email"));

        tokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(Instant.now().plus(24, ChronoUnit.HOURS))
                .used(false)
                .build();

        tokenRepository.save(resetToken);

        sendResetEmail(user.getEmail(), token);
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessLogicException("Token invalide"));

        if (resetToken.isUsed()) {
            throw new BusinessLogicException("Token déjà utilisé");
        }

        if (resetToken.getExpiryDate().isBefore(Instant.now())) {
            throw new BusinessLogicException("Token expiré");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }

    private void sendResetEmail(String email, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Réinitialisation de votre mot de passe - CampConnect");
        message.setText("Pour réinitialiser votre mot de passe, veuillez cliquer sur le lien suivant : "
                + "http://localhost:4200/reset-password?token=" + token);
        
        try {
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de réinitialisation", e);
            // On ne bloque pas le flow si l'envoi d'email échoue en local
        }
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toList()))
                .build();
    }
}
