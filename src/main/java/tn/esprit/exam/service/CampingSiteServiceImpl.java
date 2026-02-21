package tn.esprit.exam.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.exam.dto.CampingSiteDTO;
import tn.esprit.exam.entity.CampingSite;
import tn.esprit.exam.entity.User;
import tn.esprit.exam.exception.ResourceNotFoundException;
import tn.esprit.exam.exception.UnauthorizedException;
import tn.esprit.exam.repository.CampingSiteRepository;
import tn.esprit.exam.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CampingSiteServiceImpl implements ICampingSiteService {

    private final CampingSiteRepository campingSiteRepository;
    private final UserRepository userRepository;

    @Override
    public CampingSiteDTO createCampingSite(CampingSiteDTO dto, Long ownerId) {
        log.info("Creating camping site: {} for owner: {}", dto.getName(), ownerId);

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found with id: " + ownerId));

        CampingSite site = CampingSite.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .location(dto.getLocation())
                .address(dto.getAddress())
                .pricePerNight(dto.getPricePerNight())
                .capacity(dto.getCapacity())
                .category(dto.getCategory())
                .imageUrl(dto.getImageUrl())
                .hasWifi(dto.getHasWifi() != null ? dto.getHasWifi() : false)
                .hasParking(dto.getHasParking() != null ? dto.getHasParking() : false)
                .hasRestrooms(dto.getHasRestrooms() != null ? dto.getHasRestrooms() : false)
                .hasShowers(dto.getHasShowers() != null ? dto.getHasShowers() : false)
                .hasElectricity(dto.getHasElectricity() != null ? dto.getHasElectricity() : false)
                .hasPetFriendly(dto.getHasPetFriendly() != null ? dto.getHasPetFriendly() : false)
                .isActive(true)
                .isVerified(false)
                .rating(BigDecimal.ZERO)
                .reviewCount(0)
                .owner(owner)
                .build();

        CampingSite saved = campingSiteRepository.save(site);
        log.info("Camping site created with id: {}", saved.getId());

        return mapToDTO(saved);
    }

    @Override
    public CampingSiteDTO updateCampingSite(Long id, CampingSiteDTO dto, Long ownerId) {
        log.info("Updating camping site: {} by owner: {}", id, ownerId);

        CampingSite site = campingSiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Camping site not found with id: " + id));

        if (!site.getOwner().getId().equals(ownerId)) {
            throw new UnauthorizedException("You are not authorized to update this camping site");
        }

        site.setName(dto.getName());
        site.setDescription(dto.getDescription());
        site.setLocation(dto.getLocation());
        site.setAddress(dto.getAddress());
        site.setPricePerNight(dto.getPricePerNight());
        site.setCapacity(dto.getCapacity());
        site.setCategory(dto.getCategory());
        site.setImageUrl(dto.getImageUrl());
        site.setHasWifi(dto.getHasWifi());
        site.setHasParking(dto.getHasParking());
        site.setHasRestrooms(dto.getHasRestrooms());
        site.setHasShowers(dto.getHasShowers());
        site.setHasElectricity(dto.getHasElectricity());
        site.setHasPetFriendly(dto.getHasPetFriendly());

        CampingSite updated = campingSiteRepository.save(site);
        log.info("Camping site updated: {}", updated.getId());

        return mapToDTO(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public CampingSiteDTO getCampingSiteById(Long id) {
        CampingSite site = campingSiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Camping site not found with id: " + id));
        return mapToDTO(site);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampingSiteDTO> getAllCampingSites() {
        return campingSiteRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampingSiteDTO> getActiveCampingSites() {
        return campingSiteRepository.findByIsActiveTrueAndIsVerifiedTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampingSiteDTO> getCampingSitesByOwner(Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found with id: " + ownerId));
        return campingSiteRepository.findByOwner(owner).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampingSiteDTO> getCampingSitesByCategory(String category) {
        return campingSiteRepository.findByCategoryAndIsActive(category, true).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampingSiteDTO> getCampingSitesByLocation(String location) {
        return campingSiteRepository.findByLocationContainingIgnoreCase(location).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampingSiteDTO> getCampingSitesByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return campingSiteRepository.findByPriceRange(minPrice, maxPrice).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampingSiteDTO> getTopRatedCampingSites() {
        return campingSiteRepository.findTopRatedSites().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteCampingSite(Long id, Long ownerId) {
        CampingSite site = campingSiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Camping site not found with id: " + id));

        if (!site.getOwner().getId().equals(ownerId)) {
            throw new UnauthorizedException("You are not authorized to delete this camping site");
        }

        campingSiteRepository.delete(site);
        log.info("Camping site deleted: {}", id);
    }

    @Override
    public CampingSiteDTO verifyCampingSite(Long id) {
        CampingSite site = campingSiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Camping site not found with id: " + id));

        site.setIsVerified(true);
        CampingSite verified = campingSiteRepository.save(site);
        log.info("Camping site verified: {}", id);

        return mapToDTO(verified);
    }

    @Override
    public CampingSiteDTO toggleActiveStatus(Long id, Long ownerId) {
        CampingSite site = campingSiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Camping site not found with id: " + id));

        if (!site.getOwner().getId().equals(ownerId)) {
            throw new UnauthorizedException("You are not authorized to modify this camping site");
        }

        site.setIsActive(!site.getIsActive());
        CampingSite updated = campingSiteRepository.save(site);
        log.info("Camping site active status toggled: {} - {}", id, updated.getIsActive());

        return mapToDTO(updated);
    }

    private CampingSiteDTO mapToDTO(CampingSite site) {
        Long ownerId = site.getOwner() != null ? site.getOwner().getId() : null;
        String ownerName = site.getOwner() != null
                ? site.getOwner().getFirstName() + " " + site.getOwner().getLastName()
                : "Unknown";
        return CampingSiteDTO.builder()
                .id(site.getId())
                .name(site.getName())
                .description(site.getDescription())
                .location(site.getLocation())
                .address(site.getAddress())
                .pricePerNight(site.getPricePerNight())
                .capacity(site.getCapacity())
                .category(site.getCategory())
                .imageUrl(site.getImageUrl())
                .hasWifi(site.getHasWifi())
                .hasParking(site.getHasParking())
                .hasRestrooms(site.getHasRestrooms())
                .hasShowers(site.getHasShowers())
                .hasElectricity(site.getHasElectricity())
                .hasPetFriendly(site.getHasPetFriendly())
                .isActive(site.getIsActive())
                .isVerified(site.getIsVerified())
                .rating(site.getRating())
                .reviewCount(site.getReviewCount())
                .ownerId(ownerId)
                .ownerName(ownerName)
                .build();
    }
}
