package tn.esprit.exam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.exam.entity.Equipment;
import tn.esprit.exam.entity.Reservation;
import tn.esprit.exam.entity.ReservationEquipment;

import java.util.List;

@Repository
public interface ReservationEquipmentRepository extends JpaRepository<ReservationEquipment, Long> {

    List<ReservationEquipment> findByReservation(Reservation reservation);

    List<ReservationEquipment> findByEquipment(Equipment equipment);

    void deleteByReservation(Reservation reservation);
}
