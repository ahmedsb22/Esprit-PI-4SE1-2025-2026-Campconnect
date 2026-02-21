package tn.esprit.exam.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentDTO {
    private Long id;

    @NotBlank(message = "Equipment name is required")
    @Size(min = 3, max = 100)
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Price per day is required")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal pricePerDay;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0)
    private Integer stockQuantity;

    private Integer availableQuantity;
    private String imageUrl;
    private String specifications;
    private Boolean isActive;
    private BigDecimal rating;
    private Integer reviewCount;
    
    private Long providerId;
    private String providerName;
}
