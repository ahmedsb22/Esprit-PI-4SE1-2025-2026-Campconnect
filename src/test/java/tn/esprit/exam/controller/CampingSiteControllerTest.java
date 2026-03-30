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
import tn.esprit.exam.entity.CampingSite;
import tn.esprit.exam.repository.CampingSiteRepository;
import tn.esprit.exam.security.CustomUserDetailsService;
import tn.esprit.exam.security.JwtService;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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
    private CampingSiteRepository campingSiteRepository;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("GET /api/sites should return list of sites")
    void getAll_ok() throws Exception {
        CampingSite site = new CampingSite();
        site.setId(1L);
        site.setName("Camping Paradise");

        given(campingSiteRepository.findAll()).willReturn(Collections.singletonList(site));

        mockMvc.perform(get("/api/sites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Camping Paradise"));
    }

    @Test
    @DisplayName("GET /api/sites/{id} should return site by id")
    void getById_ok() throws Exception {
        CampingSite site = new CampingSite();
        site.setId(1L);
        site.setName("Mountain View");

        given(campingSiteRepository.findById(1L)).willReturn(Optional.of(site));

        mockMvc.perform(get("/api/sites/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mountain View"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/sites should create a new site")
    void create_ok() throws Exception {
        CampingSite site = new CampingSite();
        site.setName("Forest Camp");
        site.setLocation("Zaghouan");
        site.setPricePerNight(new BigDecimal("50.0"));

        given(campingSiteRepository.save(any(CampingSite.class))).willReturn(site);

        mockMvc.perform(post("/api/sites")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(site)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Forest Camp"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/sites/{id}/approve should verify the site")
    void approve_ok() throws Exception {
        CampingSite site = new CampingSite();
        site.setId(1L);
        site.setIsVerified(false);

        given(campingSiteRepository.findById(1L)).willReturn(Optional.of(site));
        given(campingSiteRepository.save(any(CampingSite.class))).willAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/api/sites/1/approve")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isVerified").value(true));
    }
}
