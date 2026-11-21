import java.io.Serializable;
import java.time.LocalTime;

/**
 * Veterinario de la clínica. Sus datos (código, nombre y especialidad) no cambian
 * tras el alta; lo único que cambia es su agenda, que crea él mismo con el
 * horario de la clínica.
 *
 * <p>Invariante InvVeterinario(v):
 * <ol>
 *   <li>codigo no es null ni vacío.</li>
 *   <li>nombre no es null ni vacío.</li>
 *   <li>especialidad no es null.</li>
 *   <li>agenda no es null y su horario es de 9:00 a 20:00.</li>
 * </ol>
 *
 * <p>Implementa {@link Serializable} para guardarse en fichero junto con la clínica.
 */
public class Veterinario implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Hora de apertura de la clínica (de lunes a sábado). */
    private static final LocalTime APERTURA = LocalTime.of(9, 0);
    /** Hora de cierre de la clínica. */
    private static final LocalTime CIERRE = LocalTime.of(20, 0);

    /** Código único del veterinario (por ejemplo, "V1"), sin espacios al principio ni al final. */
    private final String codigo;
    /** Nombre del veterinario, sin espacios al principio ni al final. */
    private final String nombre;
    /** Especialidad, que decide qué citas puede atender. */
    private final Especialidad especialidad;
    /** Agenda propia del veterinario, con el horario de la clínica. */
    private final Agenda agenda;

    /**
     * Crea un veterinario con una agenda vacía de 9:00 a 20:00.
     *
     * <p>Pre: codigo y nombre no son null ni vacíos; especialidad no es null.<br>
     * Post: getCodigo() = codigo.trim() ∧ getNombre() = nombre.trim() ∧
     * getEspecialidad() = especialidad ∧ la agenda está vacía.
     *
     * @param codigo       código del veterinario
     * @param nombre       nombre del veterinario
     * @param especialidad especialidad del veterinario
     * @throws ArgumentoException si algún argumento es null o está vacío
     */
    public Veterinario(String codigo, String nombre, Especialidad especialidad)
            throws ArgumentoException {
        if (codigo == null || codigo.isBlank()) {
            throw new ArgumentoException("El código del veterinario no puede ser nulo ni vacío.");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new ArgumentoException("El nombre del veterinario no puede ser nulo ni vacío.");
        }
        if (especialidad == null) {
            throw new ArgumentoException("La especialidad no puede ser nula.");
        }
        this.codigo = codigo.trim();
        this.nombre = nombre.trim();
        this.especialidad = especialidad;
        this.agenda = new Agenda(APERTURA, CIERRE);
    }

    /**
     * Pre: cierto. Post: r = código del veterinario (no vacío por el invariante).
     * No lanza excepciones.
     *
     * @return el código
     */
    public String getCodigo() {
        return codigo;
    }

    /**
     * Pre: cierto. Post: r = nombre del veterinario (no vacío por el invariante).
     * No lanza excepciones.
     *
     * @return el nombre
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Pre: cierto. Post: r = especialidad del veterinario (no null por el invariante).
     * No lanza excepciones.
     *
     * @return la especialidad
     */
    public Especialidad getEspecialidad() {
        return especialidad;
    }

    /**
     * Devuelve la agenda del propio veterinario, no una copia: quien la reciba
     * la modifica directamente (reservar, cancelar, retrasar).
     *
     * <p>Pre: cierto. Post: r = agenda del veterinario (no null por el invariante).
     * No lanza excepciones.
     *
     * @return la agenda
     */
    public Agenda getAgenda() {
        return agenda;
    }

    /**
     * Regla de especialidades: un veterinario puede atender una cita si la
     * especialidad exigida es GENERAL o es la suya.
     *
     * <p>Pre: cierto.<br>
     * Post: r = (requerida ≠ null ∧ (requerida = GENERAL ∨ requerida = getEspecialidad())).
     * Con null devuelve false. No lanza excepciones.
     *
     * @param requerida especialidad que exige la cita
     * @return true si puede atenderla
     */
    public boolean puedeAtender(Especialidad requerida) {
        if (requerida == null) {
            return false;
        }
        return requerida == Especialidad.GENERAL || requerida == especialidad;
    }

    /**
     * Pre: cierto. Post: r = "código - nombre (especialidad)", por ejemplo
     * "V1 - Laura Martín (GENERAL)". No lanza excepciones.
     *
     * @return la representación en texto
     */
    @Override
    public String toString() {
        return codigo + " - " + nombre + " (" + especialidad + ")";
    }
}
