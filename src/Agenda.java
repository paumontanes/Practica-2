import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

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
public class Agenda implements Serializable {

    private final List<Cita> citas;
    private final LocalTime cierre;
    private final LocalTime apertura;

    // ---------------------------------------------------------------- auxiliares

    /** mismoCliente(a, b) ≡ a = b ignorando mayúsculas y minúsculas (decisión D2). */
    public static boolean mismoCliente(String a, String b) {
        return a != null && a.equalsIgnoreCase(b);
    }

private static void exigirCliente(String cliente) throws ArgumentoException {
        if (cliente == null || cliente.isBlank()) {
            throw new ArgumentoException("El cliente no puede estar vacío");
        }
    }

    private static void exigirFecha(LocalDate fecha) throws ArgumentoException {
        if (fecha == null) {
            throw new ArgumentoException("La fecha no puede ser null");
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
      
        LocalTime cursor = apertura;
        for (Cita c : citas) {
            if (!c.getFecha().equals(fecha)) {
                continue;
            }
            if (ChronoUnit.MINUTES.between(cursor, c.getInicio()) >= duracion) {
                return cursor;
            }
            cursor = c.getFin();
        }
        if (ChronoUnit.MINUTES.between(cursor, cierre) >= duracion) {
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
    public Agenda(LocalTime apertura, LocalTime cierre) throws ArgumentoException {
    if (apertura == null || cierre == null || !apertura.isBefore(cierre)) {
        throw new ArgumentoException("Horario de apertura y cierre no válido");
    }
    this.apertura = apertura;
    this.cierre = cierre;
    this.citas = new ArrayList<>();
}

    public LocalTime getApertura() {
        return apertura;
    }

    public LocalTime getCierre() {
        return cierre;
    }

    public int getJornada() {
        return (int) ChronoUnit.MINUTES.between(apertura, cierre);
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
  public void reservar(Cita c) throws ArgumentoException {
        if (c == null) {
            throw new ArgumentoException("La cita no puede ser null");
        }
        if (c.getInicio().isBefore(apertura) || c.getFin().isAfter(cierre)) {
            throw new ArgumentoException("La cita está fuera del horario de la agenda");
        }
        if (tieneCita(c.getCliente(), c.getFecha())) {
            throw new ArgumentoException("El cliente ya tiene una cita ese día");
        }
        if (!estaLibre(c.getFecha(), c.getInicio(), c.getDuracion())) {
            throw new ArgumentoException("El tramo horario ya está ocupado");
        }
        int pos = 0;
        while (pos < citas.size() && !esPosterior(citas.get(pos), c.getFecha(), c.getInicio())) {
            pos++;
        }
        citas.add(pos, c);
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
  public void cancelar(String cliente, LocalDate fecha) throws ArgumentoException {
        exigirCliente(cliente);
        exigirFecha(fecha);
        int pos = posicionDe(cliente, fecha);
        if (pos < 0) {
            throw new ArgumentoException("No hay cita de " + cliente + " el " + fecha);
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
  public void retrasar(String cliente, LocalDate fecha, int minutos) throws ArgumentoException {
        exigirCliente(cliente);
        exigirFecha(fecha);
        if (minutos <= 0) {
            throw new ArgumentoException("Los minutos a retrasar deben ser positivos");
        }
        int pos = posicionDe(cliente, fecha);
        if (pos < 0) {
            throw new ArgumentoException("No hay cita de " + cliente + " el " + fecha);
        }
        Cita c = citas.get(pos);
        Cita retrasada = c.conInicio(c.getInicio().plusMinutes(minutos));

        if (retrasada.getInicio().isBefore(apertura) || retrasada.getFin().isAfter(cierre)) {
            throw new ArgumentoException("La cita retrasada queda fuera del horario");
        }
        if (pos + 1 < citas.size() && citas.get(pos + 1).getFecha().equals(fecha)
                && retrasada.getFin().isAfter(citas.get(pos + 1).getInicio())) {
            throw new ArgumentoException("La cita retrasada se solapa con la siguiente");
        }
        citas.set(pos, retrasada);
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
   public Cita getCitaEn(int i) throws ArgumentoException {
        if (i < 0 || i >= citas.size()) {
            throw new ArgumentoException("Posición " + i + " fuera de rango [0, " + citas.size() + ")");
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
   public ArrayList<Cita> getCitasDel(LocalDate fecha) throws ArgumentoException {
        exigirFecha(fecha);
        ArrayList<Cita> resultado = new ArrayList<>();
        for (Cita c : citas) {
            if (c.getFecha().equals(fecha)) {
                resultado.add(c);
            }
        }
        return resultado;
    }
    public ArrayList<Cita> getCitasEntre(LocalDate desde, LocalDate hasta) throws ArgumentoException {
        exigirFecha(desde);
        exigirFecha(hasta);
        if (desde.isAfter(hasta)) {
            throw new ArgumentoException("La fecha inicial no puede ser posterior a la final");
        }
        ArrayList<Cita> resultado = new ArrayList<>();
        for (Cita c : citas) {
            if (!c.getFecha().isBefore(desde) && !c.getFecha().isAfter(hasta)) {
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
    public boolean tieneCita(String cliente, LocalDate fecha) throws ArgumentoException {
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
    public Cita getCitaDe(String cliente, LocalDate fecha) throws ArgumentoException {
        exigirCliente(cliente);
        exigirFecha(fecha);
        int pos = posicionDe(cliente, fecha);
        if (pos < 0) {
            return null;
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
    public boolean estaLibre(LocalDate fecha, LocalTime h, int d) throws ArgumentoException {
        exigirFecha(fecha);
        if (h == null) {
            throw new ArgumentoException("La hora no puede ser null");
        }
        exigirDuracion(d);
        LocalTime fin = h.plusMinutes(d);
        if (h.isBefore(apertura) || fin.isAfter(cierre)) {
            return false;
        }
        for (Cita c : citas) {
            if (c.getFecha().equals(fecha) && h.isBefore(c.getFin()) && c.getInicio().isBefore(fin)) {
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
   public boolean hayHueco(LocalDate fecha, int d) throws ArgumentoException {
        exigirFecha(fecha);
        exigirDuracion(d);
        return buscarHueco(fecha, d) != null;
    }

    /**
     * primerHueco(a, fecha, duracion) → Hora
     * Pre: fecha ≠ null ∧ duracion > 0 ∧ hayHueco(fecha, duracion).
     * Post: r es la menor hora h tal que estaLibre(fecha, h, duracion).
     *
     * @throws IllegalArgumentException si se incumple la precondición
     */
public LocalTime primerHueco(LocalDate fecha, int d) throws ArgumentoException {
        exigirFecha(fecha);
        exigirDuracion(d);
        return buscarHueco(fecha, d);
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
        if (duracion > getJornada()) {
            throw new IllegalArgumentException("La duración supera la jornada (" + getJornada() + " min)");
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
 public int getTiempoLibre(LocalDate fecha) throws ArgumentoException {
        exigirFecha(fecha);
        int ocupado = 0;
        for (Cita c : citas) {
            if (c.getFecha().equals(fecha)) {
                ocupado += c.getDuracion();
            }
        }
        return getJornada() - ocupado;
    }

     private static void exigirDuracion(int duracion) throws ArgumentoException {
        if (duracion <= 0) {
            throw new ArgumentoException("La duración debe ser positiva");
        }
    }

    // ---------------------------------------------------------------- invariante y texto

    /**
     * Comprueba InvAgenda. Se usa en las pruebas; no modifica la agenda.
     */
   boolean invariante() {
        for (int i = 0; i < citas.size(); i++) {
            Cita c = citas.get(i);
            if (c.getCliente() == null || c.getCliente().isBlank()
                    || c.getInicio().isBefore(apertura) || c.getFin().isAfter(cierre)) {
                return false;
            }
            if (i + 1 < citas.size()) {
                Cita s = citas.get(i + 1);
                boolean ok = c.getFecha().isBefore(s.getFecha())
                        || (c.getFecha().equals(s.getFecha()) && !c.getFin().isAfter(s.getInicio()));
                if (!ok) {
                    return false;
                }
            }
            for (int j = i + 1; j < citas.size(); j++) {
                Cita d = citas.get(j);
                if (c.getFecha().equals(d.getFecha()) && mismoCliente(c.getCliente(), d.getCliente())) {
                    return false;
                }
            }
        }
        return true;
    }

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
            sb.append("  ").append(c.getInicio()).append('-').append(c.getFin())
                    .append("  ").append(c.getCliente()).append('\n');
        }
        return sb.toString();
    }
}
