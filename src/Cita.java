import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.Duration;
/**
     * TAD Cita (clínica veterinaria). Inmutable.
     *
     * Invariante InvCita(c):
     *   1. cliente no es vacío (ni null ni solo espacios).
     *   2. intervaloValido(fecha, inicio, duracion):
     *        laborable(fecha) && APERTURA <= inicio && duracion > 0 && inicio + duracion <= CIERRE
     *
     * La cita ocupa el intervalo semiabierto [inicio, fin): la hora de fin no cuenta como ocupada.
     */
    public final class Cita {

        /** Hora de apertura de la clínica. */
        public static final LocalTime APERTURA = LocalTime.of(9, 0);
        /** Hora de cierre de la clínica. */
        public static final LocalTime CIERRE = LocalTime.of(14, 0);
        /** Minutos entre APERTURA y CIERRE. */
        public static final int JORNADA = 300;

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

        // ---------------------------------------------------------------- predicados

        /** laborable(f) ≡ diaSemana(f) ∈ {lunes, ..., viernes}. */
        public static boolean laborable(LocalDate fecha) {
            if (fecha == null) {
                return false;
            }
            DayOfWeek d = fecha.getDayOfWeek();
            return d != DayOfWeek.SATURDAY && d != DayOfWeek.SUNDAY;
        }

        /**
         * intervaloValido(f, h, d) ≡ laborable(f) ∧ APERTURA ≤ h ∧ d > 0 ∧ h + d ≤ CIERRE.
         *
         * LocalTime da la vuelta a medianoche (23:30 + 60 min = 00:30), así que no se
         * comprueba sumando: se compara d con los minutos que hay entre h y CIERRE.
         * Es un predicado total: devuelve false con argumentos null en vez de lanzar.
         */
        public static boolean intervaloValido(LocalDate fecha, LocalTime inicio, int duracion) {
            if (fecha == null || inicio == null) {
                return false;
            }
            if (!laborable(fecha)) {
                return false;
            }
            if (inicio.isBefore(APERTURA)) {
                return false;
            }
            if (duracion <= 0) {
                return false;
            }
            long minutosHastaCierre = Duration.between(inicio, CIERRE).toMinutes();
            return duracion <= minutosHastaCierre;
        }

        // ---------------------------------------------------------------- crear

        /**
         * crear(cliente, fecha, inicio, duracion) → Cita
         * Pre: cliente no vacío ∧ intervaloValido(fecha, inicio, duracion)
         * Post: r.cliente = cliente ∧ r.fecha = fecha ∧ r.inicio = inicio ∧ r.duracion = duracion
         *
         * @throws IllegalArgumentException si se incumple la precondición
         */
        public static Cita crear(String cliente, LocalDate fecha, LocalTime inicio, int duracion) {
            if (cliente == null || cliente.isBlank()) {
                throw new IllegalArgumentException("El cliente no puede estar vacío");
            }
            if (!intervaloValido(fecha, inicio, duracion)) {
                throw new IllegalArgumentException(
                        "Intervalo no válido: " + fecha + " " + inicio + " durante " + duracion + " min");
            }
            return new Cita(cliente, fecha, inicio, duracion);
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


