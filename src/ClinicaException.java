/**
 * Excepción comprobada que indica que una operación incumple una norma de la clínica
 * (domingo cerrado, fuera de horario, cita en el pasado, especialidad incorrecta, etc.).
 * El mensaje explica el motivo.
 */
public class ClinicaException extends Exception {
    private static final long serialVersionUID = 1L;

    public ClinicaException(String mensaje) {
        super(mensaje);
    }
}
