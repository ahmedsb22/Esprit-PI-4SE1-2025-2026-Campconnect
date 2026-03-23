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
import tn.esprit.exam.dto.ContractDTO;
import tn.esprit.exam.entity.Contract;
import tn.esprit.exam.repository.ContractRepository;
import tn.esprit.exam.repository.ReservationRepository;
import tn.esprit.exam.security.CustomUserDetailsService;
import tn.esprit.exam.security.JwtService;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ContractController.class)
@AutoConfigureMockMvc(addFilters = false)
class ContractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ContractRepository contractRepository;

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
    @DisplayName("GET /api/contracts should return list of contracts")
    void getAll_ok() throws Exception {
        Contract contract = new Contract();
        contract.setId(1L);
        contract.setContractNumber("CNT-001");

        given(contractRepository.findAll()).willReturn(Collections.singletonList(contract));

        mockMvc.perform(get("/api/contracts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].contractNumber").value("CNT-001"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/contracts should create new contract")
    void create_ok() throws Exception {
        ContractDTO dto = new ContractDTO();
        dto.setContractNumber("CNT-NEW");
        dto.setTerms("Terms and conditions");

        Contract saved = new Contract();
        saved.setId(10L);
        saved.setContractNumber("CNT-NEW");

        given(contractRepository.save(any(Contract.class))).willReturn(saved);

        mockMvc.perform(post("/api/contracts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contractNumber").value("CNT-NEW"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/contracts/{id} should return no content")
    void delete_ok() throws Exception {
        given(contractRepository.existsById(1L)).willReturn(true);

        mockMvc.perform(delete("/api/contracts/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
