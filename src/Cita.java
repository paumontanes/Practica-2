import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.Duration;
import java.io.Serializable;

    public abstract class Cita implements Serializable {

        private final String cliente;
        private final LocalDate fecha;
        private final LocalTime inicio;
        private final int duracion;

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
        public abstract Cita conInicio(LocalTime nuevoInicio) throws ArgumentoException;


        public final String getCliente() {
            return cliente;
        }

        public final LocalDate getFecha() {
            return fecha;
        }

        public final LocalTime getInicio() {
            return inicio;
        }

        public final int getDuracion() {
            return duracion;
        }

        public final LocalTime getFin() {
            return inicio.plusMinutes(duracion);
        }

        @Override
        public String toString() {
            return cliente + " " + fecha + " " + inicio + "-" + getFin();
        }
    }


