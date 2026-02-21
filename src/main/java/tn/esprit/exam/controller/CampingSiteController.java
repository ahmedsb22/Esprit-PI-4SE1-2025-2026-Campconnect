package tn.esprit.exam.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tn.esprit.exam.entity.CampingSite;
import tn.esprit.exam.repository.CampingSiteRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sites")
@RequiredArgsConstructor
public class CampingSiteController {

    private final CampingSiteRepository campingSiteRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public List<CampingSite> getAll() {
        List<CampingSite> sites = campingSiteRepository.findAll();
        // detach owner to prevent lazy loading during serialization
        sites.forEach(s -> { if (s.getOwner() != null) s.getOwner().getRoles(); });
        return sites;
    }

    @GetMapping("/active")
    @Transactional(readOnly = true)
    public List<CampingSite> getActive() {
        return campingSiteRepository.findByIsActiveTrue();
    }

    @GetMapping("/search")
    @Transactional(readOnly = true)
    public List<CampingSite> search(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {
        return campingSiteRepository.findAll().stream()
                .filter(s -> location == null || (s.getLocation() != null && s.getLocation().toLowerCase().contains(location.toLowerCase())))
                .filter(s -> category == null || (s.getCategory() != null && s.getCategory().equalsIgnoreCase(category)))
                .filter(s -> minPrice == null || (s.getPricePerNight() != null && s.getPricePerNight().doubleValue() >= minPrice))
                .filter(s -> maxPrice == null || (s.getPricePerNight() != null && s.getPricePerNight().doubleValue() <= maxPrice))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public CampingSite getById(@PathVariable Long id) {
        return campingSiteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CampingSite not found: " + id));
    }

    @PostMapping
    @Transactional
    public CampingSite create(@RequestBody CampingSite site) {
        site.setId(null);
        site.setReservations(new java.util.HashSet<>());
        return campingSiteRepository.save(site);
    }

    @PutMapping("/{id}")
    @Transactional
    public CampingSite update(@PathVariable Long id, @RequestBody CampingSite site) {
        CampingSite existing = campingSiteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CampingSite not found: " + id));
        site.setId(id);
        site.setReservations(existing.getReservations());
        return campingSiteRepository.save(site);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!campingSiteRepository.existsById(id)) throw new IllegalArgumentException("CampingSite not found: " + id);
        campingSiteRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/approve")
    @Transactional
    public CampingSite approve(@PathVariable Long id) {
        CampingSite site = campingSiteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CampingSite not found: " + id));
        site.setIsVerified(true);
        site.setIsActive(true);
        return campingSiteRepository.save(site);
    }

    @PutMapping("/{id}/reject")
    @Transactional
    public CampingSite reject(@PathVariable Long id) {
        CampingSite site = campingSiteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CampingSite not found: " + id));
        site.setIsVerified(false);
        site.setIsActive(false);
        return campingSiteRepository.save(site);
    }
}
