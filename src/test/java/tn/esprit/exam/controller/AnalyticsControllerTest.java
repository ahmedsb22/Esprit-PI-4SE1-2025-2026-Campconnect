package tn.esprit.exam.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.exam.repository.*;
import tn.esprit.exam.security.CustomUserDetailsService;
import tn.esprit.exam.security.JwtService;

import java.util.Collections;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AnalyticsController.class)
@AutoConfigureMockMvc(addFilters = false)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CampingSiteRepository campingSiteRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ReservationRepository reservationRepository;

    @MockBean
    private ReservationEquipmentRepository reservationEquipmentRepository;

    @MockBean
    private InvoiceRepository invoiceRepository;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/analytics/dashboard should return dashboard statistics")
    void getDashboard_ok() throws Exception {
        given(campingSiteRepository.count()).willReturn(10L);
        given(userRepository.count()).willReturn(50L);
        given(reservationRepository.count()).willReturn(20L);
        given(reservationEquipmentRepository.count()).willReturn(5L);
        given(invoiceRepository.findAll()).willReturn(Collections.emptyList());
        given(reservationRepository.findAll()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/analytics/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSites").value(10))
                .andExpect(jsonPath("$.totalUsers").value(50))
                .andExpect(jsonPath("$.totalBookings").value(20));
    }
}
