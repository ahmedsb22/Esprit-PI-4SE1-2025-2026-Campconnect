package tn.esprit.exam.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.exam.dto.ReviewDTO;
import tn.esprit.exam.entity.*;
import tn.esprit.exam.exception.BusinessLogicException;
import tn.esprit.exam.exception.ResourceNotFoundException;
import tn.esprit.exam.exception.UnauthorizedException;
import tn.esprit.exam.repository.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReviewServiceImpl implements IReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final CampingSiteRepository campingSiteRepository;
    private final ReservationRepository reservationRepository;

    @Override
    public ReviewDTO createReview(ReviewDTO dto, Long authorId) {
        log.info("Creating review for site: {} by user: {}", dto.getCampingSiteId(), authorId);

        // Valider la note (1 à 5)
        if (dto.getRating() == null || dto.getRating() < 1 || dto.getRating() > 5) {
            throw new BusinessLogicException("La note doit être entre 1 et 5");
        }

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé: " + authorId));

        CampingSite site = campingSiteRepository.findById(dto.getCampingSiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Site non trouvé: " + dto.getCampingSiteId()));

        // Vérifier si lié à une réservation
        Reservation reservation = null;
        boolean isVerified = false;

        if (dto.getReservationId() != null) {
            reservation = reservationRepository.findById(dto.getReservationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée: " + dto.getReservationId()));

            // Vérifier que la réservation appartient au camper
            if (!reservation.getCamper().getId().equals(authorId)) {
                throw new UnauthorizedException("Cette réservation ne vous appartient pas");
            }

            // Vérifier qu'un avis n'existe pas déjà pour cette réservation
            if (reviewRepository.existsByReservationId(dto.getReservationId())) {
                throw new BusinessLogicException("Un avis existe déjà pour cette réservation");
            }

            // Marquer comme vérifié si la réservation est COMPLETED
            isVerified = reservation.getStatus() == ReservationStatus.COMPLETED;
        }

        Review review = Review.builder()
                .rating(dto.getRating())
                .comment(dto.getComment())
                .isVerified(isVerified)
                .author(author)
                .campingSite(site)
                .reservation(reservation)
                .build();

        Review saved = reviewRepository.save(review);

        // Mettre à jour la note moyenne du site
        updateSiteRating(site.getId());

        log.info("Review created with id: {}", saved.getId());
        return ReviewDTO.fromEntity(saved);
    }

    @Override
    public ReviewDTO updateReview(Long id, ReviewDTO dto, Long authorId) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Avis non trouvé: " + id));

        if (!review.getAuthor().getId().equals(authorId)) {
            throw new UnauthorizedException("Vous n'êtes pas autorisé à modifier cet avis");
        }

        if (dto.getRating() != null) {
            if (dto.getRating() < 1 || dto.getRating() > 5) {
                throw new BusinessLogicException("La note doit être entre 1 et 5");
            }
            review.setRating(dto.getRating());
        }

        if (dto.getComment() != null) {
            review.setComment(dto.getComment());
        }

        Review updated = reviewRepository.save(review);
        updateSiteRating(review.getCampingSite().getId());

        log.info("Review updated: {}", id);
        return ReviewDTO.fromEntity(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewDTO getReviewById(Long id) {
        return ReviewDTO.fromEntity(
                reviewRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Avis non trouvé: " + id))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDTO> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(ReviewDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDTO> getReviewsBySite(Long campingSiteId) {
        return reviewRepository.findByCampingSiteId(campingSiteId).stream()
                .map(ReviewDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDTO> getReviewsByAuthor(Long authorId) {
        return reviewRepository.findByAuthorId(authorId).stream()
                .map(ReviewDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteReview(Long id, Long authorId) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Avis non trouvé: " + id));

        // Seul l'auteur ou un admin peut supprimer
        if (!review.getAuthor().getId().equals(authorId)) {
            throw new UnauthorizedException("Vous n'êtes pas autorisé à supprimer cet avis");
        }

        Long siteId = review.getCampingSite().getId();
        reviewRepository.delete(review);
        updateSiteRating(siteId);

        log.info("Review deleted: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageRatingBySite(Long campingSiteId) {
        Double avg = reviewRepository.findAverageRatingBySiteId(campingSiteId);
        return avg != null ? avg : 0.0;
    }

    // Met à jour la note moyenne et le nombre d'avis du site
    private void updateSiteRating(Long siteId) {
        CampingSite site = campingSiteRepository.findById(siteId).orElse(null);
        if (site == null) return;

        List<Review> reviews = reviewRepository.findByCampingSiteId(siteId);
        int count = reviews.size();
        double avg = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        site.setRating(new java.math.BigDecimal(avg).setScale(2, java.math.RoundingMode.HALF_UP));
        site.setReviewCount(count);
        campingSiteRepository.save(site);
    }
}