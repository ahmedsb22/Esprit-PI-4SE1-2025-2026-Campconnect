package tn.esprit.exam.service;

import tn.esprit.exam.dto.CampingSiteDTO;

import java.math.BigDecimal;
import java.util.List;

public interface ICampingSiteService {
    CampingSiteDTO createCampingSite(CampingSiteDTO dto, Long ownerId);
    CampingSiteDTO updateCampingSite(Long id, CampingSiteDTO dto, Long ownerId);
    CampingSiteDTO getCampingSiteById(Long id);
    List<CampingSiteDTO> getAllCampingSites();
    List<CampingSiteDTO> getActiveCampingSites();
    List<CampingSiteDTO> getCampingSitesByOwner(Long ownerId);
    List<CampingSiteDTO> getCampingSitesByCategory(String category);
    List<CampingSiteDTO> getCampingSitesByLocation(String location);
    List<CampingSiteDTO> getCampingSitesByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);
    List<CampingSiteDTO> getTopRatedCampingSites();
    void deleteCampingSite(Long id, Long ownerId);
    CampingSiteDTO verifyCampingSite(Long id);
    CampingSiteDTO toggleActiveStatus(Long id, Long ownerId);
}
