package tn.esprit.exam.controller;

import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @GetMapping
    public List<Map<String, Object>> getAll() {
        return userRepository.findAll().stream()
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        return toMap(user);
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, Object> payload) {
        String email = (String) payload.get("email");
        if (email == null || email.isBlank()) throw new BusinessLogicException("Email is required");
        if (userRepository.existsByEmail(email)) throw new BusinessLogicException("Email already exists");

        User user = new User();
        user.setEmail(email);
        user.setPassword((String) payload.getOrDefault("password", "password123"));
        user.setFirstName((String) payload.getOrDefault("firstName", ""));
        user.setLastName((String) payload.getOrDefault("lastName", ""));
        user.setPhone((String) payload.getOrDefault("phone", ""));
        user.setAddress((String) payload.getOrDefault("address", ""));
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        // Assign role
        String roleName = payload.containsKey("roles") && payload.get("roles") instanceof List
                ? ((List<?>) payload.get("roles")).isEmpty() ? "CAMPER" : ((List<?>) payload.get("roles")).get(0).toString()
                : (String) payload.getOrDefault("role", "CAMPER");

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
        return toMap(saved);
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        if (payload.containsKey("firstName")) user.setFirstName((String) payload.get("firstName"));
        if (payload.containsKey("lastName")) user.setLastName((String) payload.get("lastName"));
        if (payload.containsKey("email")) user.setEmail((String) payload.get("email"));
        if (payload.containsKey("phone")) user.setPhone((String) payload.get("phone"));
        if (payload.containsKey("address")) user.setAddress((String) payload.get("address"));
        user.setUpdatedAt(Instant.now());

        return toMap(userRepository.save(user));
    }

    @PutMapping("/{id}/status")
    public Map<String, Object> updateStatus(@PathVariable Long id, @RequestParam boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        user.setUpdatedAt(Instant.now());
        return toMap(userRepository.save(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!userRepository.existsById(id)) throw new IllegalArgumentException("User not found: " + id);
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private Map<String, Object> toMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("firstName", user.getFirstName() != null ? user.getFirstName() : "");
        map.put("lastName", user.getLastName() != null ? user.getLastName() : "");
        map.put("email", user.getEmail() != null ? user.getEmail() : "");
        map.put("phone", user.getPhone() != null ? user.getPhone() : "");
        map.put("address", user.getAddress() != null ? user.getAddress() : "");
        map.put("profileImageUrl", user.getProfileImage() != null ? user.getProfileImage() : "");
        map.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : "");
        map.put("updatedAt", user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : "");
        map.put("active", true);
        List<String> roles = user.getRoles() != null
                ? user.getRoles().stream()
                    .filter(r -> r.getName() != null)
                    .map(r -> r.getName().name())
                    .collect(Collectors.toList())
                : List.of();
        map.put("roles", roles);
        map.put("role", roles.isEmpty() ? "CAMPER" : roles.get(0));
        return map;
    }
}
