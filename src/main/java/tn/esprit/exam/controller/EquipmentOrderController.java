package tn.esprit.exam.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tn.esprit.exam.entity.Equipment;
import tn.esprit.exam.entity.Reservation;
import tn.esprit.exam.entity.ReservationEquipment;
import tn.esprit.exam.exception.ResourceNotFoundException;
import tn.esprit.exam.repository.EquipmentRepository;
import tn.esprit.exam.repository.ReservationEquipmentRepository;
import tn.esprit.exam.repository.ReservationRepository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/equipment-orders")
@RequiredArgsConstructor
public class EquipmentOrderController {

    private final ReservationEquipmentRepository reservationEquipmentRepository;
    private final EquipmentRepository equipmentRepository;
    private final ReservationRepository reservationRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAll() {
        return reservationEquipmentRepository.findAll().stream()
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public Map<String, Object> getById(@PathVariable Long id) {
        return toMap(reservationEquipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id)));
    }

    @PostMapping
    @Transactional
    public Map<String, Object> create(@RequestBody Map<String, Object> payload) {
        ReservationEquipment re = new ReservationEquipment();
        Equipment equipment = null;

        if (payload.containsKey("equipmentId")) {
            Long eqId = Long.valueOf(payload.get("equipmentId").toString());
            equipment = equipmentRepository.findById(eqId)
                    .orElseThrow(() -> new ResourceNotFoundException("Equipment not found: " + eqId));
            re.setEquipment(equipment);
        } else if (payload.containsKey("equipment") && payload.get("equipment") instanceof Map) {
            Object eqId = ((Map<?,?>) payload.get("equipment")).get("id");
            if (eqId != null) {
                equipment = equipmentRepository.findById(Long.valueOf(eqId.toString()))
                        .orElseThrow(() -> new ResourceNotFoundException("Equipment not found: " + eqId));
                re.setEquipment(equipment);
            }
        }

        if (payload.containsKey("reservationId")) {
            Long resId = Long.valueOf(payload.get("reservationId").toString());
            reservationRepository.findById(resId).ifPresent(re::setReservation);
        } else if (payload.containsKey("reservation") && payload.get("reservation") instanceof Map) {
            Object resId = ((Map<?,?>) payload.get("reservation")).get("id");
            if (resId != null) reservationRepository.findById(Long.valueOf(resId.toString())).ifPresent(re::setReservation);
        }

        int qty = payload.containsKey("quantity") ? Integer.parseInt(payload.get("quantity").toString()) : 1;
        re.setQuantity(qty);

        BigDecimal ppd = equipment != null && equipment.getPricePerDay() != null
                ? equipment.getPricePerDay()
                : (payload.containsKey("pricePerDay") ? new BigDecimal(payload.get("pricePerDay").toString()) : BigDecimal.ZERO);
        re.setPricePerDay(ppd);
        re.setSubtotal(ppd.multiply(BigDecimal.valueOf(qty)));

        return toMap(reservationEquipmentRepository.save(re));
    }

    @PutMapping("/{id}")
    @Transactional
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        ReservationEquipment re = reservationEquipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        if (payload.containsKey("quantity")) {
            int qty = Integer.parseInt(payload.get("quantity").toString());
            re.setQuantity(qty);
            if (re.getPricePerDay() != null) re.setSubtotal(re.getPricePerDay().multiply(BigDecimal.valueOf(qty)));
        }
        if (payload.containsKey("pricePerDay")) {
            BigDecimal ppd = new BigDecimal(payload.get("pricePerDay").toString());
            re.setPricePerDay(ppd);
            if (re.getQuantity() != null) re.setSubtotal(ppd.multiply(BigDecimal.valueOf(re.getQuantity())));
        }
        return toMap(reservationEquipmentRepository.save(re));
    }

    @PutMapping("/{id}/status")
    @Transactional
    public Map<String, Object> updateStatus(@PathVariable Long id, @RequestParam String status) {
        ReservationEquipment re = reservationEquipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        if ("CANCELLED".equalsIgnoreCase(status)) re.setQuantity(0);
        return toMap(reservationEquipmentRepository.save(re));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!reservationEquipmentRepository.existsById(id))
            throw new ResourceNotFoundException("Order not found: " + id);
        reservationEquipmentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private Map<String, Object> toMap(ReservationEquipment re) {
        String customerName = "Anonymous";
        String equipmentName = "N/A";
        String reservationNumber = "";

        if (re.getEquipment() != null) equipmentName = re.getEquipment().getName() != null ? re.getEquipment().getName() : "N/A";
        if (re.getReservation() != null) {
            reservationNumber = re.getReservation().getReservationNumber() != null ? re.getReservation().getReservationNumber() : "";
            if (re.getReservation().getCamper() != null) {
                String fn = re.getReservation().getCamper().getFirstName() != null ? re.getReservation().getCamper().getFirstName() : "";
                String ln = re.getReservation().getCamper().getLastName() != null ? re.getReservation().getCamper().getLastName() : "";
                customerName = (fn + " " + ln).trim();
                if (customerName.isEmpty()) customerName = re.getReservation().getCamper().getEmail() != null ? re.getReservation().getCamper().getEmail() : "Anonymous";
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", re.getId() != null ? re.getId() : 0L);
        result.put("equipmentName", equipmentName);
        result.put("equipmentId", re.getEquipment() != null ? re.getEquipment().getId() : null);
        result.put("customerName", customerName);
        result.put("reservationNumber", reservationNumber);
        result.put("reservationId", re.getReservation() != null ? re.getReservation().getId() : null);
        result.put("quantity", re.getQuantity() != null ? re.getQuantity() : 0);
        result.put("pricePerDay", re.getPricePerDay() != null ? re.getPricePerDay() : BigDecimal.ZERO);
        result.put("subtotal", re.getSubtotal() != null ? re.getSubtotal() : BigDecimal.ZERO);
        result.put("status", re.getQuantity() != null && re.getQuantity() == 0 ? "CANCELLED" : "ACTIVE");
        return result;
    }
}
