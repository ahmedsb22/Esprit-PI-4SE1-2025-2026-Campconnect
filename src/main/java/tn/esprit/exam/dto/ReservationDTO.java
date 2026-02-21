package tn.esprit.exam.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.exam.entity.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationDTO {
    private Long id;
    private String reservationNumber;

    @NotNull(message = "Check-in date is required")
    @Future(message = "Check-in date must be in the future")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    @Future(message = "Check-out date must be in the future")
    private LocalDate checkOutDate;

    @NotNull(message = "Number of guests is required")
    @Min(value = 1)
    private Integer numberOfGuests;

    private BigDecimal totalPrice;
    private ReservationStatus status;
    private String specialRequests;

    @NotNull(message = "Camping site is required")
    private Long campingSiteId;
    private String campingSiteName;
    private String campingSiteLocation;

    private Long camperId;
    private String camperName;
    private String camperEmail;

    private List<ReservationEquipmentDTO> equipments;
    private Long contractId;
}
