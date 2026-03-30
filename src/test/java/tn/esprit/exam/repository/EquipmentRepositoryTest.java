package tn.esprit.exam.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import tn.esprit.exam.entity.Equipment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EquipmentRepositoryTest {

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByIsActiveTrue_ShouldReturnOnlyActiveEquipment() {
        // Given
        Equipment activeEq = Equipment.builder()
                .name("Active Gear")
                .isActive(true)
                .pricePerDay(BigDecimal.TEN)
                .stockQuantity(10)
                .availableQuantity(10)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Equipment inactiveEq = Equipment.builder()
                .name("Inactive Gear")
                .isActive(false)
                .pricePerDay(BigDecimal.TEN)
                .stockQuantity(10)
                .availableQuantity(10)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        entityManager.persist(activeEq);
        entityManager.persist(inactiveEq);
        entityManager.flush();

        // When
        List<Equipment> activeList = equipmentRepository.findByIsActiveTrue();

        // Then
        assertThat(activeList).hasSize(1);
        assertThat(activeList.get(0).getName()).isEqualTo("Active Gear");
    }
}
