package tn.esprit.exam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.exam.entity.Equipment;
import tn.esprit.exam.entity.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    List<Equipment> findByIsActiveTrue();

    List<Equipment> findByCategory(String category);

    List<Equipment> findByCategoryAndIsActiveTrue(String category);

    List<Equipment> findByProvider(User provider);

    List<Equipment> findByNameContainingIgnoreCase(String name);

    @Query("SELECT e FROM Equipment e WHERE e.isActive = true AND e.availableQuantity > 0")
    List<Equipment> findAvailableEquipment();

    @Query("SELECT e FROM Equipment e WHERE e.isActive = true AND e.category = :category " +
            "AND e.availableQuantity > 0")
    List<Equipment> findAvailableByCategory(@Param("category") String category);

    @Query("SELECT e FROM Equipment e WHERE e.isActive = true " +
            "AND e.pricePerDay BETWEEN :minPrice AND :maxPrice")
    List<Equipment> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
                                     @Param("maxPrice") BigDecimal maxPrice);

    List<Equipment> findByPricePerDayBetween(BigDecimal minPrice, BigDecimal maxPrice);

    List<Equipment> findByAvailableQuantityGreaterThan(Integer quantity);

    @Query("SELECT e FROM Equipment e WHERE e.isActive = true " +
            "ORDER BY e.rating DESC, e.reviewCount DESC")
    List<Equipment> findTopRatedEquipment();

    Optional<Equipment> findByIdAndIsActiveTrue(Long id);

    long countByProvider(User provider);

    long countByIsActiveTrue();
}
