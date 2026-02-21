package tn.esprit.exam.entity;

public enum ContractStatus {
    DRAFT,      // Contract created but not sent
    PENDING,    // Contract sent, waiting for signature
    SIGNED,     // Contract signed by camper
    ACTIVE,     // Contract is currently active (during reservation period)
    COMPLETED,  // Contract completed (after checkout)
    CANCELLED   // Contract cancelled
}
