package tn.esprit.exam.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.exam.dto.ReservationDTO;
import tn.esprit.exam.entity.ReservationStatus;
import tn.esprit.exam.service.IReservationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@Tag(
    name = "Bookings",
    description = "Gestion complète des réservations (bookings) - création, modification, suivi du statut"
)
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final IReservationService reservationService;

    // Formateurs de dates supportés
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter FR_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @GetMapping
    @Operation(
        summary = "Récupérer toutes les réservations",
        description = "Retourne la liste complète de toutes les réservations du système"
    )
    @ApiResponse(responseCode = "200", description = "Liste des réservations")
    public List<ReservationDTO> getAll() {
        return reservationService.getAllReservations();
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Récupérer une réservation par ID",
        description = "Retourne les détails complets d'une réservation spécifique"
    )
    @ApiResponse(responseCode = "200", description = "Réservation trouvée")
    @ApiResponse(responseCode = "404", description = "Réservation non trouvée")
    public ResponseEntity<?> getById(
            @PathVariable
            @Parameter(description = "ID unique de la réservation", example = "1")
            Long id) {
        try {
            ReservationDTO dto = reservationService.getReservationById(id);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching reservation {}", id, e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to fetch reservation"));
        }
    }

    @PostMapping
    @Operation(
        summary = "Créer une nouvelle réservation",
        description = "Crée une nouvelle réservation pour un camping avec les dates et le nombre de clients"
    )
    @ApiResponse(responseCode = "201", description = "Réservation créée avec succès")
    @ApiResponse(responseCode = "400", description = "Données invalides ou manquantes")
    @ApiResponse(responseCode = "404", description = "Site ou client non trouvé")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> payload) {
        try {
            log.info("Received booking request: {}", payload);

            ReservationDTO dto = new ReservationDTO();

            // campingSiteId
            Object campingSiteObj = getNestedValue(payload, "campingSiteId", "campingSite", "id");
            if (campingSiteObj != null) {
                dto.setCampingSiteId(parseLong(campingSiteObj, "campingSiteId"));
            }
            if (dto.getCampingSiteId() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Validation failed",
                        "field", "campingSiteId",
                        "message", "campingSiteId is required"
                ));
            }

            // camperId
            Long camperId = 1L;
            Object camperObj = getNestedValue(payload, "camperId", "camper", "id");
            if (camperObj != null) {
                camperId = parseLong(camperObj, "camperId");
            }

            // checkInDate
            Object checkInObj = payload.get("checkInDate");
            if (checkInObj != null) {
                dto.setCheckInDate(parseLocalDate(checkInObj.toString(), "checkInDate"));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Validation failed",
                        "field", "checkInDate",
                        "message", "checkInDate is required"
                ));
            }

            // checkOutDate
            Object checkOutObj = payload.get("checkOutDate");
            if (checkOutObj != null) {
                dto.setCheckOutDate(parseLocalDate(checkOutObj.toString(), "checkOutDate"));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Validation failed",
                        "field", "checkOutDate",
                        "message", "checkOutDate is required"
                ));
            }

            // numberOfGuests
            Object guestsObj = payload.get("numberOfGuests");
            if (guestsObj != null) {
                Integer guests = parseInt(guestsObj, "numberOfGuests");
                if (guests < 1) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "error", "Validation failed",
                            "field", "numberOfGuests",
                            "message", "numberOfGuests must be at least 1"
                    ));
                }
                dto.setNumberOfGuests(guests);
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Validation failed",
                        "field", "numberOfGuests",
                        "message", "numberOfGuests is required"
                ));
            }

            // Champs optionnels
            if (payload.containsKey("specialRequests") && payload.get("specialRequests") != null) {
                dto.setSpecialRequests(payload.get("specialRequests").toString());
            }

            if (payload.containsKey("totalPrice") && payload.get("totalPrice") != null) {
                try {
                    dto.setTotalPrice(new BigDecimal(payload.get("totalPrice").toString()));
                } catch (NumberFormatException e) {
                    log.warn("Invalid totalPrice format: {}", payload.get("totalPrice"));
                }
            }

            // Status
            String statusStr = payload.containsKey("status") ? payload.get("status").toString() : "PENDING";
            try {
                dto.setStatus(ReservationStatus.valueOf(statusStr.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Unknown status '{}', defaulting to PENDING", statusStr);
                dto.setStatus(ReservationStatus.PENDING);
            }

            ReservationDTO created = reservationService.createReservation(dto, camperId);
            log.info("Reservation created successfully: {}", created.getId());
            return ResponseEntity.status(201).body(created);

        } catch (IllegalArgumentException e) {
            log.warn("Booking validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid request",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Unexpected error while creating booking", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Internal server error",
                    "message", "Please try again later"
            ));
        }
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Mettre à jour une réservation",
        description = "Modifie les détails d'une réservation existante (dates, nombre d'hôtes, etc.)"
    )
    @ApiResponse(responseCode = "200", description = "Réservation mise à jour")
    @ApiResponse(responseCode = "404", description = "Réservation non trouvée")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    public ResponseEntity<?> update(
            @PathVariable
            @Parameter(description = "ID de la réservation", example = "1")
            Long id,
            @RequestBody Map<String, Object> payload) {
        try {
            log.info("Updating reservation {}: {}", id, payload);

            ReservationDTO dto = reservationService.getReservationById(id);
            if (dto == null) {
                return ResponseEntity.notFound().build();
            }

            if (payload.containsKey("checkInDate") && payload.get("checkInDate") != null) {
                dto.setCheckInDate(parseLocalDate(payload.get("checkInDate").toString(), "checkInDate"));
            }

            if (payload.containsKey("checkOutDate") && payload.get("checkOutDate") != null) {
                dto.setCheckOutDate(parseLocalDate(payload.get("checkOutDate").toString(), "checkOutDate"));
            }

            if (payload.containsKey("numberOfGuests") && payload.get("numberOfGuests") != null) {
                Integer guests = parseInt(payload.get("numberOfGuests"), "numberOfGuests");
                if (guests < 1) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "error", "Validation failed",
                            "field", "numberOfGuests",
                            "message", "numberOfGuests must be at least 1"
                    ));
                }
                dto.setNumberOfGuests(guests);
            }

            if (payload.containsKey("specialRequests")) {
                dto.setSpecialRequests(payload.get("specialRequests") != null ?
                        payload.get("specialRequests").toString() : null);
            }

            if (payload.containsKey("status") && payload.get("status") != null) {
                try {
                    dto.setStatus(ReservationStatus.valueOf(payload.get("status").toString().trim().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    log.warn("Unknown status '{}', ignoring update", payload.get("status"));
                }
            }

            ReservationDTO updated = reservationService.updateReservation(id, dto, dto.getCamperId());
            log.info("Reservation {} updated successfully", id);
            return ResponseEntity.ok(updated);

        } catch (IllegalArgumentException e) {
            log.warn("Update validation error for reservation {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error while updating reservation {}", id, e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to update reservation"));
        }
    }

    @PutMapping("/{id}/status")
    @Operation(
        summary = "Changer le statut d'une réservation",
        description = "Change le statut d'une réservation (PENDING, CONFIRMED, CANCELLED, COMPLETED, etc.)"
    )
    @ApiResponse(responseCode = "200", description = "Statut mis à jour")
    @ApiResponse(responseCode = "404", description = "Réservation non trouvée")
    @ApiResponse(responseCode = "400", description = "Statut invalide")
    public ResponseEntity<?> updateStatus(
            @PathVariable
            @Parameter(description = "ID de la réservation", example = "1")
            Long id,
            @RequestParam
            @Parameter(description = "Nouveau statut (PENDING, CONFIRMED, CANCELLED, COMPLETED)", example = "CONFIRMED")
            String status) {
        try {
            log.info("Updating status of reservation {} to {}", id, status);
            ReservationStatus newStatus = ReservationStatus.valueOf(status.trim().toUpperCase());
            ReservationDTO updated = reservationService.updateReservationStatus(id, newStatus);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid status '{}' for reservation {}", status, id);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid status",
                    "message", "Valid statuses: " + String.join(", ",
                            java.util.Arrays.stream(ReservationStatus.values())
                                    .map(Enum::name).toArray(String[]::new))
            ));
        } catch (Exception e) {
            log.error("Error updating status for reservation {}", id, e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to update status"));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Annuler/Supprimer une réservation",
        description = "Annule ou supprime une réservation existante"
    )
    @ApiResponse(responseCode = "204", description = "Réservation annulée avec succès")
    @ApiResponse(responseCode = "404", description = "Réservation non trouvée")
    @ApiResponse(responseCode = "400", description = "Impossible d'annuler cette réservation")
    public ResponseEntity<Void> delete(
            @PathVariable
            @Parameter(description = "ID de la réservation à annuler", example = "1")
            Long id) {
        try {
            log.info("Deleting reservation {}", id);
            ReservationDTO dto = reservationService.getReservationById(id);
            if (dto == null) {
                return ResponseEntity.notFound().build();
            }
            reservationService.cancelReservation(id, dto.getCamperId());
            log.info("Reservation {} deleted successfully", id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Cannot delete reservation {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error deleting reservation {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== HELPERS DE PARSING SÉCURISÉS ====================

    /**
     * Extrait une valeur pouvant être nested: payload.campingSiteId OU payload.campingSite.id
     * Version compatible Java 8+ (sans pattern matching)
     */
    private Object getNestedValue(Map<String, Object> payload, String directKey, String nestedKey, String nestedField) {
        // Essayer d'abord la clé directe
        if (payload.containsKey(directKey) && payload.get(directKey) != null) {
            return payload.get(directKey);
        }
        // Fallback: chercher dans l'objet nested
        if (payload.containsKey(nestedKey)) {
            Object nestedObj = payload.get(nestedKey);
            if (nestedObj instanceof Map) {
                Map<?, ?> nested = (Map<?, ?>) nestedObj;
                return nested.get(nestedField);
            }
        }
        return null;
    }

    /**
     * Parse un Long de façon sécurisée avec message d'erreur explicite
     */
    private Long parseLong(Object value, String fieldName) {
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.valueOf(value.toString().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    String.format("Invalid number format for '%s': expected a number, got '%s'", fieldName, value));
        }
    }

    /**
     * Parse un Integer de façon sécurisée avec message d'erreur explicite
     */
    private Integer parseInt(Object value, String fieldName) {
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.valueOf(value.toString().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    String.format("Invalid integer format for '%s': expected a number, got '%s'", fieldName, value));
        }
    }

    /**
     * Parse un LocalDate avec support multi-format: ISO ("2026-03-15") et FR ("15/03/2026")
     */
    private LocalDate parseLocalDate(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(String.format("'%s' cannot be empty", fieldName));
        }

        String trimmed = value.trim();

        // Essayer format ISO d'abord (standard API)
        try {
            return LocalDate.parse(trimmed, ISO_FORMATTER);
        } catch (DateTimeParseException e) {
            // Ignorer, essayer le format FR
        }

        // Essayer format français
        try {
            return LocalDate.parse(trimmed, FR_FORMATTER);
        } catch (DateTimeParseException e) {
            // Échec final
            throw new IllegalArgumentException(String.format(
                    "Invalid date format for '%s': '%s'. Expected: YYYY-MM-DD or dd/MM/yyyy",
                    fieldName, value));
        }
    }
}