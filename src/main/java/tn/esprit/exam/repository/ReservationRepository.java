package tn.esprit.exam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.exam.entity.CampingSite;
import tn.esprit.exam.entity.Reservation;
import tn.esprit.exam.entity.ReservationStatus;
import tn.esprit.exam.entity.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByCamper(User camper);

    List<Reservation> findByCampingSite(CampingSite campingSite);

    List<Reservation> findByStatus(ReservationStatus status);

    List<Reservation> findByCamperAndStatus(User camper, ReservationStatus status);

    Optional<Reservation> findByReservationNumber(String reservationNumber);

    @Query("SELECT r FROM Reservation r WHERE r.campingSite = :site " +
           "AND r.status IN ('CONFIRMED', 'CHECKED_IN') " +
           "AND ((r.checkInDate <= :checkOut AND r.checkOutDate >= :checkIn))")
    List<Reservation> findConflictingReservations(@Param("site") CampingSite site,
                                                   @Param("checkIn") LocalDate checkIn,
                                                   @Param("checkOut") LocalDate checkOut);

    @Query("SELECT r FROM Reservation r WHERE r.camper = :camper " +
           "ORDER BY r.createdAt DESC")
    List<Reservation> findByCamperOrderByCreatedAtDesc(@Param("camper") User camper);

    @Query("SELECT r FROM Reservation r WHERE r.campingSite.owner = :owner " +
           "ORDER BY r.createdAt DESC")
    List<Reservation> findBySiteOwnerOrderByCreatedAtDesc(@Param("owner") User owner);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.camper = :camper " +
           "AND r.status = :status")
    long countByCamperAndStatus(@Param("camper") User camper, 
                                @Param("status") ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE r.checkInDate = :date " +
           "AND r.status = 'CONFIRMED'")
    List<Reservation> findUpcomingCheckIns(@Param("date") LocalDate date);

    @Query("SELECT r FROM Reservation r WHERE r.checkOutDate = :date " +
           "AND r.status = 'CHECKED_IN'")
    List<Reservation> findUpcomingCheckOuts(@Param("date") LocalDate date);
}
