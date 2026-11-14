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

        private Cita(String cliente, LocalDate fecha, LocalTime inicio, int duracion) {
            this.cliente = cliente;
            this.fecha = fecha;
            this.inicio = inicio;
            this.duracion = duracion;
        }


        // ---------------------------------------------------------------- consultas

        public String getCliente() {
            return cliente;
        }

        public LocalDate getFecha() {
            return fecha;
        }

        public LocalTime getInicio() {
            return inicio;
        }

        public int getDuracion() {
            return duracion;
        }

        /** fin(c) = getInicio(c) + getDuracion(c); cumple inicio < fin ≤ CIERRE. */
        public LocalTime fin() {
            return inicio.plusMinutes(duracion);
        }

        @Override
        public String toString() {
            return cliente + " " + fecha + " " + inicio + "-" + fin();
        }
    }


