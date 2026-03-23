package tn.esprit.exam.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
    name = "Contracts",
    description = "Gestion des contrats de camping - CRUD et signature"
)
@RequiredArgsConstructor
public class ContractController {

    private final ContractRepository contractRepository;
    private final ReservationRepository reservationRepository;

    @GetMapping
    @Operation(
        summary = "Récupérer tous les contrats",
        description = "Retourne la liste complète de tous les contrats du système"
    )
    @ApiResponse(responseCode = "200", description = "Liste des contrats")
    @Transactional(readOnly = true)
    public List<ContractDTO> getAll() {
        return contractRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Récupérer un contrat par ID",
        description = "Retourne les détails complets d'un contrat spécifique"
    )
    @ApiResponse(responseCode = "200", description = "Contrat trouvé")
    @ApiResponse(responseCode = "404", description = "Contrat non trouvé")
    @Transactional(readOnly = true)
    public ContractDTO getById(
            @PathVariable
            @Parameter(description = "ID du contrat", example = "1")
            Long id) {
        return toDTO(contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found: " + id)));
    }

    @PostMapping
    @Operation(
        summary = "Créer un nouveau contrat",
        description = "Crée un nouveau contrat associé à une réservation"
    )
    @ApiResponse(responseCode = "201", description = "Contrat créé avec succès")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    @ApiResponse(responseCode = "404", description = "Réservation non trouvée")
    @Transactional
    public ContractDTO create(
            @RequestBody
            @Parameter(description = "Données du nouveau contrat (contractNumber, terms, status, reservationId)")
            ContractDTO dto) {
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
    @Operation(
        summary = "Mettre à jour un contrat",
        description = "Met à jour les informations d'un contrat existant (termes, statut, signature)"
    )
    @ApiResponse(responseCode = "200", description = "Contrat mis à jour")
    @ApiResponse(responseCode = "404", description = "Contrat non trouvé")
    @Transactional
    public ContractDTO update(
            @PathVariable
            @Parameter(description = "ID du contrat à mettre à jour", example = "1")
            Long id,
            @RequestBody
            @Parameter(description = "Données mises à jour du contrat (terms, status, isSigned, signatureUrl)")
            ContractDTO dto) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found: " + id));
        if (dto.getTerms() != null) contract.setTerms(dto.getTerms());
        if (dto.getStatus() != null) contract.setStatus(dto.getStatus());
        if (dto.getIsSigned() != null) contract.setIsSigned(dto.getIsSigned());
        if (dto.getSignatureUrl() != null) contract.setSignatureUrl(dto.getSignatureUrl());
        return toDTO(contractRepository.save(contract));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Supprimer un contrat",
        description = "Supprime définitivement un contrat du système"
    )
    @ApiResponse(responseCode = "204", description = "Contrat supprimé")
    @ApiResponse(responseCode = "404", description = "Contrat non trouvé")
    @Transactional
    public ResponseEntity<Void> delete(
            @PathVariable
            @Parameter(description = "ID du contrat à supprimer", example = "1")
            Long id) {
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
