package tn.esprit.exam.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.exam.dto.ContractDTO;
import tn.esprit.exam.entity.Contract;
import tn.esprit.exam.entity.ContractStatus;
import tn.esprit.exam.entity.Reservation;
import tn.esprit.exam.exception.BusinessLogicException;
import tn.esprit.exam.exception.ResourceNotFoundException;
import tn.esprit.exam.repository.ContractRepository;
import tn.esprit.exam.repository.ReservationRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ContractServiceImpl implements IContractService {

    private final ContractRepository contractRepository;
    private final ReservationRepository reservationRepository;

    @Override
    public ContractDTO createContract(Long reservationId) {
        log.info("Creating contract for reservation: {}", reservationId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + reservationId));

        // Check if contract already exists
        if (contractRepository.findByReservation(reservation).isPresent()) {
            throw new BusinessLogicException("Contract already exists for this reservation");
        }

        // Generate contract terms
        String terms = generateContractTerms(reservation);

        Contract contract = Contract.builder()
                .terms(terms)
                .isSigned(false)
                .status(ContractStatus.DRAFT)
                .reservation(reservation)
                .build();

        Contract saved = contractRepository.save(contract);
        log.info("Contract created with id: {} and number: {}", saved.getId(), saved.getContractNumber());

        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ContractDTO getContractById(Long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + id));
        return mapToDTO(contract);
    }

    @Override
    @Transactional(readOnly = true)
    public ContractDTO getContractByReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + reservationId));

        Contract contract = contractRepository.findByReservation(reservation)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found for reservation: " + reservationId));

        return mapToDTO(contract);
    }

    @Override
    @Transactional(readOnly = true)
    public ContractDTO getContractByNumber(String contractNumber) {
        Contract contract = contractRepository.findByContractNumber(contractNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with number: " + contractNumber));
        return mapToDTO(contract);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractDTO> getAllContracts() {
        return contractRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractDTO> getContractsByCamper(Long camperId) {
        return contractRepository.findByCamperId(camperId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractDTO> getContractsByOwner(Long ownerId) {
        return contractRepository.findByOwnerId(ownerId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ContractDTO signContract(Long id, String signatureUrl) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + id));

        if (contract.getIsSigned()) {
            throw new BusinessLogicException("Contract is already signed");
        }

        contract.setSignatureUrl(signatureUrl);
        contract.sign();

        Contract signed = contractRepository.save(contract);
        log.info("Contract signed: {}", id);

        return mapToDTO(signed);
    }

    @Override
    public ContractDTO updateContractTerms(Long id, String terms) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + id));

        if (contract.getIsSigned()) {
            throw new BusinessLogicException("Cannot update signed contract");
        }

        contract.setTerms(terms);
        Contract updated = contractRepository.save(contract);
        log.info("Contract terms updated: {}", id);

        return mapToDTO(updated);
    }

    private String generateContractTerms(Reservation reservation) {
        return String.format("""
                CAMPING SITE RENTAL CONTRACT
                
                Contract Number: [Auto-generated]
                Reservation Number: %s
                
                PARTIES:
                Provider: %s
                Camping Site: %s
                Location: %s
                
                Camper: %s
                Email: %s
                
                RENTAL PERIOD:
                Check-in Date: %s
                Check-out Date: %s
                Number of Guests: %d
                
                FINANCIAL TERMS:
                Total Price: %s TND
                
                TERMS AND CONDITIONS:
                1. The camper agrees to respect the camping site rules and regulations.
                2. Check-in time is from 14:00 and check-out time is before 12:00.
                3. The camper is responsible for any damages caused during the stay.
                4. Quiet hours are from 22:00 to 07:00.
                5. Pets are %s on this camping site.
                6. Cancellation must be made at least 48 hours before check-in for a full refund.
                7. The provider reserves the right to refuse entry for non-compliance with rules.
                
                EQUIPMENT RENTAL:
                %s
                
                By signing this contract, both parties agree to the terms and conditions stated above.
                
                Date: [Auto-generated upon signature]
                """,
                reservation.getReservationNumber(),
                reservation.getCampingSite().getOwner().getFirstName() + " " +
                        reservation.getCampingSite().getOwner().getLastName(),
                reservation.getCampingSite().getName(),
                reservation.getCampingSite().getLocation(),
                reservation.getCamper().getFirstName() + " " + reservation.getCamper().getLastName(),
                reservation.getCamper().getEmail(),
                reservation.getCheckInDate(),
                reservation.getCheckOutDate(),
                reservation.getNumberOfGuests(),
                reservation.getTotalPrice(),
                reservation.getCampingSite().getHasPetFriendly() ? "allowed" : "not allowed",
                reservation.getEquipments().isEmpty() ? "No equipment rented" :
                        reservation.getEquipments().stream()
                                .map(e -> String.format("- %s x%d @ %s TND/day",
                                        e.getEquipment().getName(),
                                        e.getQuantity(),
                                        e.getPricePerDay()))
                                .collect(Collectors.joining("\n"))
        );
    }

    private ContractDTO mapToDTO(Contract contract) {
        return ContractDTO.builder()
                .id(contract.getId())
                .contractNumber(contract.getContractNumber())
                .terms(contract.getTerms())
                .isSigned(contract.getIsSigned())
                .signedAt(contract.getSignedAt())
                .signatureUrl(contract.getSignatureUrl())
                .status(contract.getStatus())
                .reservationId(contract.getReservation().getId())
                .reservationNumber(contract.getReservation().getReservationNumber())
                .build();
    }
}
