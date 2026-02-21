package tn.esprit.exam.dto.angular;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * DTO matching Angular's CampingSite interface
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AngularSiteDTO {
    private Long id;
    private String name;
    private String description;
    private String location;
    private Double latitude;
    private Double longitude;
    private Integer totalPlots;
    private Integer availablePlots;
    private BigDecimal pricePerNight;
    private AngularUserDTO owner;
    
    @Builder.Default
    private List<SiteAmenity> amenities = new ArrayList<>();
    
    @Builder.Default
    private List<SiteImage> images = new ArrayList<>();
    
    private BigDecimal rating;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SiteImage {
        private Long id;
        private String url;
        private String description;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SiteAmenity {
        private Long id;
        private String name;
        private String icon;
    }
}

