package tn.esprit.exam.entity;

public enum ReservationStatus {
    PENDING,       // Reservation created, waiting for payment
    CONFIRMED,     // Payment received, reservation confirmed
    CHECKED_IN,    // Guest has checked in
    CHECKED_OUT,   // Guest has checked out
    CANCELLED,     // Reservation cancelled
    COMPLETED      // Reservation completed and reviewed
}
