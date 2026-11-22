import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Clase abstracta que representa una cita veterinaria.
 */
public abstract class CitaVeterinaria extends Cita {

    private Resultado resultado;
    private String notas;
    private final Mascota mascota;
    private final Veterinario veterinario;

    /**
     * Constructor de CitaVeterinaria.
     *
     * @param mascota mascota asociada a la cita
     * @param veterinario veterinario encargado de la cita
     * @param fecha fecha de la cita
     * @param inicio hora de inicio de la cita
     * @param duracion duración de la cita en minutos
     * @throws ArgumentoException si algún argumento no es válido
     */
    protected CitaVeterinaria(Mascota mascota, Veterinario veterinario,
                              LocalDate fecha, LocalTime inicio,
                              int duracion) throws ArgumentoException {

        super(mascota == null ? null : mascota.getId(),
                fecha, inicio, duracion);

        if (mascota == null) {
            throw new ArgumentoException("La mascota no puede ser nula");
        }

        if (veterinario == null) {
            throw new ArgumentoException("El veterinario no puede ser nulo");
        }

        this.mascota = mascota;
        this.veterinario = veterinario;
        this.resultado = null;
        this.notas = "";
    }

    /**
     * Devuelve la mascota asociada a la cita.
     *
     * @return mascota
     */
    public final Mascota getMascota() {
        return mascota;
    }

    /**
     * Devuelve el veterinario asociado a la cita.
     *
     * @return veterinario
     */
    public final Veterinario getVeterinario() {
        return veterinario;
    }

    /**
     * Devuelve el resultado de la cita.
     *
     * @return resultado
     */
    public final Resultado getResultado() {
        return resultado;
    }

    /**
     * Devuelve las notas de la cita.
     *
     * @return notas
     */
    public final String getNotas() {
        return notas;
    }

    /**
     * Devuelve el precio de la cita.
     *
     * @return precio
     */
    public abstract int precio();

    /**
     * Devuelve el tipo de cita.
     *
     * @return tipo de cita
     */
    public abstract String getTipo();

    /**
     * Devuelve la especialidad requerida para la cita.
     *
     * @return especialidad requerida
     */
    public abstract Especialidad especialidadRequerida();

    /**
     * Devuelve una descripción de la cita.
     *
     * @return descripción
     */
    public abstract String descripcion();

    /**
     * Comprueba las normas comunes de las citas veterinarias.
     *
     * @param ahora fecha y hora actual
     * @throws ArgumentoException si no se cumplen las normas
     */
    public final void comprobarNormas(LocalDateTime ahora)
            throws ArgumentoException, ClinicaException {

        if (ahora == null) {
            throw new ArgumentoException("La fecha actual no puede ser nula");   // este NO cambia
        }

        if (getFecha().getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new ClinicaException("No se admiten citas los domingos");      // cambia
        }

        LocalDateTime finCita = LocalDateTime.of(getFecha(), getInicio())
                .plusMinutes(getDuracion());

        if (getInicio().isBefore(LocalTime.of(9, 0))
                || finCita.toLocalTime().isAfter(LocalTime.of(20, 0))
                || !finCita.toLocalDate().equals(getFecha())) {

            throw new ClinicaException("La cita está fuera del horario");        // cambia
        }

        if (LocalDateTime.of(getFecha(), getInicio()).isBefore(ahora)) {
            throw new ClinicaException("No se pueden reservar citas pasadas");   // cambia
        }

        if (especialidadRequerida() == null
                || !veterinario.puedeAtender(especialidadRequerida())) {
            throw new ClinicaException("Especialidad no válida");                // cambia
        }

        comprobarNormasPropias(ahora);
    }

    /**
     * Comprueba las normas particulares de cada tipo de cita.
     *
     * @param ahora fecha y hora actual
     * @throws ArgumentoException si no se cumplen las normas particulares
     */
    protected void comprobarNormasPropias(LocalDateTime ahora)
            throws ClinicaException {
    }

    /**
     * Finaliza la cita indicando el resultado y las notas.
     *
     * @param resultado resultado de la cita
     * @param notas notas asociadas a la cita
     * @throws ArgumentoException si el resultado no es válido
     * o la cita ya ha sido finalizada
     */
    public final void terminar(Resultado resultado, String notas)
            throws ArgumentoException {

        if (resultado == null) {
            throw new ArgumentoException("El resultado no puede ser nulo");
        }

        if (this.resultado != null) {
            throw new ArgumentoException("La cita ya ha sido finalizada");
        }

        this.resultado = resultado;
        this.notas = texto(notas, "Las notas", true);
    }

    /**
     * Calcula el ingreso generado por la cita.
     *
     * @return ingreso generado
     */
    public final int ingreso() {
        if (resultado == Resultado.ATENDIDA) {
            return precio();
        }

        return 0;
    }

    /**
     * Devuelve los campos comunes de la cita veterinaria.
     *
     * @return campos comunes
     */
    protected final String camposComunes() {
        return "Mascota: " + mascota + ", Veterinario: " + veterinario
                + ", Tipo: " + getTipo() + ", Resultado: " + resultado
                + ", Notas: " + notas;
    }

    /**
     * Devuelve una representación textual de la cita.
     *
     * @return representación textual
     */
    @Override
    public String toString() {
        return super.toString() + " " + camposComunes();
    }

    /**
     * Comprueba y normaliza un texto.
     *
     * @param s texto a comprobar
     * @param que descripción del texto
     * @param vacioPermitido indica si se permite el texto vacío
     * @return texto normalizado
     * @throws ArgumentoException si el texto no es válido
     */
    protected static String texto(String s, String que,
                                  boolean vacioPermitido)
            throws ArgumentoException {

        if (s == null) {
            throw new ArgumentoException(que + " no puede ser nulo");
        }

        s = s.trim();

        if (!vacioPermitido && s.isEmpty()) {
            throw new ArgumentoException(que + " no puede estar vacío");
        }

        return s;
    }
}