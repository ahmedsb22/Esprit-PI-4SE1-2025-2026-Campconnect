package tn.esprit.exam.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import tn.esprit.exam.dto.ReservationDTO;
import tn.esprit.exam.entity.ReservationStatus;
import tn.esprit.exam.security.CustomUserDetailsService;
import tn.esprit.exam.security.JwtService;
import tn.esprit.exam.service.IReservationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IReservationService reservationService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    @WithMockUser
    @DisplayName("GET /api/bookings should return list of reservations")
    void getAllBookings_ok() throws Exception {
        ReservationDTO dto = ReservationDTO.builder()
                .id(1L)
                .reservationNumber("RES-1")
                .checkInDate(LocalDate.now().plusDays(1))
                .checkOutDate(LocalDate.now().plusDays(2))
                .numberOfGuests(2)
                .totalPrice(BigDecimal.TEN)
                .status(ReservationStatus.PENDING)
                .build();

        given(reservationService.getAllReservations()).willReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].reservationNumber").value("RES-1"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/bookings should validate required fields and return 400 on missing campingSiteId")
    void createBooking_missingCampingSiteId_returnsBadRequest() throws Exception {
        String payload = "{ \"checkInDate\": \"2030-01-10\", \"checkOutDate\": \"2030-01-12\", \"numberOfGuests\": 2 }";

        mockMvc.perform(
                        post("/api/bookings")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("campingSiteId"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/bookings with minimal valid payload should create reservation")
    void createBooking_validPayload_ok() throws Exception {
        ReservationDTO created = ReservationDTO.builder()
                .id(42L)
                .reservationNumber("RES-42")
                .checkInDate(LocalDate.of(2030, 1, 10))
                .checkOutDate(LocalDate.of(2030, 1, 12))
                .numberOfGuests(2)
                .totalPrice(BigDecimal.TEN)
                .status(ReservationStatus.PENDING)
                .build();

        given(reservationService.createReservation(any(ReservationDTO.class), eq(1L)))
                .willReturn(created);

        String payload = "{ \"campingSiteId\": 5, \"camperId\": 1, " +
                "\"checkInDate\": \"2030-01-10\", \"checkOutDate\": \"2030-01-12\", \"numberOfGuests\": 2 }";

        mockMvc.perform(
                        post("/api/bookings")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(42L))
                .andExpect(jsonPath("$.reservationNumber").value("RES-42"));
    }

}

