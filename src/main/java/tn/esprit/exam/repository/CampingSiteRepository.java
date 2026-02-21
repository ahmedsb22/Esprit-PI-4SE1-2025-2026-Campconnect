package tn.esprit.exam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.exam.entity.CampingSite;
import tn.esprit.exam.entity.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CampingSiteRepository extends JpaRepository<CampingSite, Long> {

    List<CampingSite> findByIsActiveTrue();

    List<CampingSite> findByIsActiveTrueAndIsVerifiedTrue();

    List<CampingSite> findByOwner(User owner);

    List<CampingSite> findByCategory(String category);

    List<CampingSite> findByCategoryAndIsActive(String category, Boolean isActive);

    List<CampingSite> findByLocationContainingIgnoreCase(String location);

    List<CampingSite> findByNameContainingIgnoreCase(String name);

    @Query("SELECT c FROM CampingSite c WHERE c.isActive = true AND c.isVerified = true " +
            "AND c.pricePerNight BETWEEN :minPrice AND :maxPrice")
    List<CampingSite> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
                                       @Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT c FROM CampingSite c WHERE c.isActive = true AND c.isVerified = true " +
            "AND c.capacity >= :minCapacity")
    List<CampingSite> findByMinimumCapacity(@Param("minCapacity") Integer minCapacity);

    @Query("SELECT c FROM CampingSite c WHERE c.isActive = true AND c.isVerified = true " +
            "ORDER BY c.rating DESC, c.reviewCount DESC")
    List<CampingSite> findTopRatedSites();

    Optional<CampingSite> findByIdAndIsActiveTrue(Long id);

    long countByOwner(User owner);

    long countByIsVerifiedTrue();
}
