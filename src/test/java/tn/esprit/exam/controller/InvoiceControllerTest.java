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
import tn.esprit.exam.entity.Invoice;
import tn.esprit.exam.repository.InvoiceRepository;
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

@WebMvcTest(controllers = InvoiceController.class)
@AutoConfigureMockMvc(addFilters = false)
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InvoiceRepository invoiceRepository;

    @MockBean
    private ReservationRepository reservationRepository;

    @MockBean
    private ReservationEquipmentRepository reservationEquipmentRepository;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/invoices should return list of invoices")
    void getAll_ok() throws Exception {
        Invoice invoice = new Invoice();
        invoice.setId(1L);
        invoice.setInvoiceNumber("INV-1001");
        invoice.setTotalAmount(new BigDecimal("150.0"));

        given(invoiceRepository.findAll()).willReturn(Collections.singletonList(invoice));

        mockMvc.perform(get("/api/invoices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].invoiceNumber").value("INV-1001"))
                .andExpect(jsonPath("$[0].totalAmount").value(150.0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/invoices should create new invoice")
    void create_ok() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("invoiceNumber", "INV-NEW");
        payload.put("totalAmount", 200.0);

        Invoice saved = new Invoice();
        saved.setId(5L);
        saved.setInvoiceNumber("INV-NEW");
        saved.setTotalAmount(new BigDecimal("200.0"));

        given(invoiceRepository.save(any(Invoice.class))).willReturn(saved);

        mockMvc.perform(post("/api/invoices")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invoiceNumber").value("INV-NEW"))
                .andExpect(jsonPath("$.totalAmount").value(200.0));
    }
}
