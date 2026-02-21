package tn.esprit.exam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.exam.entity.Contract;
import tn.esprit.exam.entity.ContractStatus;
import tn.esprit.exam.entity.Reservation;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    Optional<Contract> findByReservation(Reservation reservation);

    Optional<Contract> findByContractNumber(String contractNumber);

    List<Contract> findByStatus(ContractStatus status);

    List<Contract> findByIsSigned(Boolean isSigned);

    @Query("SELECT c FROM Contract c WHERE c.reservation.camper.id = :camperId")
    List<Contract> findByCamperId(@Param("camperId") Long camperId);

    @Query("SELECT c FROM Contract c WHERE c.reservation.campingSite.owner.id = :ownerId")
    List<Contract> findByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT COUNT(c) FROM Contract c WHERE c.status = :status")
    long countByStatus(@Param("status") ContractStatus status);
}
