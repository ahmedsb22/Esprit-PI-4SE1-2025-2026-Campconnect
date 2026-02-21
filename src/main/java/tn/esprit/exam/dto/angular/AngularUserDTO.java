package tn.esprit.exam.dto.angular;

import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO matching Angular's User interface
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AngularUserDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private String profileImage;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

