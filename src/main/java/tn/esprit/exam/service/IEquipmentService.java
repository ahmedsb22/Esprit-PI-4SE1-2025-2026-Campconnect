package tn.esprit.exam.service;

import tn.esprit.exam.dto.EquipmentDTO;

import java.math.BigDecimal;
import java.util.List;

public interface IEquipmentService {
    EquipmentDTO createEquipment(EquipmentDTO dto, Long providerId);
    EquipmentDTO updateEquipment(Long id, EquipmentDTO dto, Long providerId);
    EquipmentDTO getEquipmentById(Long id);
    List<EquipmentDTO> getAllEquipment();
    List<EquipmentDTO> getActiveEquipment();
    List<EquipmentDTO> getAvailableEquipment();
    List<EquipmentDTO> getEquipmentByCategory(String category);
    List<EquipmentDTO> getEquipmentByProvider(Long providerId);
    List<EquipmentDTO> getEquipmentByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);
    List<EquipmentDTO> getTopRatedEquipment();
    void deleteEquipment(Long id, Long providerId);
    EquipmentDTO toggleActiveStatus(Long id, Long providerId);
    void updateAvailability(Long equipmentId, Integer quantityChange);
}
