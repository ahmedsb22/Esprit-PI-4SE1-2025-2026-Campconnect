package tn.esprit.exam.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.exam.entity.User;
import tn.esprit.exam.repository.RoleRepository;
import tn.esprit.exam.repository.UserRepository;
import tn.esprit.exam.security.CustomUserDetailsService;
import tn.esprit.exam.security.JwtService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/users should return list of users as maps")
    void getAll_ok() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("admin@test.tn");
        user.setFirstName("Admin");
        user.setLastName("User");

        given(userRepository.findAll()).willReturn(Collections.singletonList(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("admin@test.tn"))
                .andExpect(jsonPath("$[0].firstName").value("Admin"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/users/{id} should return user by id")
    void getById_ok() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@test.tn");

        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@test.tn"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/users should create new user")
    void create_ok() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", "newuser@test.tn");
        payload.put("firstName", "New");
        payload.put("lastName", "User");
        payload.put("role", "CAMPER");

        User saved = new User();
        saved.setId(2L);
        saved.setEmail("newuser@test.tn");
        saved.setFirstName("New");

        given(userRepository.existsByEmail("newuser@test.tn")).willReturn(false);
        given(userRepository.save(any(User.class))).willReturn(saved);

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("newuser@test.tn"));
    }
}
