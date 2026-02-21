package tn.esprit.exam.service;

import tn.esprit.exam.dto.ReservationDTO;
import tn.esprit.exam.entity.ReservationStatus;

import java.time.LocalDate;
import java.util.List;

public interface IReservationService {
    ReservationDTO createReservation(ReservationDTO dto, Long camperId);
    ReservationDTO updateReservation(Long id, ReservationDTO dto, Long camperId);
    ReservationDTO getReservationById(Long id);
    ReservationDTO getReservationByNumber(String reservationNumber);
    List<ReservationDTO> getAllReservations();
    List<ReservationDTO> getReservationsByCamper(Long camperId);
    List<ReservationDTO> getReservationsBySiteOwner(Long ownerId);
    List<ReservationDTO> getReservationsByStatus(ReservationStatus status);
    ReservationDTO updateReservationStatus(Long id, ReservationStatus status);
    void cancelReservation(Long id, Long camperId);
    boolean isDateRangeAvailable(Long campingSiteId, LocalDate checkIn, LocalDate checkOut);
    ReservationDTO checkIn(Long id);
    ReservationDTO checkOut(Long id);
}
