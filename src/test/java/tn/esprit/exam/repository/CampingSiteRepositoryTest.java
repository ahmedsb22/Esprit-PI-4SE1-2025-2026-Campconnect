package tn.esprit.exam.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import tn.esprit.exam.entity.CampingSite;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CampingSiteRepositoryTest {

    @Autowired
    private CampingSiteRepository campingSiteRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByIsActiveTrue_ShouldReturnOnlyActiveSites() {
        // Given
        CampingSite activeSite = CampingSite.builder()
                .name("Active Site")
                .isActive(true)
                .isVerified(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        CampingSite inactiveSite = CampingSite.builder()
                .name("Inactive Site")
                .isActive(false)
                .isVerified(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        entityManager.persist(activeSite);
        entityManager.persist(inactiveSite);
        entityManager.flush();

        // When
        List<CampingSite> activeSites = campingSiteRepository.findByIsActiveTrue();

        // Then
        assertThat(activeSites).hasSize(1);
        assertThat(activeSites.get(0).getName()).isEqualTo("Active Site");
    }
}
