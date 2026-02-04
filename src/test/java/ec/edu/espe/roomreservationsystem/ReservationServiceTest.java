package ec.edu.espe.roomreservationsystem;

import ec.edu.espe.roomreservationsystem.dto.ReservationResponse;
import ec.edu.espe.roomreservationsystem.model.RoomReservation;
import ec.edu.espe.roomreservationsystem.repository.ReservationRepository;
import ec.edu.espe.roomreservationsystem.service.ReservationService;
import ec.edu.espe.roomreservationsystem.service.UserPolicyClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReservationServiceTest {

    private ReservationRepository reservationRepository;
    private UserPolicyClient userPolicyClient;
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationRepository = mock(ReservationRepository.class);
        userPolicyClient = mock(UserPolicyClient.class);
        reservationService = new ReservationService(reservationRepository, userPolicyClient);
    }


    //Prueba 1: Creación exitosa de una reserva con datos válidos.
    @Test
    void createReservation_validData_shouldSaveAndReturnResponse() {
        // =========================
        // ARRANGE
        // =========================
        String roomCode = "LAB-101";
        String email = "kayanez2@espe.edu.ec";
        int hours = 3;

        // Room NO está reservada previamente
        when(reservationRepository.existsByRoomCode(roomCode)).thenReturn(false);

        // Usuario NO está bloqueado
        when(userPolicyClient.isUserBlocked(email)).thenReturn(false);

        // Simular guardado
        doAnswer(invocation -> invocation.getArgument(0)).when(reservationRepository).save(ArgumentMatchers.any(RoomReservation.class));

        // ACT
        ReservationResponse response = reservationService.createReservation(roomCode, email, hours);

        // ASSERT
        assertNotNull(response);
        assertNotNull(response.getReservationId());
        assertEquals(roomCode, response.getRoomCode());
        assertEquals(email, response.getReservedByEmail());
        assertEquals(hours, response.getHours());
        assertEquals("CREATED", response.getStatus());

        // VERIFY
        verify(userPolicyClient, times(1)).isUserBlocked(email);
        verify(reservationRepository, times(1)).existsByRoomCode(roomCode);
        verify(reservationRepository, times(1)).save(any(RoomReservation.class));
    }


    //Prueba 2: Error por correo electrónico inválido.
    @Test
    void createReservation_invalidEmail_shouldThrowAndNotCallDependencies() {
        // ARRANGE
        String roomCode = "LAB-101";
        String invalidEmail = "kayanez2@espe*edu.ec"; // email mal escrito
        int hours = 3;

        // ACT + ASSERT
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reservationService.createReservation(roomCode, invalidEmail, hours)
        );

        assertEquals("invalid email address", exception.getMessage());

        // VERIFY (no se llaman dependencias)
        verifyNoInteractions(userPolicyClient);
        verifyNoInteractions(reservationRepository);
    }


    //Prueba 3: Error por número de horas fuera del rango permitido.
    @Test
    void createReservation_hoursOutOfRange_shouldThrowAndNotCallDependencies() {
        // ARRANGE
        String roomCode = "LAB-101";
        String email = "kayanez2@espe.edu.ec";
        int invalidHours = -1; // menor a cero

        // ACT + ASSERT
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reservationService.createReservation(roomCode, email, invalidHours)
        );

        assertEquals("hours must be greater than 0 and less than or equal to 8", exception.getMessage());

        // VERIFY (no se llaman dependencias)
        verifyNoInteractions(userPolicyClient);
        verifyNoInteractions(reservationRepository);
    }


    //Prueba 4: Error cuando la sala ya se encuentra reservada.
    @Test
    void createReservation_roomAlreadyReserved_shouldThrow() {
        // ARRANGE
        String roomCode = "LAB-101";
        String email = "kayanez2@espe.edu.ec";
        int hours = 3;

        // Usuario NO está bloqueado
        //Devuelve
        when(userPolicyClient.isUserBlocked(email)).thenReturn(false);

        // Room YA está reservada
        when(reservationRepository.existsByRoomCode(roomCode)).thenReturn(true);

        // ACT + ASSERT
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> reservationService.createReservation(roomCode, email, hours)
        );

        assertEquals("room is already reserved", exception.getMessage());

        // VERIFY
        verify(userPolicyClient, times(1)).isUserBlocked(email);
        verify(reservationRepository, times(1)).existsByRoomCode(roomCode);
        verify(reservationRepository, never()).save(any(RoomReservation.class));
    }

    @Test
    void createReservation_userBlocked_shouldThrow() {
        // ARRANGE
        String roomCode = "LAB-101";
        String email = "blocked@espe.edu.ec";
        int hours = 3;

        // Usuario SÍ está bloqueado
        when(userPolicyClient.isUserBlocked(email)).thenReturn(true);

        // ACT + ASSERT
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> reservationService.createReservation(roomCode, email, hours)
        );

        assertEquals("user is blocked by institutional policies", exception.getMessage());

        // VERIFY
        verify(userPolicyClient, times(1)).isUserBlocked(email);
        verify(reservationRepository, never()).existsByRoomCode(anyString());
        verify(reservationRepository, never()).save(any(RoomReservation.class));
    }

    @Test
    void createReservation_nullRoomCode_shouldThrowAndNotCallDependencies() {
        // ARRANGE
        String roomCode = null;
        String email = "student@espe.edu.ec";
        int hours = 3;

        // ACT + ASSERT
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reservationService.createReservation(roomCode, email, hours)
        );

        assertEquals("roomCode cannot be null or empty", exception.getMessage());

        // VERIFY (no se llaman dependencias)
        verifyNoInteractions(userPolicyClient);
        verifyNoInteractions(reservationRepository);
    }

    @Test
    void createReservation_emptyRoomCode_shouldThrowAndNotCallDependencies() {
        // ARRANGE
        String roomCode = "";
        String email = "student@espe.edu.ec";
        int hours = 3;

        // ACT + ASSERT
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reservationService.createReservation(roomCode, email, hours)
        );

        assertEquals("roomCode cannot be null or empty", exception.getMessage());

        // VERIFY (no se llaman dependencias)
        verifyNoInteractions(userPolicyClient);
        verifyNoInteractions(reservationRepository);
    }

    @Test
    void createReservation_zeroHours_shouldThrowAndNotCallDependencies() {
        // ARRANGE
        String roomCode = "LAB-101";
        String email = "student@espe.edu.ec";
        int hours = 0;

        // ACT + ASSERT
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reservationService.createReservation(roomCode, email, hours)
        );

        assertEquals("hours must be greater than 0 and less than or equal to 8", exception.getMessage());

        // VERIFY (no se llaman dependencias)
        verifyNoInteractions(userPolicyClient);
        verifyNoInteractions(reservationRepository);
    }
}
