package tn.esprit.exam.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.exam.dto.ReservationDTO;
import tn.esprit.exam.entity.ReservationStatus;
import tn.esprit.exam.service.IReservationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final IReservationService reservationService;

    @GetMapping
    public List<ReservationDTO> getAll() {
        return reservationService.getAllReservations();
    }

    @GetMapping("/{id}")
    public ReservationDTO getById(@PathVariable Long id) {
        return reservationService.getReservationById(id);
    }

    @PostMapping
    public ReservationDTO create(@RequestBody Map<String, Object> payload) {
        ReservationDTO dto = new ReservationDTO();

        // campingSiteId
        if (payload.containsKey("campingSiteId")) {
            dto.setCampingSiteId(Long.valueOf(payload.get("campingSiteId").toString()));
        } else if (payload.containsKey("campingSite") && payload.get("campingSite") instanceof Map<?,?> m) {
            if (m.get("id") != null) dto.setCampingSiteId(Long.valueOf(m.get("id").toString()));
        }

        // camperId
        Long camperId = 1L; // default admin
        if (payload.containsKey("camperId")) {
            camperId = Long.valueOf(payload.get("camperId").toString());
        } else if (payload.containsKey("camper") && payload.get("camper") instanceof Map<?,?> m) {
            if (m.get("id") != null) camperId = Long.valueOf(m.get("id").toString());
        }

        if (payload.containsKey("checkInDate"))
            dto.setCheckInDate(java.time.LocalDate.parse(payload.get("checkInDate").toString()));
        if (payload.containsKey("checkOutDate"))
            dto.setCheckOutDate(java.time.LocalDate.parse(payload.get("checkOutDate").toString()));
        if (payload.containsKey("numberOfGuests"))
            dto.setNumberOfGuests(Integer.valueOf(payload.get("numberOfGuests").toString()));
        if (payload.containsKey("specialRequests"))
            dto.setSpecialRequests(payload.get("specialRequests").toString());
        if (payload.containsKey("totalPrice"))
            dto.setTotalPrice(new java.math.BigDecimal(payload.get("totalPrice").toString()));

        String statusStr = payload.containsKey("status") ? payload.get("status").toString() : "PENDING";
        try { dto.setStatus(ReservationStatus.valueOf(statusStr)); }
        catch (Exception e) { dto.setStatus(ReservationStatus.PENDING); }

        // Ensure campingSiteId not null
        if (dto.getCampingSiteId() == null) dto.setCampingSiteId(1L);

        return reservationService.createReservation(dto, camperId);
    }

    @PutMapping("/{id}")
    public ReservationDTO update(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        ReservationDTO dto = reservationService.getReservationById(id);

        if (payload.containsKey("checkInDate"))
            dto.setCheckInDate(java.time.LocalDate.parse(payload.get("checkInDate").toString()));
        if (payload.containsKey("checkOutDate"))
            dto.setCheckOutDate(java.time.LocalDate.parse(payload.get("checkOutDate").toString()));
        if (payload.containsKey("numberOfGuests"))
            dto.setNumberOfGuests(Integer.valueOf(payload.get("numberOfGuests").toString()));
        if (payload.containsKey("specialRequests"))
            dto.setSpecialRequests(payload.get("specialRequests").toString());
        if (payload.containsKey("status")) {
            try { dto.setStatus(ReservationStatus.valueOf(payload.get("status").toString())); }
            catch (Exception ignored) {}
        }

        return reservationService.updateReservation(id, dto, dto.getCamperId());
    }

    @PutMapping("/{id}/status")
    public ReservationDTO updateStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            return reservationService.updateReservationStatus(id, ReservationStatus.valueOf(status));
        } catch (Exception e) {
            return reservationService.updateReservationStatus(id, ReservationStatus.PENDING);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ReservationDTO dto = reservationService.getReservationById(id);
        reservationService.cancelReservation(id, dto.getCamperId());
        return ResponseEntity.noContent().build();
    }
}
