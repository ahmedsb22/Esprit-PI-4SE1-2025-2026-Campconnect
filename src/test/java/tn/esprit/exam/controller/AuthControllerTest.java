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
import tn.esprit.exam.dto.auth.AuthResponse;
import tn.esprit.exam.dto.auth.LoginRequest;
import tn.esprit.exam.dto.auth.RegisterRequest;
import tn.esprit.exam.entity.User;
import tn.esprit.exam.security.CustomUserDetailsService;
import tn.esprit.exam.security.JwtService;
import tn.esprit.exam.service.IAuthService;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IAuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("POST /api/auth/login should return auth response")
    void login_ok() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.tn");
        request.setPassword("password");

        AuthResponse response = AuthResponse.builder()
                .token("jwt-token")
                .email("test@test.tn")
                .build();

        given(authService.login(any(LoginRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.email").value("test@test.tn"));
    }

    @Test
    @DisplayName("POST /api/auth/register should return auth response")
    void register_ok() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@test.tn");
        request.setPassword("password");
        request.setFirstName("First");
        request.setLastName("Last");

        AuthResponse response = AuthResponse.builder()
                .token("new-jwt-token")
                .email("new@test.tn")
                .build();

        given(authService.register(any(RegisterRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-jwt-token"))
                .andExpect(jsonPath("$.email").value("new@test.tn"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/auth/profile should return current user")
    void profile_ok() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("current@test.tn");

        given(authService.getCurrentUser()).willReturn(user);

        mockMvc.perform(get("/api/auth/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("current@test.tn"));
    }

    @Test
    @DisplayName("POST /api/auth/forgot-password should return success message")
    void forgotPassword_ok() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"test@test.tn\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email de réinitialisation envoyé"));
    }
}
