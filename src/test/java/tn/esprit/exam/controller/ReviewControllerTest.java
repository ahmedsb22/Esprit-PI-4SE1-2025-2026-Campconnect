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
import tn.esprit.exam.dto.ReviewDTO;
import tn.esprit.exam.security.CustomUserDetailsService;
import tn.esprit.exam.security.JwtService;
import tn.esprit.exam.service.IReviewService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IReviewService reviewService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("GET /api/reviews should return list of reviews")
    void getAll_ok() throws Exception {
        ReviewDTO dto = ReviewDTO.builder()
                .id(1L)
                .rating(5)
                .comment("Excellent site !")
                .authorId(1L)
                .authorName("Ahmed Chaib")
                .campingSiteId(1L)
                .campingSiteName("Desert Oasis")
                .isVerified(true)
                .createdAt(LocalDateTime.now())
                .build();

        given(reviewService.getAllReviews()).willReturn(List.of(dto));

        mockMvc.perform(get("/api/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].rating").value(5))
                .andExpect(jsonPath("$[0].comment").value("Excellent site !"));
    }

    @Test
    @DisplayName("GET /api/reviews/{id} should return review by id")
    void getById_ok() throws Exception {
        ReviewDTO dto = ReviewDTO.builder()
                .id(1L)
                .rating(4)
                .comment("Très bien")
                .authorName("Ahmed Chaib")
                .campingSiteName("Forest Camp")
                .build();

        given(reviewService.getReviewById(1L)).willReturn(dto);

        mockMvc.perform(get("/api/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.comment").value("Très bien"));
    }

    @Test
    @DisplayName("GET /api/reviews/site/{id} should return reviews for a site")
    void getBySite_ok() throws Exception {
        ReviewDTO dto = ReviewDTO.builder()
                .id(1L)
                .rating(5)
                .comment("Magnifique !")
                .campingSiteId(1L)
                .campingSiteName("Desert Oasis")
                .build();

        given(reviewService.getReviewsBySite(1L)).willReturn(List.of(dto));

        mockMvc.perform(get("/api/reviews/site/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].campingSiteId").value(1L))
                .andExpect(jsonPath("$[0].rating").value(5));
    }

    @Test
    @DisplayName("GET /api/reviews/site/{id}/rating should return average rating")
    void getAverageRating_ok() throws Exception {
        given(reviewService.getAverageRatingBySite(1L)).willReturn(4.5);

        mockMvc.perform(get("/api/reviews/site/1/rating"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(4.5));
    }

    @Test
    @WithMockUser(roles = "CAMPER")
    @DisplayName("POST /api/reviews should create review linked to reservation")
    void create_ok() throws Exception {
        ReviewDTO input = ReviewDTO.builder()
                .rating(5)
                .comment("Excellent site, très bien équipé !")
                .campingSiteId(1L)
                .reservationId(1L)
                .build();

        ReviewDTO created = ReviewDTO.builder()
                .id(1L)
                .rating(5)
                .comment("Excellent site, très bien équipé !")
                .authorId(1L)
                .authorName("Ahmed Chaib")
                .campingSiteId(1L)
                .campingSiteName("Desert Oasis")
                .isVerified(true)
                .createdAt(LocalDateTime.now())
                .build();

        given(reviewService.createReview(any(ReviewDTO.class), eq(1L)))
                .willReturn(created);

        mockMvc.perform(post("/api/reviews")
                        .param("authorId", "1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.isVerified").value(true));
    }

    @Test
    @WithMockUser(roles = "CAMPER")
    @DisplayName("PUT /api/reviews/{id} should update review by author")
    void update_ok() throws Exception {
        ReviewDTO input = ReviewDTO.builder()
                .rating(4)
                .comment("Bien mais peut mieux faire")
                .build();

        ReviewDTO updated = ReviewDTO.builder()
                .id(1L)
                .rating(4)
                .comment("Bien mais peut mieux faire")
                .authorId(1L)
                .authorName("Ahmed Chaib")
                .build();

        given(reviewService.updateReview(eq(1L), any(ReviewDTO.class), eq(1L)))
                .willReturn(updated);

        mockMvc.perform(put("/api/reviews/1")
                        .param("authorId", "1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.comment").value("Bien mais peut mieux faire"));
    }

    @Test
    @WithMockUser(roles = "CAMPER")
    @DisplayName("DELETE /api/reviews/{id} should delete review by author")
    void delete_ok() throws Exception {
        doNothing().when(reviewService).deleteReview(1L, 1L);

        mockMvc.perform(delete("/api/reviews/1")
                        .param("authorId", "1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
