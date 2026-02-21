package tn.esprit.exam.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tn.esprit.exam.dto.ContractDTO;
import tn.esprit.exam.entity.Contract;
import tn.esprit.exam.entity.ContractStatus;
import tn.esprit.exam.entity.Reservation;
import tn.esprit.exam.exception.ResourceNotFoundException;
import tn.esprit.exam.repository.ContractRepository;
import tn.esprit.exam.repository.ReservationRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractRepository contractRepository;
    private final ReservationRepository reservationRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public List<ContractDTO> getAll() {
        return contractRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ContractDTO getById(@PathVariable Long id) {
        return toDTO(contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found: " + id)));
    }

    @PostMapping
    @Transactional
    public ContractDTO create(@RequestBody ContractDTO dto) {
        Contract contract = new Contract();
        contract.setContractNumber(dto.getContractNumber() != null ? dto.getContractNumber() : "CNT-" + System.currentTimeMillis());
        contract.setTerms(dto.getTerms() != null ? dto.getTerms() : "Standard camping contract terms.");
        contract.setStatus(dto.getStatus() != null ? dto.getStatus() : ContractStatus.DRAFT);
        contract.setIsSigned(dto.getIsSigned() != null ? dto.getIsSigned() : false);
        if (dto.getReservationId() != null) {
            Reservation reservation = reservationRepository.findById(dto.getReservationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + dto.getReservationId()));
            contract.setReservation(reservation);
        }
        return toDTO(contractRepository.save(contract));
    }

    @PutMapping("/{id}")
    @Transactional
    public ContractDTO update(@PathVariable Long id, @RequestBody ContractDTO dto) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found: " + id));
        if (dto.getTerms() != null) contract.setTerms(dto.getTerms());
        if (dto.getStatus() != null) contract.setStatus(dto.getStatus());
        if (dto.getIsSigned() != null) contract.setIsSigned(dto.getIsSigned());
        if (dto.getSignatureUrl() != null) contract.setSignatureUrl(dto.getSignatureUrl());
        return toDTO(contractRepository.save(contract));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!contractRepository.existsById(id))
            throw new ResourceNotFoundException("Contract not found: " + id);
        contractRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private ContractDTO toDTO(Contract c) {
        ContractDTO dto = new ContractDTO();
        dto.setId(c.getId());
        dto.setContractNumber(c.getContractNumber());
        dto.setTerms(c.getTerms());
        dto.setIsSigned(c.getIsSigned());
        dto.setSignedAt(c.getSignedAt());
        dto.setSignatureUrl(c.getSignatureUrl());
        dto.setStatus(c.getStatus());
        if (c.getReservation() != null) {
            dto.setReservationId(c.getReservation().getId());
            dto.setReservationNumber(c.getReservation().getReservationNumber());
        }
        return dto;
    }
}
