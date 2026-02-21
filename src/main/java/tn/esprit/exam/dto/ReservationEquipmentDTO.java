package tn.esprit.exam.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationEquipmentDTO {
    private Long id;

    @NotNull(message = "Equipment is required")
    private Long equipmentId;
    
    private String equipmentName;
    private String equipmentCategory;

    @NotNull(message = "Quantity is required")
    @Min(value = 1)
    private Integer quantity;

    private BigDecimal pricePerDay;
    private BigDecimal subtotal;
}
