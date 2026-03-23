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
import tn.esprit.exam.dto.EquipmentDTO;
import tn.esprit.exam.entity.Equipment;
import tn.esprit.exam.repository.EquipmentRepository;
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

@WebMvcTest(controllers = EquipmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class EquipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EquipmentRepository equipmentRepository;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("GET /api/equipment should return list of equipment DTOs")
    void getAll_ok() throws Exception {
        Equipment equipment = new Equipment();
        equipment.setId(1L);
        equipment.setName("Tent");
        equipment.setCategory("Shelter");
        equipment.setPricePerDay(new BigDecimal("15.0"));

        given(equipmentRepository.findAll()).willReturn(Collections.singletonList(equipment));

        mockMvc.perform(get("/api/equipment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Tent"));
    }

    @Test
    @DisplayName("GET /api/equipment/{id} should return equipment DTO")
    void getById_ok() throws Exception {
        Equipment equipment = new Equipment();
        equipment.setId(1L);
        equipment.setName("Sleeping Bag");
        equipment.setPricePerDay(new BigDecimal("5.0"));

        given(equipmentRepository.findById(1L)).willReturn(Optional.of(equipment));

        mockMvc.perform(get("/api/equipment/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sleeping Bag"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/equipment should create new equipment")
    void create_ok() throws Exception {
        Equipment equipment = new Equipment();
        equipment.setName("Gas Stove");
        equipment.setPricePerDay(new BigDecimal("10.0"));

        given(equipmentRepository.save(any(Equipment.class))).willReturn(equipment);

        mockMvc.perform(post("/api/equipment")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(equipment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Gas Stove"));
    }
}
