package tn.esprit.exam.service;

import tn.esprit.exam.dto.ContractDTO;

import java.util.List;

public interface IContractService {
    ContractDTO createContract(Long reservationId);
    ContractDTO getContractById(Long id);
    ContractDTO getContractByReservation(Long reservationId);
    ContractDTO getContractByNumber(String contractNumber);
    List<ContractDTO> getAllContracts();
    List<ContractDTO> getContractsByCamper(Long camperId);
    List<ContractDTO> getContractsByOwner(Long ownerId);
    ContractDTO signContract(Long id, String signatureUrl);
    ContractDTO updateContractTerms(Long id, String terms);
}
