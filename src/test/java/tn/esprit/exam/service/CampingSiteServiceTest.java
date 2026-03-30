package tn.esprit.exam.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.exam.dto.CampingSiteDTO;
import tn.esprit.exam.entity.CampingSite;
import tn.esprit.exam.entity.User;
import tn.esprit.exam.exception.ResourceNotFoundException;
import tn.esprit.exam.repository.CampingSiteRepository;
import tn.esprit.exam.repository.UserRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CampingSiteServiceTest {

    @Mock
    private CampingSiteRepository campingSiteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CampingSiteServiceImpl campingSiteService;

    private User owner;
    private CampingSite site;
    private CampingSiteDTO siteDTO;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(1L)
                .email("owner@test.tn")
                .firstName("Owner")
                .lastName("Test")
                .build();

        site = CampingSite.builder()
                .id(1L)
                .name("Test Site")
                .description("Description")
                .location("Test Location")
                .pricePerNight(new BigDecimal("50.00"))
                .capacity(4)
                .category("Mountain")
                .owner(owner)
                .build();

        siteDTO = CampingSiteDTO.builder()
                .name("Test Site")
                .description("Description")
                .location("Test Location")
                .pricePerNight(new BigDecimal("50.00"))
                .capacity(4)
                .category("Mountain")
                .build();
    }

    @Test
    void createCampingSite_shouldReturnSavedDTO() {
        given(userRepository.findById(1L)).willReturn(Optional.of(owner));
        given(campingSiteRepository.save(any(CampingSite.class))).willReturn(site);

        CampingSiteDTO saved = campingSiteService.createCampingSite(siteDTO, 1L);

        assertThat(saved).isNotNull();
        assertThat(saved.getName()).isEqualTo("Test Site");
        verify(campingSiteRepository).save(any(CampingSite.class));
    }

    @Test
    void getCampingSiteById_shouldReturnDTO_whenExists() {
        given(campingSiteRepository.findById(1L)).willReturn(Optional.of(site));

        CampingSiteDTO found = campingSiteService.getCampingSiteById(1L);

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(1L);
    }

    @Test
    void getCampingSiteById_shouldThrowException_whenNotExists() {
        given(campingSiteRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> campingSiteService.getCampingSiteById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
