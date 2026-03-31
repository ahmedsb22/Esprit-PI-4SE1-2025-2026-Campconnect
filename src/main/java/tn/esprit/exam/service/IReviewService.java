package tn.esprit.exam.service;

import tn.esprit.exam.dto.ReviewDTO;
import java.util.List;

public interface IReviewService {
    ReviewDTO createReview(ReviewDTO dto, Long authorId);
    ReviewDTO updateReview(Long id, ReviewDTO dto, Long authorId);
    ReviewDTO getReviewById(Long id);
    List<ReviewDTO> getAllReviews();
    List<ReviewDTO> getReviewsBySite(Long campingSiteId);
    List<ReviewDTO> getReviewsByAuthor(Long authorId);
    void deleteReview(Long id, Long authorId);
    Double getAverageRatingBySite(Long campingSiteId);
}