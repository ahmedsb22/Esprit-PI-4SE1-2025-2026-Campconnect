package tn.esprit.exam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.exam.entity.Review;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByCampingSiteId(Long campingSiteId);

    List<Review> findByAuthorId(Long authorId);

    List<Review> findByIsVerifiedTrue();

    boolean existsByReservationId(Long reservationId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.campingSite.id = :siteId")
    Double findAverageRatingBySiteId(Long siteId);
}