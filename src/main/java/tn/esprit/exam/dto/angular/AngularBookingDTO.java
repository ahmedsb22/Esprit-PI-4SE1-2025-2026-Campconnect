package tn.esprit.exam.dto.angular;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO matching Angular's Booking interface
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AngularBookingDTO {
    private Long id;
    private AngularUserDTO camper;
    private AngularSiteDTO site;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer numberOfGuests;
    private BigDecimal totalPrice;
    private String status;
    private String specialRequests;
    private LocalDateTime createdAt;
    private LocalDateTime cancelledAt;
    private Long invoiceId;
}

