package tn.esprit.exam.dto.angular;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * DTO matching Angular's Equipment interface
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AngularEquipmentDTO {
    private Long id;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private Integer stock;
    
    @Builder.Default
    private List<String> images = new ArrayList<>();
    
    private AngularSiteDTO site;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

