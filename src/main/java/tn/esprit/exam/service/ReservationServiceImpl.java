package tn.esprit.exam.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.exam.dto.ReservationDTO;
import tn.esprit.exam.dto.ReservationEquipmentDTO;
import tn.esprit.exam.entity.*;
import tn.esprit.exam.exception.BusinessLogicException;
import tn.esprit.exam.exception.ResourceNotFoundException;
import tn.esprit.exam.exception.UnauthorizedException;
import tn.esprit.exam.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReservationServiceImpl implements IReservationService {

    private final ReservationRepository reservationRepository;
    private final CampingSiteRepository campingSiteRepository;
    private final UserRepository userRepository;
    private final EquipmentRepository equipmentRepository;
    private final ReservationEquipmentRepository reservationEquipmentRepository;
    private final IEquipmentService equipmentService;

    @Override
    public ReservationDTO createReservation(ReservationDTO dto, Long camperId) {
        log.info("Creating reservation for camper: {}", camperId);

        User camper = userRepository.findById(camperId)
                .orElseThrow(() -> new ResourceNotFoundException("Camper not found with id: " + camperId));

        CampingSite site = campingSiteRepository.findById(dto.getCampingSiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Camping site not found with id: " + dto.getCampingSiteId()));

        // Validate dates
        if (!dto.getCheckOutDate().isAfter(dto.getCheckInDate())) {
            throw new BusinessLogicException("Check-out date must be after check-in date");
        }

        // Check availability
        if (!isDateRangeAvailable(dto.getCampingSiteId(), dto.getCheckInDate(), dto.getCheckOutDate())) {
            throw new BusinessLogicException("Camping site is not available for selected dates");
        }

        // Calculate number of nights
        long nights = ChronoUnit.DAYS.between(dto.getCheckInDate(), dto.getCheckOutDate());
        BigDecimal sitePrice = site.getPricePerNight().multiply(BigDecimal.valueOf(nights));

        Reservation reservation = Reservation.builder()
                .checkInDate(dto.getCheckInDate())
                .checkOutDate(dto.getCheckOutDate())
                .numberOfGuests(dto.getNumberOfGuests())
                .totalPrice(sitePrice)
                .status(ReservationStatus.PENDING)
                .specialRequests(dto.getSpecialRequests())
                .camper(camper)
                .campingSite(site)
                .build();

        Reservation saved = reservationRepository.save(reservation);

        // Add equipment if provided
        BigDecimal equipmentTotal = BigDecimal.ZERO;
        if (dto.getEquipments() != null && !dto.getEquipments().isEmpty()) {
            for (ReservationEquipmentDTO eqDto : dto.getEquipments()) {
                Equipment equipment = equipmentRepository.findById(eqDto.getEquipmentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Equipment not found with id: " + eqDto.getEquipmentId()));

                // Check equipment availability
                if (equipment.getAvailableQuantity() < eqDto.getQuantity()) {
                    throw new BusinessLogicException("Not enough equipment available: " + equipment.getName());
                }

                BigDecimal equipmentSubtotal = equipment.getPricePerDay()
                        .multiply(BigDecimal.valueOf(eqDto.getQuantity()))
                        .multiply(BigDecimal.valueOf(nights));

                ReservationEquipment resEquipment = ReservationEquipment.builder()
                        .quantity(eqDto.getQuantity())
                        .pricePerDay(equipment.getPricePerDay())
                        .subtotal(equipmentSubtotal)
                        .reservation(saved)
                        .equipment(equipment)
                        .build();

                reservationEquipmentRepository.save(resEquipment);

                // Update equipment availability
                equipmentService.updateAvailability(equipment.getId(), -eqDto.getQuantity());

                equipmentTotal = equipmentTotal.add(equipmentSubtotal);
            }
        }

        // Update total price
        saved.setTotalPrice(sitePrice.add(equipmentTotal));
        reservationRepository.save(saved);

        log.info("Reservation created with id: {} and number: {}", saved.getId(), saved.getReservationNumber());

        return mapToDTO(saved);
    }

    @Override
    public ReservationDTO updateReservation(Long id, ReservationDTO dto, Long camperId) {
        log.info("Updating reservation: {} by camper: {}", id, camperId);

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        if (dto.getCheckInDate() != null) reservation.setCheckInDate(dto.getCheckInDate());
        if (dto.getCheckOutDate() != null) reservation.setCheckOutDate(dto.getCheckOutDate());
        if (dto.getNumberOfGuests() != null) reservation.setNumberOfGuests(dto.getNumberOfGuests());
        if (dto.getSpecialRequests() != null) reservation.setSpecialRequests(dto.getSpecialRequests());
        if (dto.getStatus() != null) reservation.setStatus(dto.getStatus());

        Reservation updated = reservationRepository.save(reservation);
        log.info("Reservation updated: {}", updated.getId());

        return mapToDTO(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationDTO getReservationById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));
        return mapToDTO(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationDTO getReservationByNumber(String reservationNumber) {
        Reservation reservation = reservationRepository.findByReservationNumber(reservationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with number: " + reservationNumber));
        return mapToDTO(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationDTO> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationDTO> getReservationsByCamper(Long camperId) {
        User camper = userRepository.findById(camperId)
                .orElseThrow(() -> new ResourceNotFoundException("Camper not found with id: " + camperId));
        return reservationRepository.findByCamperOrderByCreatedAtDesc(camper).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationDTO> getReservationsBySiteOwner(Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found with id: " + ownerId));
        return reservationRepository.findBySiteOwnerOrderByCreatedAtDesc(owner).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationDTO> getReservationsByStatus(ReservationStatus status) {
        return reservationRepository.findByStatus(status).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ReservationDTO updateReservationStatus(Long id, ReservationStatus status) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        reservation.setStatus(status);
        Reservation updated = reservationRepository.save(reservation);
        log.info("Reservation status updated: {} - {}", id, status);

        return mapToDTO(updated);
    }

    @Override
    public void cancelReservation(Long id, Long camperId) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        // Return equipment to available stock
        List<ReservationEquipment> equipments = reservationEquipmentRepository.findByReservation(reservation);
        for (ReservationEquipment resEquipment : equipments) {
            equipmentService.updateAvailability(resEquipment.getEquipment().getId(), resEquipment.getQuantity());
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
        log.info("Reservation cancelled: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDateRangeAvailable(Long campingSiteId, LocalDate checkIn, LocalDate checkOut) {
        CampingSite site = campingSiteRepository.findById(campingSiteId)
                .orElseThrow(() -> new ResourceNotFoundException("Camping site not found with id: " + campingSiteId));

        List<Reservation> conflicts = reservationRepository.findConflictingReservations(site, checkIn, checkOut);
        return conflicts.isEmpty();
    }

    @Override
    public ReservationDTO checkIn(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new BusinessLogicException("Only confirmed reservations can be checked in");
        }

        reservation.setStatus(ReservationStatus.CHECKED_IN);
        Reservation updated = reservationRepository.save(reservation);
        log.info("Reservation checked in: {}", id);

        return mapToDTO(updated);
    }

    @Override
    public ReservationDTO checkOut(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        if (reservation.getStatus() != ReservationStatus.CHECKED_IN) {
            throw new BusinessLogicException("Only checked-in reservations can be checked out");
        }

        reservation.setStatus(ReservationStatus.CHECKED_OUT);
        Reservation updated = reservationRepository.save(reservation);
        log.info("Reservation checked out: {}", id);

        return mapToDTO(updated);
    }

    private ReservationDTO mapToDTO(Reservation reservation) {
        List<ReservationEquipmentDTO> equipmentDTOs = reservation.getEquipments() != null
                ? reservation.getEquipments().stream().map(this::mapEquipmentToDTO).collect(Collectors.toList())
                : List.of();

        CampingSite site = reservation.getCampingSite();
        User camper = reservation.getCamper();

        return ReservationDTO.builder()
                .id(reservation.getId())
                .reservationNumber(reservation.getReservationNumber())
                .checkInDate(reservation.getCheckInDate())
                .checkOutDate(reservation.getCheckOutDate())
                .numberOfGuests(reservation.getNumberOfGuests())
                .totalPrice(reservation.getTotalPrice())
                .status(reservation.getStatus())
                .specialRequests(reservation.getSpecialRequests())
                .campingSiteId(site != null ? site.getId() : null)
                .campingSiteName(site != null ? site.getName() : "N/A")
                .campingSiteLocation(site != null ? site.getLocation() : "")
                .camperId(camper != null ? camper.getId() : null)
                .camperName(camper != null ? camper.getFirstName() + " " + camper.getLastName() : "Anonymous")
                .camperEmail(camper != null ? camper.getEmail() : "")
                .equipments(equipmentDTOs)
                .contractId(reservation.getContract() != null ? reservation.getContract().getId() : null)
                .build();
    }

    private ReservationEquipmentDTO mapEquipmentToDTO(ReservationEquipment resEquipment) {
        return ReservationEquipmentDTO.builder()
                .id(resEquipment.getId())
                .equipmentId(resEquipment.getEquipment().getId())
                .equipmentName(resEquipment.getEquipment().getName())
                .equipmentCategory(resEquipment.getEquipment().getCategory())
                .quantity(resEquipment.getQuantity())
                .pricePerDay(resEquipment.getPricePerDay())
                .subtotal(resEquipment.getSubtotal())
                .build();
    }
}
