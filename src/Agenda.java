import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * TAD Agenda de citas de una clínica veterinaria.
 *
 * Estado: citas = ⟨c0, ..., cn-1⟩, siempre en orden cronológico y sin solapamientos.
 *
 * InvAgenda ≡
 *   1. ∀ i ∈ [0, n): InvCita(citas[i])
 *   2. ∀ i ∈ [0, n-1): citas[i].fecha < citas[i+1].fecha
 *        ∨ (citas[i].fecha = citas[i+1].fecha ∧ fin(citas[i]) ≤ citas[i+1].inicio)
 *   3. ∀ i < j: citas[i].fecha = citas[j].fecha ⇒ ¬mismoCliente(citas[i].cliente, citas[j].cliente)
 *
 * Decisiones de diseño (Fase 0):
 *   D1. cancelar y retrasar identifican la cita por (cliente, fecha). Por la condición 3 del
 *       invariante, esa pareja identifica siempre una sola cita.
 *   D2. mismoCliente(a, b) ≡ a y b son iguales ignorando mayúsculas y minúsculas
 *       («Ana García» y «ana garcía» son el mismo cliente).
 *   D3. Retrasar una cita que pisaría a la siguiente del mismo día NO es un error: retrasar
 *       devuelve false y la agenda no cambia. No se mueven las citas siguientes.
 *
 * Convención de errores: IllegalArgumentException si se incumple una precondición;
 * IndexOutOfBoundsException para posiciones fuera de rango. Las operaciones reservar y
 * retrasar devuelven false cuando son correctas pero no se pueden realizar (no hay hueco).
 * Ningún método de esta clase escribe por pantalla.
 */
public class Agenda {

    private final List<Cita> citas;

    // ---------------------------------------------------------------- auxiliares

    /** mismoCliente(a, b) ≡ a = b ignorando mayúsculas y minúsculas (decisión D2). */
    public static boolean mismoCliente(String a, String b) {
        return a != null && a.equalsIgnoreCase(b);
    }

    private static void exigirCliente(String cliente) {
        if (cliente == null || cliente.isBlank()) {
            throw new IllegalArgumentException("El cliente no puede estar vacío");
        }
    }

    private static void exigirFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha no puede ser null");
        }
    }

    /** Posición de la cita de (cliente, fecha), o -1 si no existe. */
    private int posicionDe(String cliente, LocalDate fecha) {
        for (int i = 0; i < citas.size(); i++) {
            Cita c = citas.get(i);
            if (c.getFecha().equals(fecha) && mismoCliente(c.getCliente(), cliente)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Hora más temprana a la que empieza un hueco de duración dada en la fecha,
     * o null si no hay ninguno (o el día no es laborable).
     */
    private LocalTime buscarHueco(LocalDate fecha, int duracion) {
        if (!Cita.laborable(fecha)) {
            return null;
        }
        LocalTime cursor = Cita.APERTURA;
        for (Cita c : citas) {
            if (!c.getFecha().equals(fecha)) {
                continue;
            }
            if (ChronoUnit.MINUTES.between(cursor, c.getInicio()) >= duracion) {
                return cursor;
            }
            cursor = c.fin();
        }
        if (ChronoUnit.MINUTES.between(cursor, Cita.CIERRE) >= duracion) {
            return cursor;
        }
        return null;
    }

    // ---------------------------------------------------------------- constructor

    /**
     * Agenda() → Agenda
     * Pre: cierto.
     * Post: citas = ⟨⟩ (numCitas = 0). InvAgenda se cumple trivialmente.
     */
    public Agenda() {
        this.citas = new ArrayList<>();
    }

    // ---------------------------------------------------------------- modificadoras

    /**
     * reservar(a, cliente, fecha, inicio, duracion) → Booleano
     * Pre: cliente no vacío ∧ intervaloValido(fecha, inicio, duracion).
     * Post: si estaLibre(fecha, inicio, duracion) ∧ ¬tieneCita(cliente, fecha):
     *         devuelve true y la agenda contiene además la cita (cliente, fecha, inicio, duracion)
     *         en su lugar cronológico; el resto de citas no cambia.
     *       En otro caso devuelve false y la agenda no cambia.
     *
     * @throws IllegalArgumentException si se incumple la precondición
     */
    public boolean reservar(String cliente, LocalDate fecha, LocalTime inicio, int duracion) {
        exigirCliente(cliente);
        if (!Cita.intervaloValido(fecha, inicio, duracion)) {
            throw new IllegalArgumentException(
                    "Intervalo no válido: " + fecha + " " + inicio + " durante " + duracion + " min");
        }
        if (!estaLibre(fecha, inicio, duracion) || tieneCita(cliente, fecha)) {
            return false;
        }
        Cita nueva = Cita.crear(cliente, fecha, inicio, duracion);
        int pos = 0;
        while (pos < citas.size() && !esPosterior(citas.get(pos), fecha, inicio)) {
            pos++;
        }
        citas.add(pos, nueva);
        return true;
    }

    /** c empieza estrictamente después de (fecha, inicio). */
    private static boolean esPosterior(Cita c, LocalDate fecha, LocalTime inicio) {
        return c.getFecha().isAfter(fecha)
                || (c.getFecha().equals(fecha) && c.getInicio().isAfter(inicio));
    }

    /**
     * cancelar(a, cliente, fecha)
     * Pre: cliente no vacío ∧ fecha ≠ null ∧ tieneCita(cliente, fecha).
     * Post: la agenda es la anterior sin la cita de (cliente, fecha); el resto de citas
     *       conserva el orden y el hueco queda libre.
     *
     * @throws IllegalArgumentException si se incumple la precondición
     */
    public void cancelar(String cliente, LocalDate fecha) {
        exigirCliente(cliente);
        exigirFecha(fecha);
        int pos = posicionDe(cliente, fecha);
        if (pos < 0) {
            throw new IllegalArgumentException("No hay cita de " + cliente + " el " + fecha);
        }
        citas.remove(pos);
    }

    /**
     * retrasar(a, cliente, fecha, minutos) → Booleano
     * Pre: cliente no vacío ∧ fecha ≠ null ∧ minutos > 0 ∧ tieneCita(cliente, fecha).
     * Post: sea c la cita de (cliente, fecha). Si c.inicio + minutos + c.duracion ≤ CIERRE y,
     *       si hay una cita siguiente el mismo día, c.fin + minutos ≤ siguiente.inicio:
     *         devuelve true y c se sustituye por una cita de mismo cliente, fecha y duración
     *         con inicio c.inicio + minutos; el resto no cambia.
     *       En otro caso (decisión D3) devuelve false y la agenda no cambia.
     *
     * @throws IllegalArgumentException si se incumple la precondición
     */
    public boolean retrasar(String cliente, LocalDate fecha, int minutos) {
        exigirCliente(cliente);
        exigirFecha(fecha);
        if (minutos <= 0) {
            throw new IllegalArgumentException("Los minutos a retrasar deben ser positivos");
        }
        int pos = posicionDe(cliente, fecha);
        if (pos < 0) {
            throw new IllegalArgumentException("No hay cita de " + cliente + " el " + fecha);
        }
        Cita c = citas.get(pos);
        // Se compara con los minutos que quedan, sin sumar a la hora (LocalTime da la vuelta).
        long limite = ChronoUnit.MINUTES.between(c.fin(), Cita.CIERRE);
        if (pos + 1 < citas.size() && citas.get(pos + 1).getFecha().equals(fecha)) {
            limite = Math.min(limite, ChronoUnit.MINUTES.between(c.fin(), citas.get(pos + 1).getInicio()));
        }
        if (minutos > limite) {
            return false;
        }
        citas.set(pos, Cita.crear(c.getCliente(), fecha, c.getInicio().plusMinutes(minutos), c.getDuracion()));
        return true;
    }

    // ---------------------------------------------------------------- consultas básicas

    /**
     * numCitas(a) → Entero
     * Pre: cierto.
     * Post: r = |a.citas|; la agenda no cambia.
     */
    public int numCitas() {
        return citas.size();
    }

    /**
     * citaEn(a, i) → Cita
     * Pre: 0 ≤ i < numCitas(a).
     * Post: r = a.citas[i]; la agenda no cambia.
     * Devolver la cita es seguro porque Cita es inmutable.
     *
     * @throws IndexOutOfBoundsException si i está fuera de rango
     */
    public Cita citaEn(int i) {
        if (i < 0 || i >= citas.size()) {
            throw new IndexOutOfBoundsException(
                    "Posición " + i + " fuera de rango [0, " + citas.size() + ")");
        }
        return citas.get(i);
    }

    /**
     * citasDel(a, fecha) → Secuencia de Cita
     * Pre: fecha ≠ null.
     * Post: r contiene exactamente las citas de a con esa fecha, en el mismo orden que en a;
     *       la agenda no cambia. Se devuelve una copia: modificarla no afecta a la agenda.
     *
     * @throws IllegalArgumentException si fecha es null
     */
    public List<Cita> citasDel(LocalDate fecha) {
        exigirFecha(fecha);
        List<Cita> resultado = new ArrayList<>();
        for (Cita c : citas) {
            if (c.getFecha().equals(fecha)) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    /**
     * tieneCita(a, cliente, fecha) → Booleano
     * Pre: cliente no vacío ∧ fecha ≠ null.
     * Post: r ⇔ ∃ i: mismoCliente(citas[i].cliente, cliente) ∧ citas[i].fecha = fecha.
     *
     * @throws IllegalArgumentException si se incumple la precondición
     */
    public boolean tieneCita(String cliente, LocalDate fecha) {
        exigirCliente(cliente);
        exigirFecha(fecha);
        return posicionDe(cliente, fecha) >= 0;
    }

    /**
     * citaDe(a, cliente, fecha) → Cita
     * Pre: cliente no vacío ∧ fecha ≠ null ∧ tieneCita(cliente, fecha).
     * Post: r es la única cita de a con mismoCliente(r.cliente, cliente) ∧ r.fecha = fecha.
     *
     * @throws IllegalArgumentException si se incumple la precondición
     */
    public Cita citaDe(String cliente, LocalDate fecha) {
        exigirCliente(cliente);
        exigirFecha(fecha);
        int pos = posicionDe(cliente, fecha);
        if (pos < 0) {
            throw new IllegalArgumentException("No hay cita de " + cliente + " el " + fecha);
        }
        return citas.get(pos);
    }

    /**
     * numCitasDe(a, cliente) → Entero
     * Pre: cliente no vacío.
     * Post: r = |{ i : mismoCliente(citas[i].cliente, cliente) }|.
     *
     * @throws IllegalArgumentException si el cliente es vacío
     */
    public int numCitasDe(String cliente) {
        exigirCliente(cliente);
        int n = 0;
        for (Cita c : citas) {
            if (mismoCliente(c.getCliente(), cliente)) {
                n++;
            }
        }
        return n;
    }

    // ---------------------------------------------------------------- huecos

    /**
     * estaLibre(a, fecha, inicio, duracion) → Booleano
     * Pre: cierto (operación total).
     * Post: r ⇔ intervaloValido(fecha, inicio, duracion) ∧
     *       ∀ i: citas[i].fecha = fecha ⇒ (fin(citas[i]) ≤ inicio ∨ inicio + duracion ≤ citas[i].inicio).
     */
    public boolean estaLibre(LocalDate fecha, LocalTime inicio, int duracion) {
        if (!Cita.intervaloValido(fecha, inicio, duracion)) {
            return false;
        }
        LocalTime fin = inicio.plusMinutes(duracion); // sin vuelta: intervaloValido => fin ≤ CIERRE
        for (Cita c : citas) {
            if (c.getFecha().equals(fecha) && inicio.isBefore(c.fin()) && c.getInicio().isBefore(fin)) {
                return false;
            }
        }
        return true;
    }

    /**
     * hayHueco(a, fecha, duracion) → Booleano
     * Pre: fecha ≠ null ∧ duracion > 0.
     * Post: r ⇔ ∃ h: estaLibre(fecha, h, duracion).
     *
     * @throws IllegalArgumentException si se incumple la precondición
     */
    public boolean hayHueco(LocalDate fecha, int duracion) {
        exigirFecha(fecha);
        exigirDuracion(duracion);
        return buscarHueco(fecha, duracion) != null;
    }

    /**
     * primerHueco(a, fecha, duracion) → Hora
     * Pre: fecha ≠ null ∧ duracion > 0 ∧ hayHueco(fecha, duracion).
     * Post: r es la menor hora h tal que estaLibre(fecha, h, duracion).
     *
     * @throws IllegalArgumentException si se incumple la precondición
     */
    public LocalTime primerHueco(LocalDate fecha, int duracion) {
        exigirFecha(fecha);
        exigirDuracion(duracion);
        LocalTime h = buscarHueco(fecha, duracion);
        if (h == null) {
            throw new IllegalArgumentException("No hay hueco de " + duracion + " min el " + fecha);
        }
        return h;
    }

    /**
     * primerDiaConHueco(a, fecha, duracion) → Fecha
     * Pre: fecha ≠ null ∧ 0 < duracion ≤ JORNADA.
     * Post: r es la menor fecha f ≥ fecha tal que hayHueco(f, duracion).
     *       (Siempre existe: la agenda es finita y duracion ≤ JORNADA.)
     *
     * @throws IllegalArgumentException si se incumple la precondición
     */
    public LocalDate primerDiaConHueco(LocalDate fecha, int duracion) {
        exigirFecha(fecha);
        exigirDuracion(duracion);
        if (duracion > Cita.JORNADA) {
            throw new IllegalArgumentException("La duración supera la jornada (" + Cita.JORNADA + " min)");
        }
        LocalDate dia = fecha;
        while (buscarHueco(dia, duracion) == null) {
            dia = dia.plusDays(1);
        }
        return dia;
    }

    /**
     * tiempoLibre(a, fecha) → Entero
     * Pre: fecha ≠ null ∧ laborable(fecha).
     * Post: r = JORNADA − Σ { citas[i].duracion : citas[i].fecha = fecha }.
     *
     * @throws IllegalArgumentException si se incumple la precondición
     */
    public int tiempoLibre(LocalDate fecha) {
        exigirFecha(fecha);
        if (!Cita.laborable(fecha)) {
            throw new IllegalArgumentException("El " + fecha + " no es laborable");
        }
        int ocupado = 0;
        for (Cita c : citas) {
            if (c.getFecha().equals(fecha)) {
                ocupado += c.getDuracion();
            }
        }
        return Cita.JORNADA - ocupado;
    }

    private static void exigirDuracion(int duracion) {
        if (duracion <= 0) {
            throw new IllegalArgumentException("La duración debe ser positiva");
        }
    }

    // ---------------------------------------------------------------- invariante y texto

    /**
     * Comprueba InvAgenda. Se usa en las pruebas; no modifica la agenda.
     */
    boolean invariante() {
        for (int i = 0; i < citas.size(); i++) {
            Cita c = citas.get(i);
            // 1. InvCita (la clase Cita lo garantiza al crear; se recomprueba por seguridad)
            if (c.getCliente() == null || c.getCliente().isBlank()
                    || !Cita.intervaloValido(c.getFecha(), c.getInicio(), c.getDuracion())) {
                return false;
            }
            // 2. orden cronológico sin solapamientos (pares consecutivos)
            if (i + 1 < citas.size()) {
                Cita s = citas.get(i + 1);
                boolean ok = c.getFecha().isBefore(s.getFecha())
                        || (c.getFecha().equals(s.getFecha()) && !c.fin().isAfter(s.getInicio()));
                if (!ok) {
                    return false;
                }
            }
            // 3. un cliente, como mucho una cita al día
            for (int j = i + 1; j < citas.size(); j++) {
                Cita d = citas.get(j);
                if (c.getFecha().equals(d.getFecha()) && mismoCliente(c.getCliente(), d.getCliente())) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Muestra las citas agrupadas por día. */
    @Override
    public String toString() {
        if (citas.isEmpty()) {
            return "Agenda vacía";
        }
        StringBuilder sb = new StringBuilder();
        LocalDate actual = null;
        for (Cita c : citas) {
            if (!c.getFecha().equals(actual)) {
                if (actual != null) {
                    sb.append('\n');
                }
                actual = c.getFecha();
                DayOfWeek d = actual.getDayOfWeek();
                sb.append(actual).append(" (").append(d).append(")\n");
            }
            sb.append("  ").append(c.getInicio()).append('-').append(c.fin())
                    .append("  ").append(c.getCliente()).append('\n');
        }
        return sb.toString();
    }
}
