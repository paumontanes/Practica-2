import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.Duration;
import java.io.Serializable;
/**
 * Cita abstracta de una agenda. Es inmutable: sus datos se fijan al crearla
 * y no cambian; una cita "modificada" es siempre una cita nueva.
 *
 * Invariante: el cliente no es null ni vacío, la fecha y el inicio no son null
 * y la duración es positiva (en minutos).
 *
 * Cada tipo de cita concreto (por ejemplo, consulta o cirugía) debe implementar
 * conInicio. Los getters son final para que ninguna subclase pueda romper
 * el invariante.
 */

    public abstract class Cita implements Serializable {

          /** Nombre o identificador del cliente. */
        private final String cliente;

        /** Día de la cita. */
        private final LocalDate fecha;

         /** Hora a la que empieza la cita. */
        private final LocalTime inicio;

         /** Duración de la cita, en minutos. */
        private final int duracion;

         /**
     * Crea una cita.
     *
     * Pre: cliente no es null ni vacío, fecha no es null, inicio no es null
     *      y duracion > 0.
     * Post: la cita tiene exactamente esos datos.
     *
     * @param cliente  cliente de la cita
     * @param fecha    día de la cita
     * @param inicio   hora de inicio
     * @param duracion duración en minutos
     * @throws ArgumentoException si se incumple la precondición
     */
       protected Cita(String cliente, LocalDate fecha, LocalTime inicio, int duracion)
        throws ArgumentoException {
    if (cliente == null || cliente.isBlank()) {
        throw new ArgumentoException("El cliente no puede estar vacío");
    }
    if (fecha == null) {
        throw new ArgumentoException("La fecha no puede ser null");
    }
    if (inicio == null) {
        throw new ArgumentoException("La hora de inicio no puede ser null");
    }
    if (duracion <= 0) {
        throw new ArgumentoException("La duración debe ser positiva");
    }
    this.cliente = cliente;
    this.fecha = fecha;
    this.inicio = inicio;
    this.duracion = duracion;
}

         /**
     * Crea una copia de esta cita que empieza a otra hora.
     *
     * Pre: nuevoInicio no es null y la cita resultante cumple el invariante.
     * Post: devuelve una cita del mismo tipo y con los mismos datos que esta,
     *       salvo el inicio, que es nuevoInicio. Esta cita no cambia.
     *
     * @param nuevoInicio nueva hora de inicio
     * @return la cita retrasada o adelantada
     * @throws ArgumentoException si se incumple la precondición
     */
        public abstract Cita conInicio(LocalTime nuevoInicio) throws ArgumentoException;

/**
     * Devuelve el cliente de la cita.
     * Pre: cierto. Post: devuelve el cliente, que no es vacío.
     */
        public final String getCliente() {
            return cliente;
        }

         /**
     * Devuelve la fecha de la cita.
     * Pre: cierto. Post: devuelve la fecha, que no es null.
     */
        public final LocalDate getFecha() {
            return fecha;
        }

        /**
     * Devuelve la hora de inicio de la cita.
     * Pre: cierto. Post: devuelve el inicio, que no es null.
     */
        public final LocalTime getInicio() {
            return inicio;
        }

        /**
     * Devuelve la duración de la cita en minutos.
     * Pre: cierto. Post: devuelve la duración, que es mayor que 0.
     */
        public final int getDuracion() {
            return duracion;
        }

        
    /**
     * Devuelve la hora a la que termina la cita.
     * Pre: cierto. Post: devuelve getInicio() + getDuracion() minutos.
     */
        public final LocalTime getFin() {
            return inicio.plusMinutes(duracion);
        }

           /**
     * Devuelve una representación en texto de la cita.
     * Pre: cierto. Post: texto con el formato "cliente fecha inicio-fin".
     */
        @Override
        public String toString() {
            return cliente + " " + fecha + " " + inicio + "-" + getFin();
        }
    }


