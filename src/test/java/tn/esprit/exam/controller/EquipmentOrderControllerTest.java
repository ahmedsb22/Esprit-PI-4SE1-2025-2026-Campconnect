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
import tn.esprit.exam.dto.EquipmentOrderDTO;
import tn.esprit.exam.security.CustomUserDetailsService;
import tn.esprit.exam.security.JwtService;
import tn.esprit.exam.service.IEquipmentOrderService;

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

@WebMvcTest(controllers = EquipmentOrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class EquipmentOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IEquipmentOrderService equipmentOrderService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("GET /api/equipment-orders should return list of orders")
    void getAll_ok() throws Exception {
        EquipmentOrderDTO dto = new EquipmentOrderDTO();
        dto.setId(1L);
        dto.setEquipmentId(1L);
        dto.setQuantity(2);
        dto.setPricePerDay(new BigDecimal("15.0"));
        dto.setSubtotal(new BigDecimal("30.0"));

        given(equipmentOrderService.getAllOrders()).willReturn(List.of(dto));

        mockMvc.perform(get("/api/equipment-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].quantity").value(2));
    }

    @Test
    @DisplayName("GET /api/equipment-orders/{id} should return order by id")
    void getById_ok() throws Exception {
        EquipmentOrderDTO dto = new EquipmentOrderDTO();
        dto.setId(1L);
        dto.setEquipmentId(2L);
        dto.setQuantity(1);
        dto.setPricePerDay(new BigDecimal("10.0"));
        dto.setSubtotal(new BigDecimal("10.0"));

        given(equipmentOrderService.getOrderById(1L)).willReturn(dto);

        mockMvc.perform(get("/api/equipment-orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.quantity").value(1));
    }

    @Test
    @DisplayName("GET /api/equipment-orders/reservation/{id} should return orders by reservation")
    void getByReservation_ok() throws Exception {
        EquipmentOrderDTO dto = new EquipmentOrderDTO();
        dto.setId(1L);
        dto.setReservationId(5L);
        dto.setQuantity(3);
        dto.setSubtotal(new BigDecimal("45.0"));

        given(equipmentOrderService.getOrdersByReservation(5L)).willReturn(List.of(dto));

        mockMvc.perform(get("/api/equipment-orders/reservation/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reservationId").value(5L))
                .andExpect(jsonPath("$[0].quantity").value(3));
    }

    @Test
    @WithMockUser(roles = "CAMPER")
    @DisplayName("POST /api/equipment-orders should create order with auto price calculation")
    void create_ok() throws Exception {
        EquipmentOrderDTO input = new EquipmentOrderDTO();
        input.setEquipmentId(1L);
        input.setQuantity(2);
        input.setReservationId(1L);

        EquipmentOrderDTO created = new EquipmentOrderDTO();
        created.setId(1L);
        created.setEquipmentId(1L);
        created.setQuantity(2);
        created.setPricePerDay(new BigDecimal("15.0"));
        created.setSubtotal(new BigDecimal("30.0"));
        created.setReservationId(1L);

        given(equipmentOrderService.createOrder(any(EquipmentOrderDTO.class), eq(1L)))
                .willReturn(created);

        mockMvc.perform(post("/api/equipment-orders")
                        .param("userId", "1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.subtotal").value(30.0))
                .andExpect(jsonPath("$.quantity").value(2));
    }

    @Test
    @WithMockUser(roles = "CAMPER")
    @DisplayName("PUT /api/equipment-orders/{id} should update order quantity")
    void update_ok() throws Exception {
        EquipmentOrderDTO input = new EquipmentOrderDTO();
        input.setQuantity(3);

        EquipmentOrderDTO updated = new EquipmentOrderDTO();
        updated.setId(1L);
        updated.setQuantity(3);
        updated.setPricePerDay(new BigDecimal("15.0"));
        updated.setSubtotal(new BigDecimal("45.0"));

        given(equipmentOrderService.updateOrder(eq(1L), any(EquipmentOrderDTO.class)))
                .willReturn(updated);

        mockMvc.perform(put("/api/equipment-orders/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(3))
                .andExpect(jsonPath("$.subtotal").value(45.0));
    }

    @Test
    @WithMockUser(roles = "CAMPER")
    @DisplayName("DELETE /api/equipment-orders/{id} should cancel order and restore quantity")
    void cancel_ok() throws Exception {
        doNothing().when(equipmentOrderService).cancelOrder(1L);

        mockMvc.perform(delete("/api/equipment-orders/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}