package ec.edu.espe.roomreservationsystem.model;

import java.util.UUID;

public class RoomReservation {

    private final String id;
    private final String roomCode;
    private final String reservedByEmail;
    private final int hours;
    private ReservationStatus status;


    //Constructor
    public RoomReservation(String roomCode, String reservedByEmail, int hours) {
        this.id = UUID.randomUUID().toString();
        this.roomCode = roomCode;
        this.reservedByEmail = reservedByEmail;
        this.hours = hours;
        this.status = ReservationStatus.CREATED;
    }

    //Getters
    public String getId() {
        return id;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public String getReservedByEmail() {
        return reservedByEmail;
    }

    public int getHours() {
        return hours;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void confirm() {
        this.status = ReservationStatus.CONFIRMED;
    }

    //Estado de reserva
    public enum ReservationStatus {
        CREATED,
        CONFIRMED

    }
}
