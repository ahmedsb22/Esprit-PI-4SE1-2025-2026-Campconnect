package tn.esprit.exam.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tn.esprit.exam.entity.Invoice;
import tn.esprit.exam.entity.Reservation;
import tn.esprit.exam.entity.ReservationEquipment;
import tn.esprit.exam.exception.ResourceNotFoundException;
import tn.esprit.exam.repository.InvoiceRepository;
import tn.esprit.exam.repository.ReservationEquipmentRepository;
import tn.esprit.exam.repository.ReservationRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceRepository invoiceRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationEquipmentRepository reservationEquipmentRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAll() {
        return invoiceRepository.findAll().stream()
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public Map<String, Object> getById(@PathVariable Long id) {
        return toMap(invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id)));
    }

    @PostMapping
    @Transactional
    public Map<String, Object> create(@RequestBody Map<String, Object> payload) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(payload.containsKey("invoiceNumber")
                ? payload.get("invoiceNumber").toString()
                : "INV-" + System.currentTimeMillis());
        invoice.setStatus(Invoice.InvoiceStatus.DRAFT);
        invoice.setIssuedAt(LocalDateTime.now());
        invoice.setTotalAmount(payload.containsKey("totalAmount")
                ? new BigDecimal(payload.get("totalAmount").toString())
                : BigDecimal.ZERO);

        if (payload.containsKey("reservation") && payload.get("reservation") instanceof Map) {
            Object resId = ((Map<?,?>) payload.get("reservation")).get("id");
            if (resId != null) {
                Reservation res = reservationRepository.findById(Long.valueOf(resId.toString()))
                        .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + resId));
                invoice.setReservation(res);
                if (invoice.getTotalAmount().compareTo(BigDecimal.ZERO) == 0 && res.getTotalPrice() != null) {
                    invoice.setTotalAmount(res.getTotalPrice());
                }
            }
        }
        if (payload.containsKey("equipmentOrder") && payload.get("equipmentOrder") instanceof Map) {
            Object ordId = ((Map<?,?>) payload.get("equipmentOrder")).get("id");
            if (ordId != null) {
                ReservationEquipment re = reservationEquipmentRepository.findById(Long.valueOf(ordId.toString()))
                        .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + ordId));
                invoice.setEquipmentOrder(re);
                if (invoice.getTotalAmount().compareTo(BigDecimal.ZERO) == 0 && re.getSubtotal() != null) {
                    invoice.setTotalAmount(re.getSubtotal());
                }
            }
        }
        return toMap(invoiceRepository.save(invoice));
    }

    @PutMapping("/{id}")
    @Transactional
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id));
        if (payload.containsKey("status")) {
            try { invoice.setStatus(Invoice.InvoiceStatus.valueOf(payload.get("status").toString().toUpperCase())); }
            catch (Exception ignored) {}
        }
        if (payload.containsKey("totalAmount")) {
            invoice.setTotalAmount(new BigDecimal(payload.get("totalAmount").toString()));
        }
        if (payload.containsKey("notes")) invoice.setNotes(payload.get("notes").toString());
        return toMap(invoiceRepository.save(invoice));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!invoiceRepository.existsById(id))
            throw new ResourceNotFoundException("Invoice not found: " + id);
        invoiceRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private Map<String, Object> toMap(Invoice inv) {
        String camperName = "N/A";
        String camperEmail = "";
        String siteName = "N/A";

        if (inv.getReservation() != null) {
            Reservation res = inv.getReservation();
            if (res.getCamper() != null) {
                camperName = ((res.getCamper().getFirstName() != null ? res.getCamper().getFirstName() : "") + " "
                        + (res.getCamper().getLastName() != null ? res.getCamper().getLastName() : "")).trim();
                camperEmail = res.getCamper().getEmail() != null ? res.getCamper().getEmail() : "";
            }
            if (res.getCampingSite() != null && res.getCampingSite().getName() != null) {
                siteName = res.getCampingSite().getName();
            }
        } else if (inv.getEquipmentOrder() != null && inv.getEquipmentOrder().getReservation() != null) {
            Reservation res = inv.getEquipmentOrder().getReservation();
            if (res.getCamper() != null) {
                camperName = ((res.getCamper().getFirstName() != null ? res.getCamper().getFirstName() : "") + " "
                        + (res.getCamper().getLastName() != null ? res.getCamper().getLastName() : "")).trim();
                camperEmail = res.getCamper().getEmail() != null ? res.getCamper().getEmail() : "";
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", inv.getId() != null ? inv.getId() : 0L);
        result.put("invoiceNumber", inv.getInvoiceNumber() != null ? inv.getInvoiceNumber() : "");
        result.put("status", inv.getStatus() != null ? inv.getStatus().name() : "DRAFT");
        result.put("totalAmount", inv.getTotalAmount() != null ? inv.getTotalAmount() : BigDecimal.ZERO);
        result.put("issuedAt", inv.getIssuedAt() != null ? inv.getIssuedAt().toString() : "");
        result.put("notes", inv.getNotes() != null ? inv.getNotes() : "");
        result.put("camperName", camperName);
        result.put("camperEmail", camperEmail);
        result.put("siteName", siteName);
        result.put("reservationId", inv.getReservation() != null ? inv.getReservation().getId() : null);
        result.put("equipmentOrderId", inv.getEquipmentOrder() != null ? inv.getEquipmentOrder().getId() : null);
        return result;
    }
}
