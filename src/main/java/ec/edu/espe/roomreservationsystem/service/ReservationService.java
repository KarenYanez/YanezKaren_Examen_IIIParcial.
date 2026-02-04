package ec.edu.espe.roomreservationsystem.service;

import ec.edu.espe.roomreservationsystem.dto.ReservationResponse;
import ec.edu.espe.roomreservationsystem.model.RoomReservation;
import ec.edu.espe.roomreservationsystem.repository.ReservationRepository;

public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserPolicyClient userPolicyClient;

    public ReservationService(ReservationRepository reservationRepository, UserPolicyClient userPolicyClient) {
        this.reservationRepository = reservationRepository;
        this.userPolicyClient = userPolicyClient;
    }

    public ReservationResponse createReservation(String roomCode, String email, int hours) {

        // Validación: roomCode no puede ser nulo ni vacío
        if (roomCode == null || roomCode.trim().isEmpty()) {
            throw new IllegalArgumentException("roomCode cannot be null or empty");
        }

        // Validación: email debe tener un formato válido
        if (email == null || email.trim().isEmpty() || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("invalid email address");
        }

        // Validación: hours debe ser mayor a 0 y menor o igual a 8
        if (hours <= 0 || hours > 8) {
            throw new IllegalArgumentException("hours must be greater than 0 and less than or equal to 8");
        }

        // Validación: No se permite crear reservas para usuarios bloqueados por políticas institucionales
        if (userPolicyClient.isUserBlocked(email)) {
            throw new IllegalStateException("user is blocked by institutional policies");
        }

        // Validación: No se puede crear una reserva si la sala ya se encuentra reservada
        // Validación: No se puede crear una reserva si la sala ya se encuentra reservada
        if (reservationRepository.existsByRoomCode(roomCode)) {
            throw new IllegalStateException("room is already reserved");
        }

        // Crear la reserva
        RoomReservation reservation = new RoomReservation(roomCode, email, hours);

        // Guardar en repositorio
        reservationRepository.save(reservation);

        // Retornar respuesta
        return new ReservationResponse(
                reservation.getId(),
                reservation.getRoomCode(),
                reservation.getReservedByEmail(),
                reservation.getHours(),
                reservation.getStatus().name()
        );
    }
}
