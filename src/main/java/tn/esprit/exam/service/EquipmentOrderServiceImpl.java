package tn.esprit.exam.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.exam.dto.EquipmentOrderDTO;
import tn.esprit.exam.entity.Equipment;
import tn.esprit.exam.entity.Reservation;
import tn.esprit.exam.entity.ReservationEquipment;
import tn.esprit.exam.exception.BusinessLogicException;
import tn.esprit.exam.exception.ResourceNotFoundException;
import tn.esprit.exam.repository.EquipmentRepository;
import tn.esprit.exam.repository.ReservationEquipmentRepository;
import tn.esprit.exam.repository.ReservationRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EquipmentOrderServiceImpl implements IEquipmentOrderService {

    private final ReservationEquipmentRepository reservationEquipmentRepository;
    private final EquipmentRepository equipmentRepository;
    private final ReservationRepository reservationRepository;

    @Override
    public EquipmentOrderDTO createOrder(EquipmentOrderDTO dto, Long userId) {
        log.info("Creating equipment order for user: {}", userId);

        // Vérifier que l'équipement existe
        Equipment equipment = equipmentRepository.findById(dto.getEquipmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Equipment not found with id: " + dto.getEquipmentId()));

        // Vérifier la disponibilité
        int qty = dto.getQuantity() != null ? dto.getQuantity() : 1;
        if (equipment.getAvailableQuantity() == null || equipment.getAvailableQuantity() < qty) {
            throw new BusinessLogicException(
                    "Not enough equipment available. Requested: " + qty +
                    ", Available: " + equipment.getAvailableQuantity());
        }

        // Vérifier que la réservation existe si fournie
        Reservation reservation = null;
        if (dto.getReservationId() != null) {
            reservation = reservationRepository.findById(dto.getReservationId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Reservation not found with id: " + dto.getReservationId()));
        }

        // Calculer le sous-total automatiquement
        BigDecimal pricePerDay = equipment.getPricePerDay();
        BigDecimal subtotal = pricePerDay.multiply(BigDecimal.valueOf(qty));

        ReservationEquipment order = ReservationEquipment.builder()
                .equipment(equipment)
                .reservation(reservation)
                .quantity(qty)
                .pricePerDay(pricePerDay)
                .subtotal(subtotal)
                .build();

        // Mettre à jour la quantité disponible
        equipment.setAvailableQuantity(equipment.getAvailableQuantity() - qty);
        equipmentRepository.save(equipment);

        ReservationEquipment saved = reservationEquipmentRepository.save(order);
        log.info("Equipment order created with id: {}", saved.getId());

        return EquipmentOrderDTO.fromEntity(saved);
    }

    @Override
    public EquipmentOrderDTO updateOrder(Long id, EquipmentOrderDTO dto) {
        log.info("Updating equipment order: {}", id);

        ReservationEquipment order = reservationEquipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Equipment order not found with id: " + id));

        // Mettre à jour la quantité
        if (dto.getQuantity() != null) {
            int oldQty = order.getQuantity();
            int newQty = dto.getQuantity();
            int diff = newQty - oldQty;

            Equipment equipment = order.getEquipment();

            // Vérifier la disponibilité si on augmente la quantité
            if (diff > 0 && equipment.getAvailableQuantity() < diff) {
                throw new BusinessLogicException(
                        "Not enough equipment available for update");
            }

            // Mettre à jour la quantité disponible
            equipment.setAvailableQuantity(equipment.getAvailableQuantity() - diff);
            equipmentRepository.save(equipment);

            order.setQuantity(newQty);
            order.setSubtotal(order.getPricePerDay().multiply(BigDecimal.valueOf(newQty)));
        }

        ReservationEquipment updated = reservationEquipmentRepository.save(order);
        log.info("Equipment order updated: {}", id);

        return EquipmentOrderDTO.fromEntity(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public EquipmentOrderDTO getOrderById(Long id) {
        return EquipmentOrderDTO.fromEntity(
                reservationEquipmentRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Equipment order not found with id: " + id))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentOrderDTO> getAllOrders() {
        return reservationEquipmentRepository.findAll().stream()
                .map(EquipmentOrderDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentOrderDTO> getOrdersByReservation(Long reservationId) {
        return reservationEquipmentRepository.findAll().stream()
                .filter(o -> o.getReservation() != null &&
                        o.getReservation().getId().equals(reservationId))
                .map(EquipmentOrderDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void cancelOrder(Long id) {
        ReservationEquipment order = reservationEquipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Equipment order not found with id: " + id));

        // Remettre la quantité disponible
        Equipment equipment = order.getEquipment();
        equipment.setAvailableQuantity(
                equipment.getAvailableQuantity() + order.getQuantity());
        equipmentRepository.save(equipment);

        reservationEquipmentRepository.delete(order);
        log.info("Equipment order cancelled: {}", id);
    }
}