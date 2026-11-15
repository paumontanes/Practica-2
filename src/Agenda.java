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
 * Estado: un horario (apertura, cierre) y una secuencia de citas
 * citas = ⟨c0, ..., cn-1⟩, siempre en orden cronológico y sin solapamientos.
 * La agenda abre todos los días: el cierre de los domingos es una norma de la
 * clínica, no de la agenda.
 *
 * InvAgenda ≡
 *   1. apertura < cierre ∧ ∀ i ∈ [0, n): apertura ≤ citas[i].inicio ∧ citas[i].fin ≤ cierre
 *      (InvCita ya garantiza cliente no vacío, fecha e inicio no null y duración > 0)
 *   2. ∀ i ∈ [0, n-1): citas[i].fecha < citas[i+1].fecha
 *        ∨ (citas[i].fecha = citas[i+1].fecha ∧ citas[i].fin ≤ citas[i+1].inicio)
 *   3. ∀ i < j: citas[i].fecha = citas[j].fecha ⇒ ¬mismoCliente(citas[i].cliente, citas[j].cliente)
 *
 * Decisiones de diseño:
 *   D1. cancelar y retrasar identifican la cita por (cliente, fecha). Por la condición 3 del
 *       invariante, esa pareja identifica siempre una sola cita.
 *   D2. mismoCliente(a, b) ≡ a y b son iguales ignorando mayúsculas y minúsculas
 *       («Ana García» y «ana garcía» son el mismo cliente).
 *   D3. Retrasar una cita que pisaría a la siguiente del mismo día, o que terminaría después
 *       del cierre, es un error: retrasar lanza ArgumentoException y la agenda no cambia.
 *       No se mueven las citas siguientes.
 *
 * Convención de errores: ArgumentoException (comprobada) cuando un argumento es null o
 * imposible, y cuando la operación no se puede realizar (cita fuera de horario, hueco
 * ocupado, cliente con otra cita ese día, cita inexistente, posición fuera de rango).
 * Si una operación lanza la excepción, la agenda no cambia.
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

    /** Lanza ArgumentoException si el cliente es null o está vacío. */
private static void exigirCliente(String cliente) throws ArgumentoException {
        if (cliente == null || cliente.isBlank()) {
            throw new ArgumentoException("El cliente no puede estar vacío");
        }
    }

    /** Lanza ArgumentoException si la fecha es null. */
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
     * o null si el día no tiene ninguno. Recorre las citas del día con un cursor que
     * empieza en la apertura y mide el espacio hasta cada cita y, al final, hasta el cierre.
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
     * Agenda(apertura, cierre) → Agenda
     * Pre: apertura y cierre no son null y apertura < cierre.
     * Post: la agenda tiene ese horario y no tiene citas (getNumCitas = 0).
     *       InvAgenda se cumple trivialmente.
     *
     * @param apertura hora de apertura
     * @param cierre   hora de cierre
     * @throws ArgumentoException si se incumple la precondición
     */
    public Agenda(LocalTime apertura, LocalTime cierre) throws ArgumentoException {
    if (apertura == null || cierre == null || !apertura.isBefore(cierre)) {
        throw new ArgumentoException("Horario de apertura y cierre no válido");
    }
    this.apertura = apertura;
    this.cierre = cierre;
    this.citas = new ArrayList<>();
}

    /**
     * getApertura() → Hora
     * Pre: cierto. Post: devuelve la hora de apertura de la agenda; la agenda no cambia.
     */
    public LocalTime getApertura() {
        return apertura;
    }

    /**
     * getCierre() → Hora
     * Pre: cierto. Post: devuelve la hora de cierre de la agenda; la agenda no cambia.
     */
    public LocalTime getCierre() {
        return cierre;
    }

    /**
     * getJornada() → Entero
     * Pre: cierto. Post: devuelve los minutos entre la apertura y el cierre; la agenda no cambia.
     */
    public int getJornada() {
        return (int) ChronoUnit.MINUTES.between(apertura, cierre);
    }


    // ---------------------------------------------------------------- modificadoras

    /**
     * reservar(c)
     * Pre: c no es null ∧ apertura ≤ c.inicio ∧ c.fin ≤ cierre ∧
     *      ¬tieneCita(c.cliente, c.fecha) ∧ estaLibre(c.fecha, c.inicio, c.duracion).
     * Post: la agenda contiene además la cita c en su lugar cronológico; el resto de
     *       citas no cambia y conserva su orden.
     *
     * @param c cita ya creada que se quiere reservar
     * @throws ArgumentoException si se incumple la precondición (la agenda no cambia)
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
     * cancelar(cliente, fecha)
     * Pre: cliente no vacío ∧ fecha no es null ∧ tieneCita(cliente, fecha).
     * Post: la agenda es la anterior sin la cita de (cliente, fecha); el resto de citas
     *       conserva el orden y el hueco queda libre.
     *
     * @throws ArgumentoException si se incumple la precondición (la agenda no cambia)
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
     * retrasar(cliente, fecha, minutos)
     * Pre: cliente no vacío ∧ fecha no es null ∧ minutos > 0 ∧ tieneCita(cliente, fecha) ∧
     *      sea c la cita de (cliente, fecha): c.fin + minutos ≤ cierre y, si hay una cita
     *      siguiente el mismo día, c.fin + minutos ≤ siguiente.inicio (decisión D3).
     * Post: c se sustituye por la cita c.conInicio(c.inicio + minutos), que es del mismo tipo
     *       y tiene el mismo cliente, fecha y duración que c; el resto de citas no cambia.
     *
     * @throws ArgumentoException si se incumple la precondición (la agenda no cambia)
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
       if (minutos > ChronoUnit.MINUTES.between(c.getFin(), cierre)) {
        throw new ArgumentoException("La cita retrasada queda fuera del horario");
        }
    Cita retrasada = c.conInicio(c.getInicio().plusMinutes(minutos));

        if (pos + 1 < citas.size() && citas.get(pos + 1).getFecha().equals(fecha)
                && retrasada.getFin().isAfter(citas.get(pos + 1).getInicio())) {
            throw new ArgumentoException("La cita retrasada se solapa con la siguiente");
        }
        citas.set(pos, retrasada);
    }

    // ---------------------------------------------------------------- consultas básicas

    /**
     * getNumCitas() → Entero
     * Pre: cierto.
     * Post: r = |citas|; la agenda no cambia.
     */
    public int getNumCitas() {
    return citas.size();
}

    /**
     * getCitaEn(i) → Cita
     * Pre: 0 ≤ i < getNumCitas().
     * Post: r = citas[i]; la agenda no cambia.
     * Devolver la cita es seguro porque Cita es inmutable.
     *
     * @throws ArgumentoException si i está fuera de rango
     */
   public Cita getCitaEn(int i) throws ArgumentoException {
        if (i < 0 || i >= citas.size()) {
            throw new ArgumentoException("Posición " + i + " fuera de rango [0, " + citas.size() + ")");
        }
        return citas.get(i);
    }

    /**
     * getCitasDel(fecha) → ArrayList de Cita
     * Pre: fecha no es null.
     * Post: r contiene exactamente las citas con esa fecha, en el mismo orden que en la agenda;
     *       la agenda no cambia. Se devuelve una lista nueva: modificarla no afecta a la agenda.
     *
     * @throws ArgumentoException si fecha es null
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
    /**
     * getCitasEntre(desde, hasta) → ArrayList de Cita
     * Pre: desde y hasta no son null ∧ desde ≤ hasta.
     * Post: r contiene exactamente las citas con fecha en [desde, hasta] (ambas incluidas),
     *       en el mismo orden que en la agenda; la agenda no cambia. Se devuelve una lista nueva.
     *
     * @throws ArgumentoException si se incumple la precondición
     */
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
     * tieneCita(cliente, fecha) → Booleano
     * Pre: cliente no vacío ∧ fecha no es null.
     * Post: r ⇔ ∃ i: mismoCliente(citas[i].cliente, cliente) ∧ citas[i].fecha = fecha;
     *       la agenda no cambia.
     *
     * @throws ArgumentoException si se incumple la precondición
     */
    public boolean tieneCita(String cliente, LocalDate fecha) throws ArgumentoException {
        exigirCliente(cliente);
        exigirFecha(fecha);
        return posicionDe(cliente, fecha) >= 0;
    }

    /**
     * getCitaDe(cliente, fecha) → Cita
     * Pre: cliente no vacío ∧ fecha no es null ∧ tieneCita(cliente, fecha).
     * Post: r es la única cita con mismoCliente(r.cliente, cliente) ∧ r.fecha = fecha;
     *       la agenda no cambia.
     *
     * @throws ArgumentoException si se incumple la precondición
     */
    public Cita getCitaDe(String cliente, LocalDate fecha) throws ArgumentoException {
        exigirCliente(cliente);
        exigirFecha(fecha);

        int pos = posicionDe(cliente, fecha);
        if (pos < 0) {
            throw new ArgumentoException("No hay cita de " + cliente + " el " + fecha);
        }

        return citas.get(pos);
    }

    /**
     * numCitasDe(cliente) → Entero
     * Pre: cliente no vacío.
     * Post: r = |{ i : mismoCliente(citas[i].cliente, cliente) }|; la agenda no cambia.
     *
     * @throws ArgumentoException si el cliente es null o está vacío
     */
    public int numCitasDe(String cliente) throws ArgumentoException {
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
     * estaLibre(fecha, h, d) → Booleano
     * Pre: fecha no es null ∧ h no es null ∧ d > 0.
     * Post: r ⇔ apertura ≤ h ∧ h + d ≤ cierre ∧
     *       ∀ i: citas[i].fecha = fecha ⇒ (citas[i].fin ≤ h ∨ h + d ≤ citas[i].inicio);
     *       la agenda no cambia.
     *
     * @throws ArgumentoException si se incumple la precondición
     */
    public boolean estaLibre(LocalDate fecha, LocalTime h, int d) throws ArgumentoException {
        exigirFecha(fecha);
        if (h == null) {
            throw new ArgumentoException("La hora no puede ser null");
        }
        exigirDuracion(d);
       if (h.isBefore(apertura) || d > ChronoUnit.MINUTES.between(h, cierre)) {
        return false;
    }
        LocalTime fin = h.plusMinutes(d);
        for (Cita c : citas) {
            if (c.getFecha().equals(fecha) && h.isBefore(c.getFin()) && c.getInicio().isBefore(fin)) {
                return false;
            }
        }
        return true;
    }

    /**
     * hayHueco(fecha, d) → Booleano
     * Pre: fecha no es null ∧ d > 0.
     * Post: r ⇔ ∃ h: estaLibre(fecha, h, d); la agenda no cambia.
     *
     * @throws ArgumentoException si se incumple la precondición
     */
   public boolean hayHueco(LocalDate fecha, int d) throws ArgumentoException {
        exigirFecha(fecha);
        exigirDuracion(d);
        return buscarHueco(fecha, d) != null;
    }

    /**
     * primerHueco(fecha, d) → Hora
     * Pre: fecha no es null ∧ d > 0 ∧ hayHueco(fecha, d).
     * Post: r es la menor hora h tal que estaLibre(fecha, h, d); la agenda no cambia.
     *
     * @throws ArgumentoException si se incumple la precondición (la fecha es null, la
     *         duración no es positiva o no existe un hueco de esa duración)
     */
public LocalTime primerHueco(LocalDate fecha, int d) throws ArgumentoException {
    exigirFecha(fecha);
    exigirDuracion(d);

    LocalTime hueco = buscarHueco(fecha, d);
    if (hueco == null) {
        throw new ArgumentoException("No hay hueco de " + d + " min el " + fecha);
    }
    return hueco;
}

    /**
     * primerDiaConHueco(fecha, duracion) → Fecha
     * Pre: fecha no es null ∧ 0 < duracion ≤ getJornada().
     * Post: r es la menor fecha f ≥ fecha tal que hayHueco(f, duracion); la agenda no cambia.
     *       (Siempre existe: la agenda es finita y duracion cabe en una jornada.)
     *
     * @throws ArgumentoException si se incumple la precondición (la fecha es null, la
     *         duración no es positiva o supera la jornada de la agenda)
     */
public LocalDate primerDiaConHueco(LocalDate fecha, int duracion) throws ArgumentoException {
    exigirFecha(fecha);
    exigirDuracion(duracion);

    if (duracion > getJornada()) {
        throw new ArgumentoException("La duración supera la jornada (" + getJornada() + " min)");
    }

    LocalDate dia = fecha;
    while (buscarHueco(dia, duracion) == null) {
        dia = dia.plusDays(1);
    }
    return dia;
}

    /**
     * getTiempoLibre(fecha) → Entero
     * Pre: fecha no es null.
     * Post: r = getJornada() − Σ { citas[i].duracion : citas[i].fecha = fecha };
     *       la agenda no cambia.
     *
     * @throws ArgumentoException si fecha es null
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

     /** Lanza ArgumentoException si la duración no es positiva. */
     private static void exigirDuracion(int duracion) throws ArgumentoException {
        if (duracion <= 0) {
            throw new ArgumentoException("La duración debe ser positiva");
        }
    }

    // ---------------------------------------------------------------- invariante y texto

    /**
     * Comprueba InvAgenda: cada cita está dentro del horario, las citas están en orden
     * cronológico sin solaparse y ningún cliente tiene dos citas el mismo día.
     * Se usa en las pruebas (solo es visible dentro del paquete); no modifica la agenda.
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

    /**
     * toString() → Cadena
     * Pre: cierto.
     * Post: texto con las citas agrupadas por día: una cabecera con la fecha y el día de la
     *       semana, y debajo cada cita con su hora de inicio, fin y cliente. Si no hay citas
     *       devuelve "Agenda vacía". La agenda no cambia.
     */
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
