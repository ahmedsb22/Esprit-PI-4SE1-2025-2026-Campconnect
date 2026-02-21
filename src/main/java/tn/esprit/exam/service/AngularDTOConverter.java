package tn.esprit.exam.service;

import org.springframework.stereotype.Service;
import tn.esprit.exam.dto.angular.*;
import tn.esprit.exam.entity.*;
import tn.esprit.exam.entity.Equipment;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to convert entities to Angular-compatible DTOs
 */
@Service
public class AngularDTOConverter {

    public AngularUserDTO toAngularUser(User user) {
        if (user == null) return null;
        
        String role = "CAMPER";
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            role = user.getRoles().iterator().next().getName().name();
        }
        
        return AngularUserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .profileImage(user.getProfileImage())
                .role(role)
                .createdAt(user.getCreatedAt() != null 
                    ? LocalDateTime.ofInstant(user.getCreatedAt(), ZoneId.systemDefault()) 
                    : null)
                .updatedAt(user.getUpdatedAt() != null 
                    ? LocalDateTime.ofInstant(user.getUpdatedAt(), ZoneId.systemDefault()) 
                    : null)
                .build();
    }

    public AngularSiteDTO toAngularSite(CampingSite site) {
        if (site == null) return null;
        
        List<AngularSiteDTO.SiteAmenity> amenities = new ArrayList<>();
        
        // Convert boolean amenities to list
        if (Boolean.TRUE.equals(site.getHasWifi())) {
            amenities.add(AngularSiteDTO.SiteAmenity.builder()
                    .id(1L).name("WiFi").icon("wifi").build());
        }
        if (Boolean.TRUE.equals(site.getHasParking())) {
            amenities.add(AngularSiteDTO.SiteAmenity.builder()
                    .id(2L).name("Parking").icon("local_parking").build());
        }
        if (Boolean.TRUE.equals(site.getHasRestrooms())) {
            amenities.add(AngularSiteDTO.SiteAmenity.builder()
                    .id(3L).name("Restrooms").icon("wc").build());
        }
        if (Boolean.TRUE.equals(site.getHasShowers())) {
            amenities.add(AngularSiteDTO.SiteAmenity.builder()
                    .id(4L).name("Showers").icon("shower").build());
        }
        if (Boolean.TRUE.equals(site.getHasElectricity())) {
            amenities.add(AngularSiteDTO.SiteAmenity.builder()
                    .id(5L).name("Electricity").icon("bolt").build());
        }
        if (Boolean.TRUE.equals(site.getHasPetFriendly())) {
            amenities.add(AngularSiteDTO.SiteAmenity.builder()
                    .id(6L).name("Pet Friendly").icon("pets").build());
        }
        
        List<AngularSiteDTO.SiteImage> images = new ArrayList<>();
        if (site.getImageUrl() != null && !site.getImageUrl().isEmpty()) {
            images.add(AngularSiteDTO.SiteImage.builder()
                    .id(1L)
                    .url(site.getImageUrl())
                    .description("Main image")
                    .build());
        }
        
        return AngularSiteDTO.builder()
                .id(site.getId())
                .name(site.getName())
                .description(site.getDescription())
                .location(site.getLocation())
                .latitude(0.0) // Not stored in entity
                .longitude(0.0) // Not stored in entity
                .totalPlots(site.getCapacity())
                .availablePlots(site.getCapacity()) // Simplified
                .pricePerNight(site.getPricePerNight())
                .owner(toAngularUser(site.getOwner()))
                .amenities(amenities)
                .images(images)
                .rating(site.getRating())
                .status(Boolean.TRUE.equals(site.getIsActive()) ? "ACTIVE" : "INACTIVE")
                .createdAt(site.getCreatedAt())
                .updatedAt(site.getUpdatedAt())
                .build();
    }

    public AngularBookingDTO toAngularBooking(Reservation reservation) {
        if (reservation == null) return null;
        
        return AngularBookingDTO.builder()
                .id(reservation.getId())
                .camper(toAngularUser(reservation.getCamper()))
                .site(toAngularSite(reservation.getCampingSite()))
                .startDate(reservation.getCheckInDate())
                .endDate(reservation.getCheckOutDate())
                .numberOfGuests(reservation.getNumberOfGuests())
                .totalPrice(reservation.getTotalPrice())
                .status(reservation.getStatus() != null ? reservation.getStatus().name() : "PENDING")
                .specialRequests(reservation.getSpecialRequests())
                .createdAt(reservation.getCreatedAt())
                .cancelledAt(reservation.getStatus() == ReservationStatus.CANCELLED 
                    ? reservation.getUpdatedAt() : null)
                .invoiceId(reservation.getContract() != null ? reservation.getContract().getId() : null)
                .build();
    }

    public AngularAuthResponseDTO toAngularAuthResponse(User user, String token) {
        return AngularAuthResponseDTO.builder()
                .token(token)
                .user(toAngularUser(user))
                .build();
    }

    public AngularEquipmentDTO toAngularEquipment(Equipment equipment) {
        if (equipment == null) return null;

        List<String> images = new ArrayList<>();
        if (equipment.getImageUrl() != null && !equipment.getImageUrl().isEmpty()) {
            images.add(equipment.getImageUrl());
        }

        // Create a minimal site DTO to avoid circular reference
        AngularSiteDTO siteDTO = null;
        if (equipment.getProvider() != null) {
            // Equipment doesn't have a direct site relationship, but we can create a placeholder
            siteDTO = AngularSiteDTO.builder()
                    .id(0L)
                    .name("Provider: " + equipment.getProvider().getFullName())
                    .build();
        }

        return AngularEquipmentDTO.builder()
                .id(equipment.getId())
                .name(equipment.getName())
                .description(equipment.getDescription())
                .category(equipment.getCategory())
                .price(equipment.getPricePerDay())
                .stock(equipment.getStockQuantity())
                .images(images)
                .site(siteDTO)
                .createdAt(equipment.getCreatedAt())
                .updatedAt(equipment.getUpdatedAt())
                .build();
    }
}

