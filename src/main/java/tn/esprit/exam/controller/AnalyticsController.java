package tn.esprit.exam.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.exam.entity.Reservation;
import tn.esprit.exam.entity.ReservationStatus;
import tn.esprit.exam.repository.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final CampingSiteRepository campingSiteRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationEquipmentRepository reservationEquipmentRepository;
    private final InvoiceRepository invoiceRepository;

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard() {
        Map<String, Object> result = new HashMap<>();
        result.put("totalSites", campingSiteRepository.count());
        result.put("totalUsers", userRepository.count());
        result.put("totalOrders", reservationEquipmentRepository.count());
        result.put("totalBookings", reservationRepository.count());

        // Revenue from invoices
        double totalRevenue = invoiceRepository.findAll().stream()
                .filter(i -> i.getTotalAmount() != null)
                .mapToDouble(i -> i.getTotalAmount().doubleValue())
                .sum();

        Map<String, Object> revenueStats = new HashMap<>();
        revenueStats.put("totalRevenue", totalRevenue);
        revenueStats.put("monthlyRevenue", new int[]{0,0,0,0,0,0,0,0,0,0,0,0});
        result.put("revenueStats", revenueStats);

        // Booking stats from real data
        List<Reservation> bookings = reservationRepository.findAll();
        long confirmed = bookings.stream().filter(r -> r.getStatus() == ReservationStatus.CONFIRMED).count();
        long pending   = bookings.stream().filter(r -> r.getStatus() == ReservationStatus.PENDING).count();
        long cancelled = bookings.stream().filter(r -> r.getStatus() == ReservationStatus.CANCELLED).count();

        Map<String, Object> bookingStats = new HashMap<>();
        bookingStats.put("confirmedBookings", confirmed);
        bookingStats.put("pendingBookings", pending);
        bookingStats.put("cancelledBookings", cancelled);
        result.put("bookingStats", bookingStats);

        return result;
    }
}
