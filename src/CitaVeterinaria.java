import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.DayOfWeek;

/**
 * Clase abstracta que representa una cita veterinaria.
 * Extiende la clase Cita y añade información sobre la mascota,
 * el veterinario, el resultado y las notas de la consulta.
 */
public abstract class CitaVeterinaria extends Cita {

    /** Resultado final de la cita. */
    private Resultado resultado;

    /** Observaciones realizadas por el veterinario. */
    private String notas;

    /** Mascota asociada a la cita. */
    private final Mascota mascota;

    /** Veterinario responsable de la cita. */
    private final Veterinario veterinario;

    /**
     * Construye una cita veterinaria.
     *
     * @param mascota mascota asociada a la cita
     * @param veterinario veterinario responsable
     * @param fecha fecha de la cita
     * @param inicio hora de inicio
     * @param duracion duración de la cita
     * @throws ArgumentoException si los argumentos no son válidos
     */
    protected CitaVeterinaria(Mascota mascota, Veterinario veterinario,
                              LocalDate fecha, LocalTime inicio, int duracion)
            throws ArgumentoException {

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
        this.notas = null;
    }

    /**
     * Devuelve la mascota asociada a la cita.
     *
     * @return mascota de la cita
     */
    public final Mascota getMascota() {
        return mascota;
    }

    /**
     * Devuelve el veterinario responsable.
     *
     * @return veterinario de la cita
     */
    public final Veterinario getVeterinario() {
        return veterinario;
    }

    /**
     * Devuelve el resultado de la cita.
     *
     * @return resultado de la cita, o null si no ha terminado
     */
    public final Resultado getResultado() {
        return resultado;
    }

    /**
     * Devuelve las notas de la cita.
     *
     * @return observaciones del veterinario
     */
    public final String getNotas() {
        return notas;
    }

    /**
     * Calcula el precio de la cita.
     *
     * @return precio de la cita
     */
    public abstract int precio();

    /**
     * Devuelve el tipo de cita.
     *
     * @return tipo de cita
     */
    public abstract String getTipo();

    /**
     * Devuelve la especialidad necesaria para atender la cita.
     *
     * @return especialidad requerida
     */
    public abstract Especialidad especialidadRequerida();

    /**
     * Devuelve una descripción de la cita.
     *
     * @return descripción de la cita
     */
    public abstract String descripcion();

    /**
     * Comprueba las normas comunes de las citas veterinarias.
     *
     * @param ahora fecha y hora actual
     * @throws ArgumentoException si no se cumplen las normas
     */
    public final void comprobarNormas(LocalDateTime ahora)
            throws ArgumentoException {

        if (ahora == null) {
            throw new ArgumentoException("La fecha actual no puede ser nula");
        }

        if (getFecha().getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new ArgumentoException("No se admiten citas los domingos");
        }

        if (getInicio().isBefore(LocalTime.of(9, 0))
                || getFin().isAfter(LocalTime.of(20, 0))) {
            throw new ArgumentoException("La cita está fuera del horario");
        }

        if (LocalDateTime.of(getFecha(), getInicio()).isBefore(ahora)) {
            throw new ArgumentoException("No se pueden reservar citas pasadas");
        }

        if (!veterinario.puedeAtender(especialidadRequerida())) {
            throw new ArgumentoException("Especialidad no válida");
        }

        comprobarNormasPropias(ahora);
    }

    /**
     * Comprueba las normas específicas de cada tipo de cita.
     *
     * @param ahora fecha y hora actual
     * @throws ArgumentoException si no se cumplen las normas específicas
     */
    protected void comprobarNormasPropias(LocalDateTime ahora)
            throws ArgumentoException {
    }

    /**
     * Finaliza la cita y registra su resultado y observaciones.
     *
     * @param resultado resultado final de la cita
     * @param notas observaciones del veterinario
     * @throws ArgumentoException si el resultado es nulo
     * o la cita ya está terminada
     */
    public final void terminar(Resultado resultado, String notas)
            throws ArgumentoException {

        if (resultado == null) {
            throw new ArgumentoException("El resultado no puede ser nulo");
        }

        if (this.resultado != null) {
            throw new ArgumentoException("La cita ya está terminada");
        }

        this.resultado = resultado;
        this.notas = notas;
    }

    /**
     * Calcula el ingreso generado por la cita.
     *
     * @return precio si ha sido atendida, cero en otro caso
     */
    public final int ingreso() {
        if (resultado == Resultado.ATENDIDA) {
            return precio();
        }

        return 0;
    }

    /**
     * Construye una cadena con los atributos comunes
     * de las citas veterinarias.
     *
     * @return representación de los atributos comunes
     */
    protected final String camposComunes() {
        return "mascota=" + mascota
                + ", veterinario=" + veterinario
                + ", resultado=" + resultado
                + ", notas=" + notas;
    }

    /**
     * Devuelve la representación textual de la cita veterinaria.
     *
     * @return información de la cita
     */
    @Override
    public String toString() {
        return super.toString() + " " + camposComunes();
    }

    /**
     * Comprueba y normaliza una cadena de texto.
     *
     * @param s cadena que se quiere comprobar
     * @param que nombre del argumento
     * @param vacioPermitido indica si se permiten cadenas vacías
     * @return cadena sin espacios innecesarios
     * @throws ArgumentoException si la cadena no es válida
     */
    protected final String texto(String s, String que,
                                 boolean vacioPermitido)
            throws ArgumentoException {

        if (s == null) {
            throw new ArgumentoException(que + " no puede ser nulo");
        }

        String texto = s.trim();

        if (!vacioPermitido && texto.isEmpty()) {
            throw new ArgumentoException(que + " no puede estar vacío");
        }

        return texto;
    }
}