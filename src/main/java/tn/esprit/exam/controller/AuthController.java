package tn.esprit.exam.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.exam.entity.Role;
import tn.esprit.exam.entity.RoleName;
import tn.esprit.exam.entity.User;
import tn.esprit.exam.exception.BusinessLogicException;
import tn.esprit.exam.repository.RoleRepository;
import tn.esprit.exam.repository.UserRepository;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String password = payload.get("password");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessLogicException("Email ou mot de passe incorrect"));

        if (password != null && !password.equals(user.getPassword())) {
            throw new BusinessLogicException("Email ou mot de passe incorrect");
        }

        return ResponseEntity.ok(buildAuthResponse(user));
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, Object> payload) {
        String email = (String) payload.get("email");
        if (email == null || email.isBlank()) throw new BusinessLogicException("Email is required");
        if (userRepository.existsByEmail(email)) throw new BusinessLogicException("Email already in use");

        User user = new User();
        user.setEmail(email);
        user.setPassword((String) payload.getOrDefault("password", "password123"));
        user.setFirstName((String) payload.getOrDefault("firstName", ""));
        user.setLastName((String) payload.getOrDefault("lastName", ""));
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        // Assign role
        String roleName = payload.containsKey("roles") && payload.get("roles") instanceof List
                ? ((List<?>) payload.get("roles")).isEmpty() ? "CAMPER" : ((List<?>) payload.get("roles")).get(0).toString()
                : "CAMPER";
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
        log.info("User registered: {}", saved.getEmail());
        return ResponseEntity.ok(buildAuthResponse(saved));
    }

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> profile(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null) throw new BusinessLogicException("Not authenticated");
        String cleanToken = token.replace("Bearer ", "").trim();
        try {
            Long userId = Long.parseLong(cleanToken);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessLogicException("User not found"));
            return ResponseEntity.ok(buildAuthResponse(user));
        } catch (NumberFormatException e) {
            User user = userRepository.findByEmail(cleanToken)
                    .orElseThrow(() -> new BusinessLogicException("User not found"));
            return ResponseEntity.ok(buildAuthResponse(user));
        }
    }

    private Map<String, Object> buildAuthResponse(User user) {
        List<String> roles = user.getRoles() != null
                ? user.getRoles().stream()
                    .filter(r -> r.getName() != null)
                    .map(r -> r.getName().name())
                    .collect(Collectors.toList())
                : List.of("CAMPER");

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("email", user.getEmail());
        userMap.put("firstName", user.getFirstName() != null ? user.getFirstName() : "");
        userMap.put("lastName", user.getLastName() != null ? user.getLastName() : "");
        userMap.put("roles", roles);
        userMap.put("role", roles.isEmpty() ? "CAMPER" : roles.get(0));

        Map<String, Object> response = new HashMap<>();
        response.put("token", String.valueOf(user.getId()));
        response.put("user", userMap);
        return response;
    }
}
