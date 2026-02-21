package tn.esprit.exam.dto.angular;

import lombok.*;

/**
 * DTO matching Angular's AuthResponse interface
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AngularAuthResponseDTO {
    private String token;
    private AngularUserDTO user;
}

