package tn.esprit.exam.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.exam.entity.ContractStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractDTO {
    private Long id;
    private String contractNumber;

    @NotBlank(message = "Contract terms are required")
    private String terms;

    private Boolean isSigned;
    private LocalDateTime signedAt;
    private String signatureUrl;
    private ContractStatus status;

    @NotNull(message = "Reservation is required")
    private Long reservationId;
    private String reservationNumber;
}
