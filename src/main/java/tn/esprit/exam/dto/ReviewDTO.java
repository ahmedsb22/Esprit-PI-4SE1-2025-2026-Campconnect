package tn.esprit.exam.dto;

import lombok.*;
import tn.esprit.exam.entity.Review;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDTO {

    private Long id;
    private Integer rating;
    private String comment;
    private Boolean isVerified;
    private Long authorId;
    private String authorName;
    private Long campingSiteId;
    private String campingSiteName;
    private Long reservationId;
    private LocalDateTime createdAt;

    public static ReviewDTO fromEntity(Review r) {
        return ReviewDTO.builder()
                .id(r.getId())
                .rating(r.getRating())
                .comment(r.getComment())
                .isVerified(r.getIsVerified())
                .authorId(r.getAuthor() != null ? r.getAuthor().getId() : null)
                .authorName(r.getAuthor() != null ?
                        r.getAuthor().getFirstName() + " " + r.getAuthor().getLastName() : null)
                .campingSiteId(r.getCampingSite() != null ? r.getCampingSite().getId() : null)
                .campingSiteName(r.getCampingSite() != null ? r.getCampingSite().getName() : null)
                .reservationId(r.getReservation() != null ? r.getReservation().getId() : null)
                .createdAt(r.getCreatedAt())
                .build();
    }
}