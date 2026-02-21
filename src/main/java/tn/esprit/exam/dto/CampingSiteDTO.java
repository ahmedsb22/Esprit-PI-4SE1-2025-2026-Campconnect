package tn.esprit.exam.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampingSiteDTO {
    private Long id;

    @NotBlank(message = "Site name is required")
    @Size(min = 3, max = 100)
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Price per night is required")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal pricePerNight;

    @NotNull(message = "Capacity is required")
    @Min(value = 1)
    private Integer capacity;

    private String category;
    private String imageUrl;
    
    private Boolean hasWifi;
    private Boolean hasParking;
    private Boolean hasRestrooms;
    private Boolean hasShowers;
    private Boolean hasElectricity;
    private Boolean hasPetFriendly;
    
    private Boolean isActive;
    private Boolean isVerified;
    private BigDecimal rating;
    private Integer reviewCount;
    
    private Long ownerId;
    private String ownerName;
}
