package tn.esprit.exam.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.exam.dto.EquipmentDTO;
import tn.esprit.exam.entity.Equipment;
import tn.esprit.exam.entity.User;
import tn.esprit.exam.exception.ResourceNotFoundException;
import tn.esprit.exam.exception.UnauthorizedException;
import tn.esprit.exam.repository.EquipmentRepository;
import tn.esprit.exam.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EquipmentServiceImpl implements IEquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;

    @Override
    public EquipmentDTO createEquipment(EquipmentDTO dto, Long providerId) {
        log.info("Creating equipment: {} for provider: {}", dto.getName(), providerId);
        
        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with id: " + providerId));

        Equipment equipment = Equipment.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .pricePerDay(dto.getPricePerDay())
                .stockQuantity(dto.getStockQuantity())
                .availableQuantity(dto.getStockQuantity())
                .imageUrl(dto.getImageUrl())
                .specifications(dto.getSpecifications())
                .isActive(true)
                .rating(BigDecimal.ZERO)
                .reviewCount(0)
                .provider(provider)
                .build();

        Equipment saved = equipmentRepository.save(equipment);
        log.info("Equipment created with id: {}", saved.getId());
        
        return mapToDTO(saved);
    }

    @Override
    public EquipmentDTO updateEquipment(Long id, EquipmentDTO dto, Long providerId) {
        log.info("Updating equipment: {} by provider: {}", id, providerId);
        
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found with id: " + id));

        if (!equipment.getProvider().getId().equals(providerId)) {
            throw new UnauthorizedException("You are not authorized to update this equipment");
        }

        equipment.setName(dto.getName());
        equipment.setDescription(dto.getDescription());
        equipment.setCategory(dto.getCategory());
        equipment.setPricePerDay(dto.getPricePerDay());
        equipment.setStockQuantity(dto.getStockQuantity());
        equipment.setImageUrl(dto.getImageUrl());
        equipment.setSpecifications(dto.getSpecifications());

        Equipment updated = equipmentRepository.save(equipment);
        log.info("Equipment updated: {}", updated.getId());
        
        return mapToDTO(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public EquipmentDTO getEquipmentById(Long id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found with id: " + id));
        return mapToDTO(equipment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentDTO> getAllEquipment() {
        return equipmentRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentDTO> getActiveEquipment() {
        return equipmentRepository.findByIsActiveTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentDTO> getAvailableEquipment() {
        return equipmentRepository.findAvailableEquipment().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentDTO> getEquipmentByCategory(String category) {
        return equipmentRepository.findByCategoryAndIsActiveTrue(category).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentDTO> getEquipmentByProvider(Long providerId) {
        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with id: " + providerId));
        return equipmentRepository.findByProvider(provider).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentDTO> getEquipmentByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return equipmentRepository.findByPriceRange(minPrice, maxPrice).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentDTO> getTopRatedEquipment() {
        return equipmentRepository.findTopRatedEquipment().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteEquipment(Long id, Long providerId) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found with id: " + id));

        if (!equipment.getProvider().getId().equals(providerId)) {
            throw new UnauthorizedException("You are not authorized to delete this equipment");
        }

        equipmentRepository.delete(equipment);
        log.info("Equipment deleted: {}", id);
    }

    @Override
    public EquipmentDTO toggleActiveStatus(Long id, Long providerId) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found with id: " + id));

        if (!equipment.getProvider().getId().equals(providerId)) {
            throw new UnauthorizedException("You are not authorized to modify this equipment");
        }

        equipment.setIsActive(!equipment.getIsActive());
        Equipment updated = equipmentRepository.save(equipment);
        log.info("Equipment active status toggled: {} - {}", id, updated.getIsActive());
        
        return mapToDTO(updated);
    }

    @Override
    public void updateAvailability(Long equipmentId, Integer quantityChange) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found with id: " + equipmentId));

        int newAvailable = equipment.getAvailableQuantity() + quantityChange;

        // Clamp: never go below 0 or above stockQuantity
        newAvailable = Math.max(0, Math.min(newAvailable, equipment.getStockQuantity()));

        equipment.setAvailableQuantity(newAvailable);
        equipmentRepository.save(equipment);
        log.info("Equipment availability updated: {} - new available: {}", equipmentId, newAvailable);
    }

    private EquipmentDTO mapToDTO(Equipment equipment) {
        return EquipmentDTO.builder()
                .id(equipment.getId())
                .name(equipment.getName())
                .description(equipment.getDescription())
                .category(equipment.getCategory())
                .pricePerDay(equipment.getPricePerDay())
                .stockQuantity(equipment.getStockQuantity())
                .availableQuantity(equipment.getAvailableQuantity())
                .imageUrl(equipment.getImageUrl())
                .specifications(equipment.getSpecifications())
                .isActive(equipment.getIsActive())
                .rating(equipment.getRating())
                .reviewCount(equipment.getReviewCount())
                .providerId(equipment.getProvider().getId())
                .providerName(equipment.getProvider().getFirstName() + " " + equipment.getProvider().getLastName())
                .build();
    }
}
