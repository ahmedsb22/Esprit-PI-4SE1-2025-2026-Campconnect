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
import tn.esprit.exam.entity.Equipment;
import tn.esprit.exam.entity.ReservationEquipment;
import tn.esprit.exam.repository.EquipmentRepository;
import tn.esprit.exam.repository.ReservationEquipmentRepository;
import tn.esprit.exam.repository.ReservationRepository;
import tn.esprit.exam.security.CustomUserDetailsService;
import tn.esprit.exam.security.JwtService;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EquipmentOrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class EquipmentOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReservationEquipmentRepository reservationEquipmentRepository;

    @MockBean
    private EquipmentRepository equipmentRepository;

    @MockBean
    private ReservationRepository reservationRepository;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/equipment-orders should return list of orders")
    void getAll_ok() throws Exception {
        ReservationEquipment re = new ReservationEquipment();
        re.setId(1L);
        re.setQuantity(2);
        re.setSubtotal(new BigDecimal("30.0"));

        given(reservationEquipmentRepository.findAll()).willReturn(Collections.singletonList(re));

        mockMvc.perform(get("/api/equipment-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quantity").value(2))
                .andExpect(jsonPath("$[0].subtotal").value(30.0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/equipment-orders should create new order")
    void create_ok() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("equipmentId", 1);
        payload.put("quantity", 3);

        Equipment equipment = new Equipment();
        equipment.setId(1L);
        equipment.setPricePerDay(new BigDecimal("10.0"));

        ReservationEquipment saved = new ReservationEquipment();
        saved.setId(10L);
        saved.setQuantity(3);
        saved.setSubtotal(new BigDecimal("30.0"));
        saved.setEquipment(equipment);

        given(equipmentRepository.findById(1L)).willReturn(Optional.of(equipment));
        given(reservationEquipmentRepository.save(any(ReservationEquipment.class))).willReturn(saved);

        mockMvc.perform(post("/api/equipment-orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(3))
                .andExpect(jsonPath("$.subtotal").value(30.0));
    }
}
