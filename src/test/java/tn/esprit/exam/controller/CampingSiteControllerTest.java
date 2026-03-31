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
import tn.esprit.exam.dto.CampingSiteDTO;
import tn.esprit.exam.security.CustomUserDetailsService;
import tn.esprit.exam.security.JwtService;
import tn.esprit.exam.service.ICampingSiteService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CampingSiteController.class)
@AutoConfigureMockMvc(addFilters = false)
class CampingSiteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ICampingSiteService campingSiteService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("GET /api/sites should return list of sites")
    void getAll_ok() throws Exception {
        CampingSiteDTO dto = CampingSiteDTO.builder()
                .id(1L)
                .name("Camping Paradise")
                .location("Tunis")
                .pricePerNight(new BigDecimal("50.0"))
                .build();

        given(campingSiteService.getAllCampingSites()).willReturn(List.of(dto));

        mockMvc.perform(get("/api/sites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Camping Paradise"));
    }

    @Test
    @DisplayName("GET /api/sites/{id} should return site by id")
    void getById_ok() throws Exception {
        CampingSiteDTO dto = CampingSiteDTO.builder()
                .id(1L)
                .name("Mountain View")
                .build();

        given(campingSiteService.getCampingSiteById(1L)).willReturn(dto);

        mockMvc.perform(get("/api/sites/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mountain View"));
    }

    @Test
    @WithMockUser(roles = "SITE_OWNER")
    @DisplayName("POST /api/sites should create a new site linked to owner")
    void create_ok() throws Exception {
        CampingSiteDTO input = CampingSiteDTO.builder()
                .name("Forest Camp")
                .location("Zaghouan")
                .pricePerNight(new BigDecimal("50.0"))
                .build();

        CampingSiteDTO created = CampingSiteDTO.builder()
                .id(1L)
                .name("Forest Camp")
                .location("Zaghouan")
                .ownerId(1L)
                .ownerName("Ahmed Chaib")
                .pricePerNight(new BigDecimal("50.0"))
                .build();

        given(campingSiteService.createCampingSite(any(CampingSiteDTO.class), eq(1L)))
                .willReturn(created);

        mockMvc.perform(post("/api/sites")
                        .param("ownerId", "1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Forest Camp"))
                .andExpect(jsonPath("$.ownerId").value(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/sites/{id}/approve should verify the site — Admin only")
    void approve_ok() throws Exception {
        CampingSiteDTO approved = CampingSiteDTO.builder()
                .id(1L)
                .name("Desert Camp")
                .isVerified(true)
                .isActive(true)
                .build();

        given(campingSiteService.verifyCampingSite(1L)).willReturn(approved);

        mockMvc.perform(put("/api/sites/1/approve")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isVerified").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/sites/{id}/reject should reject the site — Admin only")
    void reject_ok() throws Exception {
        CampingSiteDTO rejected = CampingSiteDTO.builder()
                .id(1L)
                .name("Desert Camp")
                .isVerified(false)
                .isActive(false)
                .build();

        given(campingSiteService.rejectCampingSite(1L)).willReturn(rejected);

        mockMvc.perform(put("/api/sites/1/reject")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isVerified").value(false));
    }

    @Test
    @WithMockUser(roles = "SITE_OWNER")
    @DisplayName("DELETE /api/sites/{id} should delete site and its reservations")
    void delete_ok() throws Exception {
        doNothing().when(campingSiteService).deleteCampingSite(eq(1L), eq(1L));

        mockMvc.perform(delete("/api/sites/1")
                        .param("ownerId", "1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}