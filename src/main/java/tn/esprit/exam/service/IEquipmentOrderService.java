package tn.esprit.exam.service;

import tn.esprit.exam.dto.EquipmentOrderDTO;
import java.util.List;

public interface IEquipmentOrderService {
    EquipmentOrderDTO createOrder(EquipmentOrderDTO dto, Long userId);
    EquipmentOrderDTO updateOrder(Long id, EquipmentOrderDTO dto);
    EquipmentOrderDTO getOrderById(Long id);
    List<EquipmentOrderDTO> getAllOrders();
    List<EquipmentOrderDTO> getOrdersByReservation(Long reservationId);
    void cancelOrder(Long id);
}