package tn.esprit.exam.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.exam.dto.EquipmentDTO;
import tn.esprit.exam.entity.Equipment;
import tn.esprit.exam.entity.User;
import tn.esprit.exam.exception.ResourceNotFoundException;
import tn.esprit.exam.repository.EquipmentRepository;
import tn.esprit.exam.repository.UserRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EquipmentServiceTest {

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EquipmentServiceImpl equipmentService;

    private User provider;
    private Equipment equipment;
    private EquipmentDTO equipmentDTO;

    @BeforeEach
    void setUp() {
        provider = User.builder()
                .id(1L)
                .email("provider@test.tn")
                .firstName("Provider")
                .lastName("Test")
                .build();

        equipment = Equipment.builder()
                .id(1L)
                .name("Test Equipment")
                .description("Description")
                .category("Tools")
                .pricePerDay(new BigDecimal("15.00"))
                .stockQuantity(10)
                .availableQuantity(10)
                .provider(provider)
                .build();

        equipmentDTO = EquipmentDTO.builder()
                .name("Test Equipment")
                .description("Description")
                .category("Tools")
                .pricePerDay(new BigDecimal("15.00"))
                .stockQuantity(10)
                .build();
    }

    @Test
    void createEquipment_shouldReturnSavedDTO() {
        given(userRepository.findById(1L)).willReturn(Optional.of(provider));
        given(equipmentRepository.save(any(Equipment.class))).willReturn(equipment);

        EquipmentDTO saved = equipmentService.createEquipment(equipmentDTO, 1L);

        assertThat(saved).isNotNull();
        assertThat(saved.getName()).isEqualTo("Test Equipment");
        verify(equipmentRepository).save(any(Equipment.class));
    }

    @Test
    void getEquipmentById_shouldReturnDTO_whenExists() {
        given(equipmentRepository.findById(1L)).willReturn(Optional.of(equipment));

        EquipmentDTO found = equipmentService.getEquipmentById(1L);

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(1L);
    }

    @Test
    void getEquipmentById_shouldThrowException_whenNotExists() {
        given(equipmentRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> equipmentService.getEquipmentById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
