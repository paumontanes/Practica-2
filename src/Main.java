import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Pruebas de Cita y Agenda. Se ejecutan con "Run Main" en IntelliJ (sin JUnit).
 * Al final se imprime el resumen; si hay fallos, el programa termina con código 1.
 */
public class Main {

    private static int total = 0;
    private static int fallos = 0;

    // ---------------------------------------------------------------- ayudas

    static void comprobar(String descripcion, boolean condicion) {
        total++;
        if (!condicion) {
            fallos++;
            System.out.println("FALLO: " + descripcion);
        }
    }

    static void esperaExcepcion(String descripcion, Class<? extends Throwable> tipo, Runnable codigo) {
        total++;
        try {
            codigo.run();
            fallos++;
            System.out.println("FALLO (no lanzó excepción): " + descripcion);
        } catch (Throwable t) {
            if (!tipo.isInstance(t)) {
                fallos++;
                System.out.println("FALLO (excepción distinta: " + t.getClass().getSimpleName()
                        + "): " + descripcion);
            }
        }
    }

    /** Excepción esperada Y la agenda no ha cambiado. */
    static void esperaExcepcionSinCambios(String descripcion, Agenda a,
                                          Class<? extends Throwable> tipo, Runnable codigo) {
        String antes = instantanea(a);
        esperaExcepcion(descripcion, tipo, codigo);
        comprobar(descripcion + " -> la agenda no cambia", antes.equals(instantanea(a)));
    }

    /** Texto que describe el estado completo de la agenda. */
    static String instantanea(Agenda a) {
        return a.numCitas() + "|" + a;
    }

    /** Clientes en orden de agenda, separados por comas. */
    static String orden(Agenda a) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < a.numCitas(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(a.citaEn(i).getCliente());
        }
        return sb.toString();
    }

    /** Clientes de una lista de citas, separados por comas. */
    static String clientes(List<Cita> lista) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lista.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(lista.get(i).getCliente());
        }
        return sb.toString();
    }

    // ---------------------------------------------------------------- datos de prueba

    private static final LocalDate LUN19 = LocalDate.of(2026, 10, 19);
    private static final LocalDate MAR20 = LocalDate.of(2026, 10, 20);
    private static final LocalDate MIE21 = LocalDate.of(2026, 10, 21);
    private static final LocalDate JUE22 = LocalDate.of(2026, 10, 22);
    private static final LocalDate SAB24 = LocalDate.of(2026, 10, 24);
    private static final LocalDate DOM25 = LocalDate.of(2026, 10, 25);
    private static final LocalDate LUN26 = LocalDate.of(2026, 10, 26);
    private static final LocalDate VIE30 = LocalDate.of(2026, 10, 30);
    private static final LocalDate LUN2 = LocalDate.of(2026, 11, 2);

    private static LocalTime h(int hora, int min) {
        return LocalTime.of(hora, min);
    }

    /** Agenda del ejemplo del enunciado, con las reservas en el orden indicado. */
    private static Agenda agendaEjemplo() {
        Agenda a = new Agenda();
        a.reservar("Bruno", MAR20, h(10, 0), 60);
        a.reservar("Ana", MAR20, h(9, 0), 40);
        a.reservar("David", MAR20, h(12, 30), 60);
        a.reservar("Carmen", MAR20, h(11, 15), 45);
        a.reservar("Ana", LUN19, h(13, 0), 30);
        a.reservar("Elena", MIE21, h(9, 0), 300);
        return a;
    }

    // ---------------------------------------------------------------- Cita

    private static void pruebasCita() {
        comprobar("APERTURA = 9:00", Cita.APERTURA.equals(h(9, 0)));
        comprobar("CIERRE = 14:00", Cita.CIERRE.equals(h(14, 0)));
        comprobar("JORNADA = 300", Cita.JORNADA == 300);

        comprobar("lunes laborable", Cita.laborable(LUN26));
        comprobar("viernes laborable", Cita.laborable(VIE30));
        comprobar("sábado no laborable", !Cita.laborable(SAB24));
        comprobar("domingo no laborable", !Cita.laborable(DOM25));
        comprobar("null no laborable", !Cita.laborable(null));

        comprobar("9:00 + 30 válido", Cita.intervaloValido(LUN26, h(9, 0), 30));
        comprobar("jornada completa válida", Cita.intervaloValido(LUN26, h(9, 0), 300));
        comprobar("termina justo a las 14:00", Cita.intervaloValido(LUN26, h(13, 30), 30));
        comprobar("1 minuto válido", Cita.intervaloValido(VIE30, h(13, 59), 1));
        comprobar("antes de apertura", !Cita.intervaloValido(LUN26, h(8, 59), 30));
        comprobar("termina 1 min después del cierre", !Cita.intervaloValido(LUN26, h(13, 30), 31));
        comprobar("empieza al cierre", !Cita.intervaloValido(LUN26, h(14, 0), 30));
        comprobar("empieza después del cierre", !Cita.intervaloValido(LUN26, h(15, 0), 30));
        comprobar("duración 0", !Cita.intervaloValido(LUN26, h(10, 0), 0));
        comprobar("duración negativa", !Cita.intervaloValido(LUN26, h(10, 0), -5));
        comprobar("sábado", !Cita.intervaloValido(SAB24, h(10, 0), 30));
        comprobar("domingo", !Cita.intervaloValido(DOM25, h(10, 0), 30));
        comprobar("fecha null", !Cita.intervaloValido(null, h(10, 0), 30));
        comprobar("inicio null", !Cita.intervaloValido(LUN26, null, 30));
        comprobar("vuelta a medianoche (23:30 + 60)", !Cita.intervaloValido(LUN26, h(23, 30), 60));
        comprobar("duración enorme", !Cita.intervaloValido(LUN26, h(10, 0), Integer.MAX_VALUE));

        Cita c = Cita.crear("Ana", LUN26, h(10, 0), 45);
        comprobar("getCliente", c.getCliente().equals("Ana"));
        comprobar("getFecha", c.getFecha().equals(LUN26));
        comprobar("getInicio", c.getInicio().equals(h(10, 0)));
        comprobar("getDuracion", c.getDuracion() == 45);
        comprobar("fin = inicio + duración", c.fin().equals(h(10, 45)));
        comprobar("inicio < fin", c.getInicio().isBefore(c.fin()));

        Cita primera = Cita.crear("Luis", LUN26, h(9, 0), 1);
        comprobar("cita de 1 min a la apertura", primera.fin().equals(h(9, 1)));
        Cita ultima = Cita.crear("Eva", VIE30, h(13, 59), 1);
        comprobar("cita que acaba al cierre", ultima.fin().equals(Cita.CIERRE));
        Cita todaLaJornada = Cita.crear("Marta", LUN26, h(9, 0), Cita.JORNADA);
        comprobar("jornada completa acaba al cierre", todaLaJornada.fin().equals(Cita.CIERRE));

        esperaExcepcion("crear: cliente null", IllegalArgumentException.class,
                () -> Cita.crear(null, LUN26, h(10, 0), 30));
        esperaExcepcion("crear: cliente vacío", IllegalArgumentException.class,
                () -> Cita.crear("", LUN26, h(10, 0), 30));
        esperaExcepcion("crear: cliente solo espacios", IllegalArgumentException.class,
                () -> Cita.crear("   ", LUN26, h(10, 0), 30));
        esperaExcepcion("crear: fin de semana", IllegalArgumentException.class,
                () -> Cita.crear("Ana", SAB24, h(10, 0), 30));
        esperaExcepcion("crear: antes de apertura", IllegalArgumentException.class,
                () -> Cita.crear("Ana", LUN26, h(8, 0), 30));
        esperaExcepcion("crear: pasa del cierre", IllegalArgumentException.class,
                () -> Cita.crear("Ana", LUN26, h(13, 45), 30));
        esperaExcepcion("crear: duración 0", IllegalArgumentException.class,
                () -> Cita.crear("Ana", LUN26, h(10, 0), 0));
        esperaExcepcion("crear: duración negativa", IllegalArgumentException.class,
                () -> Cita.crear("Ana", LUN26, h(10, 0), -1));
        esperaExcepcion("crear: fecha null", IllegalArgumentException.class,
                () -> Cita.crear("Ana", null, h(10, 0), 30));
        esperaExcepcion("crear: inicio null", IllegalArgumentException.class,
                () -> Cita.crear("Ana", LUN26, null, 30));
        esperaExcepcion("crear: vuelta a medianoche", IllegalArgumentException.class,
                () -> Cita.crear("Ana", LUN26, h(23, 30), 60));

        c.fin();
        comprobar("consultas no modifican la cita", c.getInicio().equals(h(10, 0)) && c.getDuracion() == 45);
    }

    // ---------------------------------------------------------------- ejemplo del enunciado

    private static void pruebaEjemplo() {
        Agenda a = agendaEjemplo();
        comprobar("ejemplo: 6 citas", a.numCitas() == 6);
        comprobar("ejemplo: orden de la tabla", orden(a).equals("Ana,Ana,Bruno,Carmen,David,Elena"));
        comprobar("ejemplo: pos 0 = Ana lun 19 13:00-13:30",
                a.citaEn(0).getFecha().equals(LUN19) && a.citaEn(0).getInicio().equals(h(13, 0))
                        && a.citaEn(0).getDuracion() == 30 && a.citaEn(0).fin().equals(h(13, 30)));
        comprobar("ejemplo: pos 1 = Ana mar 20 9:00-9:40",
                a.citaEn(1).getFecha().equals(MAR20) && a.citaEn(1).getInicio().equals(h(9, 0))
                        && a.citaEn(1).fin().equals(h(9, 40)));
        comprobar("ejemplo: pos 2 = Bruno 10:00-11:00",
                a.citaEn(2).getInicio().equals(h(10, 0)) && a.citaEn(2).fin().equals(h(11, 0)));
        comprobar("ejemplo: pos 3 = Carmen 11:15-12:00",
                a.citaEn(3).getInicio().equals(h(11, 15)) && a.citaEn(3).fin().equals(h(12, 0)));
        comprobar("ejemplo: pos 4 = David 12:30-13:30",
                a.citaEn(4).getInicio().equals(h(12, 30)) && a.citaEn(4).fin().equals(h(13, 30)));
        comprobar("ejemplo: pos 5 = Elena mié 21 9:00-14:00",
                a.citaEn(5).getFecha().equals(MIE21) && a.citaEn(5).fin().equals(h(14, 0)));
        comprobar("ejemplo: invariante", a.invariante());

        // Consultas del enunciado
        comprobar("número de citas de Ana = 2", a.numCitasDe("Ana") == 2);
        comprobar("citas del martes 20 = Ana, Bruno, Carmen, David",
                clientes(a.citasDel(MAR20)).equals("Ana,Bruno,Carmen,David"));
        comprobar("minutos libres martes 20 = 95", a.tiempoLibre(MAR20) == 95);
        comprobar("minutos libres miércoles 21 = 0", a.tiempoLibre(MIE21) == 0);
        comprobar("¿libre mar 20 9:40 20 min? sí", a.estaLibre(MAR20, h(9, 40), 20));
        comprobar("¿libre mar 20 10:30 15 min? no", !a.estaLibre(MAR20, h(10, 30), 15));
        comprobar("¿libre sáb 24 10:00 30 min? no (es sábado)", !a.estaLibre(SAB24, h(10, 0), 30));
        comprobar("primer hueco de 30 min el martes 20 = 12:00", a.primerHueco(MAR20, 30).equals(h(12, 0)));
        comprobar("¿hay hueco de 45 min el martes 20? no", !a.hayHueco(MAR20, 45));
        comprobar("primer día con hueco de 45 min desde mar 20 = jue 22",
                a.primerDiaConHueco(MAR20, 45).equals(JUE22));
        comprobar("primer día con hueco de 30 min desde sáb 24 = lun 26",
                a.primerDiaConHueco(SAB24, 30).equals(LUN26));

        String antes = instantanea(a);
        comprobar("reservar otra cita para Ana el martes 20 -> false", !a.reservar("Ana", MAR20, h(13, 30), 30));
        comprobar("... la agenda no cambia", antes.equals(instantanea(a)));

        a.cancelar("Bruno", MAR20);
        comprobar("tras cancelar Bruno: 5 citas", a.numCitas() == 5);
        comprobar("primer hueco de 45 min el martes 20 tras cancelar Bruno = 9:40",
                a.primerHueco(MAR20, 45).equals(h(9, 40)));
        comprobar("tras cancelar: invariante", a.invariante());
    }

    // ---------------------------------------------------------------- reservar / orden

    private static void pruebasReservarYOrden() {
        Agenda a = new Agenda();
        comprobar("agenda nueva vacía", a.numCitas() == 0);
        comprobar("agenda nueva: toString", a.toString().equals("Agenda vacía"));
        comprobar("agenda nueva: invariante", a.invariante());

        comprobar("reservar en agenda vacía -> true", a.reservar("B", MAR20, h(10, 0), 30));
        comprobar("reservar al principio -> true", a.reservar("A", MAR20, h(9, 0), 30));
        comprobar("reservar al final -> true", a.reservar("D", MAR20, h(13, 0), 60));
        comprobar("termina justo al cierre", a.citaEn(2).fin().equals(Cita.CIERRE));
        comprobar("reservar en medio -> true", a.reservar("C", MAR20, h(11, 0), 30));
        comprobar("orden A,B,C,D", orden(a).equals("A,B,C,D"));

        comprobar("contigua a la anterior (9:30, tras A) -> true", a.reservar("X", MAR20, h(9, 30), 30));
        comprobar("contigua por ambos lados (10:30) -> true", a.reservar("Z", MAR20, h(10, 30), 30));
        comprobar("orden A,X,B,Z,C,D", orden(a).equals("A,X,B,Z,C,D"));

        comprobar("fecha anterior con hora posterior -> primero", a.reservar("E", LUN19, h(13, 0), 30));
        comprobar("fecha posterior con hora anterior -> último", a.reservar("F", MIE21, h(9, 0), 30));
        comprobar("misma hora en días distintos -> true", a.reservar("G", LUN19, h(9, 0), 30));
        comprobar("orden G,E,A,X,B,Z,C,D,F", orden(a).equals("G,E,A,X,B,Z,C,D,F"));
        comprobar("invariante tras reservas", a.invariante());

        // Solapamiento -> false y sin cambios
        String antes = instantanea(a);
        comprobar("solapa con A (9:15-9:45) -> false", !a.reservar("Q", MAR20, h(9, 15), 30));
        comprobar("... la agenda no cambia", antes.equals(instantanea(a)));
    }

    private static void pruebasSolapamientos() {
        Agenda a = new Agenda();
        a.reservar("A", MAR20, h(10, 0), 60); // 10:00-11:00
        String antes = instantanea(a);
        comprobar("solape por el principio (9:30-10:30) -> false", !a.reservar("B", MAR20, h(9, 30), 60));
        comprobar("solape por el final (10:30-11:30) -> false", !a.reservar("B", MAR20, h(10, 30), 60));
        comprobar("contenida (10:15-10:30) -> false", !a.reservar("B", MAR20, h(10, 15), 15));
        comprobar("contiene (9:00-12:00) -> false", !a.reservar("B", MAR20, h(9, 0), 180));
        comprobar("misma hora exacta -> false", !a.reservar("B", MAR20, h(10, 0), 60));
        comprobar("false no cambia la agenda", antes.equals(instantanea(a)));
        comprobar("justo antes (9:00-10:00) -> true", a.reservar("B", MAR20, h(9, 0), 60));
        comprobar("justo después (11:00-12:00) -> true", a.reservar("C", MAR20, h(11, 0), 60));
        comprobar("invariante", a.invariante());
    }

    // ---------------------------------------------------------------- fechas

    private static void pruebasFechas() {
        Agenda a = new Agenda();
        a.reservar("Ana", LUN19, h(9, 0), 30);
        String antes = instantanea(a);

        esperaExcepcionSinCambios("reservar en sábado", a, IllegalArgumentException.class,
                () -> a.reservar("Pepe", SAB24, h(10, 0), 30));
        esperaExcepcionSinCambios("reservar en domingo", a, IllegalArgumentException.class,
                () -> a.reservar("Pepe", DOM25, h(10, 0), 30));
        comprobar("estaLibre en sábado = false", !a.estaLibre(SAB24, h(10, 0), 30));
        comprobar("estaLibre en domingo = false", !a.estaLibre(DOM25, h(10, 0), 30));
        comprobar("hayHueco en sábado = false", !a.hayHueco(SAB24, 30));
        comprobar("sin cambios tras fin de semana", antes.equals(instantanea(a)));

        // Un cliente con citas en dos días
        comprobar("Ana en otro día -> true", a.reservar("Ana", MAR20, h(9, 0), 30));
        comprobar("Ana tiene 2 citas", a.numCitasDe("Ana") == 2);
        comprobar("tieneCita Ana lun 19", a.tieneCita("Ana", LUN19));
        comprobar("tieneCita Ana mar 20", a.tieneCita("Ana", MAR20));
        comprobar("tieneCita Ana mié 21 = false", !a.tieneCita("Ana", MIE21));
        comprobar("citaDe Ana mar 20", a.citaDe("Ana", MAR20).getFecha().equals(MAR20));
        comprobar("Ana otra vez el mismo día (aunque haya hueco) -> false",
                !a.reservar("Ana", MAR20, h(12, 0), 30));

        // Cancelar solo la cita de ese día
        a.cancelar("Ana", MAR20);
        comprobar("cancelar: solo se quita la de ese día", a.numCitasDe("Ana") == 1);
        comprobar("cancelar: queda la del lunes", a.tieneCita("Ana", LUN19) && !a.tieneCita("Ana", MAR20));
        comprobar("cancelar: el hueco queda libre", a.estaLibre(MAR20, h(9, 0), 30));

        // Retrasar solo la cita de ese día
        a.reservar("Ana", MAR20, h(9, 0), 30);
        comprobar("retrasar Ana mar 20 30 min -> true", a.retrasar("Ana", MAR20, 30));
        comprobar("retrasar: la del martes se mueve", a.citaDe("Ana", MAR20).getInicio().equals(h(9, 30)));
        comprobar("retrasar: la del lunes no se mueve", a.citaDe("Ana", LUN19).getInicio().equals(h(9, 0)));

        // Retrasar la última cita de un día cuando la siguiente es de otro día
        Agenda b = agendaEjemplo(); // pos 0: Ana lun 19 13:00-13:30; pos 1 es de mar 20
        comprobar("retrasar última del día (siguiente de otro día) hasta el cierre -> true",
                b.retrasar("Ana", LUN19, 30));
        comprobar("... queda 13:30-14:00", b.citaDe("Ana", LUN19).getInicio().equals(h(13, 30))
                && b.citaDe("Ana", LUN19).fin().equals(Cita.CIERRE));
        comprobar("... orden intacto", orden(b).equals("Ana,Ana,Bruno,Carmen,David,Elena"));
        Agenda b2 = agendaEjemplo();
        String antesB2 = instantanea(b2);
        comprobar("retrasar última del día pasando del cierre -> false", !b2.retrasar("Ana", LUN19, 31));
        comprobar("... sin cambios", antesB2.equals(instantanea(b2)));

        // Primer día con hueco que salta un día lleno y un fin de semana
        Agenda c = new Agenda();
        c.reservar("V", VIE30, h(9, 0), 300);
        comprobar("viernes lleno: tiempoLibre = 0", c.tiempoLibre(VIE30) == 0);
        comprobar("primerDiaConHueco salta viernes lleno y fin de semana -> lunes 2",
                c.primerDiaConHueco(VIE30, 30).equals(LUN2));
        comprobar("primerDiaConHueco desde el domingo -> lunes", c.primerDiaConHueco(DOM25, 30).equals(LUN26));
        comprobar("primerDiaConHueco con jornada completa en día libre = ese día",
                c.primerDiaConHueco(LUN26, 300).equals(LUN26));
        comprobar("primerDiaConHueco con jornada completa salta el día lleno",
                c.primerDiaConHueco(VIE30, 300).equals(LUN2));
    }

    // ---------------------------------------------------------------- huecos

    private static void pruebasHuecos() {
        Agenda a = new Agenda();
        comprobar("día sin citas: primer hueco = apertura", a.primerHueco(MAR20, 60).equals(Cita.APERTURA));
        comprobar("día sin citas: hay hueco de toda la jornada", a.hayHueco(MAR20, Cita.JORNADA));
        comprobar("día sin citas: tiempo libre = JORNADA", a.tiempoLibre(MAR20) == Cita.JORNADA);
        comprobar("duración mayor que la jornada: no hay hueco", !a.hayHueco(MAR20, Cita.JORNADA + 1));

        // Hueco al principio del día
        a.reservar("A", MAR20, h(10, 0), 60);
        comprobar("hueco al principio: 9:00 para 60 min", a.primerHueco(MAR20, 60).equals(h(9, 0)));
        comprobar("hueco al principio: 61 min no cabe antes, va después de A",
                a.primerHueco(MAR20, 61).equals(h(11, 0)));

        // Hueco entre dos citas
        a.reservar("B", MAR20, h(12, 0), 60);
        comprobar("hueco entre dos citas (11:00-12:00)", a.primerHueco(MAR20, 60).equals(h(9, 0)));
        a.reservar("C", MAR20, h(9, 0), 60); // llena el principio
        comprobar("hueco entre dos citas: 60 min en 11:00", a.primerHueco(MAR20, 60).equals(h(11, 0)));
        comprobar("hueco exacto entre citas se acepta (justo 60)", a.estaLibre(MAR20, h(11, 0), 60));
        comprobar("hueco entre citas, 61 min no cabe", !a.estaLibre(MAR20, h(11, 0), 61));

        // Hueco al final
        comprobar("hueco al final: 13:00-14:00", a.estaLibre(MAR20, h(13, 0), 60));
        comprobar("hueco de 61 min: no en 11:00, sí... no hay", !a.hayHueco(MAR20, 61));
        a.reservar("D", MAR20, h(11, 0), 60);
        comprobar("primer hueco al final = 13:00", a.primerHueco(MAR20, 60).equals(h(13, 0)));
        comprobar("tiempo libre con 4 citas de 60 = 60", a.tiempoLibre(MAR20) == 60);

        // Día sin hueco
        a.reservar("E", MAR20, h(13, 0), 60);
        comprobar("día sin hueco: hayHueco(1 min) = false", !a.hayHueco(MAR20, 1));
        comprobar("día sin hueco: tiempoLibre = 0", a.tiempoLibre(MAR20) == 0);
        comprobar("día sin hueco: invariante", a.invariante());

        // Cancelar abre hueco
        a.cancelar("B", MAR20);
        comprobar("cancelar abre un hueco de 60 en 12:00", a.primerHueco(MAR20, 60).equals(h(12, 0)));
        comprobar("otros días no se ven afectados", a.primerHueco(MIE21, 60).equals(Cita.APERTURA));
    }

    // ---------------------------------------------------------------- retrasar y D3

    private static void pruebasRetrasar() {
        Agenda a = agendaEjemplo();

        comprobar("retrasar Carmen 10 min (hasta 11:25-12:10) -> true", a.retrasar("Carmen", MAR20, 10));
        comprobar("Carmen empieza 11:25", a.citaDe("Carmen", MAR20).getInicio().equals(h(11, 25)));
        comprobar("Carmen mantiene su duración", a.citaDe("Carmen", MAR20).getDuracion() == 45);
        comprobar("retrasar mantiene el orden", orden(a).equals("Ana,Ana,Bruno,Carmen,David,Elena"));
        comprobar("retrasar mantiene el número de citas", a.numCitas() == 6);
        comprobar("retrasar: invariante", a.invariante());

        // D3: pisaría a la siguiente -> false y sin cambios
        String antes = instantanea(a);
        // Carmen está en 11:25-12:10; David empieza a las 12:30: caben 20 min más.
        comprobar("retrasar Carmen 21 min (pisa a David) -> false", !a.retrasar("Carmen", MAR20, 21));
        comprobar("... sin cambios", antes.equals(instantanea(a)));
        comprobar("retrasar Carmen justo hasta tocar a David (20 min) -> true", a.retrasar("Carmen", MAR20, 20));
        comprobar("... Carmen termina 12:30 y David empieza 12:30",
                a.citaDe("Carmen", MAR20).fin().equals(h(12, 30)));

        // Última cita del día: hasta el cierre
        Agenda b = agendaEjemplo();
        comprobar("retrasar David 30 min (13:00-14:00) -> true", b.retrasar("David", MAR20, 30));
        comprobar("... termina al cierre", b.citaDe("David", MAR20).fin().equals(Cita.CIERRE));
        comprobar("retrasar David 1 min más -> false", !b.retrasar("David", MAR20, 1));

        // Elena ocupa toda la jornada: no se puede retrasar nada
        comprobar("retrasar Elena (jornada entera) 1 min -> false", !b.retrasar("Elena", MIE21, 1));

        // Número enorme: no debe dar la vuelta a medianoche
        comprobar("retrasar con minutos enormes -> false", !b.retrasar("David", MAR20, Integer.MAX_VALUE));
        comprobar("retrasar 1440 min (un día entero) -> false", !agendaEjemplo().retrasar("David", MAR20, 1440));

        // Usa D2: Ana, ana
        Agenda c = agendaEjemplo();
        comprobar("retrasar con otro uso de mayúsculas (CARMEN)", c.retrasar("CARMEN", MAR20, 5));
    }

    // ---------------------------------------------------------------- D2

    private static void pruebasD2() {
        comprobar("mismoCliente: igual", Agenda.mismoCliente("Ana García", "Ana García"));
        comprobar("mismoCliente: distinto uso de mayúsculas", Agenda.mismoCliente("Ana García", "ana garcía"));
        comprobar("mismoCliente: nombres distintos", !Agenda.mismoCliente("Ana", "Luis"));
        comprobar("mismoCliente: null", !Agenda.mismoCliente(null, "Ana") && !Agenda.mismoCliente("Ana", null));

        Agenda a = new Agenda();
        a.reservar("Ana García", MAR20, h(9, 0), 30);
        comprobar("D2: «ana garcía» ya tiene cita ese día -> false", !a.reservar("ana garcía", MAR20, h(11, 0), 30));
        comprobar("D2: tieneCita ignora mayúsculas", a.tieneCita("ANA GARCÍA", MAR20));
        comprobar("D2: numCitasDe ignora mayúsculas", a.numCitasDe("ana garcía") == 1);
        comprobar("D2: citaDe ignora mayúsculas", a.citaDe("ana garcía", MAR20).getInicio().equals(h(9, 0)));
        comprobar("D2: la cita guarda el nombre tal como se escribió", a.citaEn(0).getCliente().equals("Ana García"));
        comprobar("D2: en otro día sí puede reservar", a.reservar("ana garcía", MIE21, h(9, 0), 30));
        comprobar("D2: numCitasDe cuenta ambos días", a.numCitasDe("Ana García") == 2);
        a.cancelar("ANA GARCÍA", MAR20);
        comprobar("D2: cancelar ignora mayúsculas", !a.tieneCita("Ana García", MAR20) && a.numCitas() == 1);
    }

    // ---------------------------------------------------------------- precondiciones

    private static void pruebasPrecondiciones() {
        Agenda a = agendaEjemplo();

        // reservar
        esperaExcepcionSinCambios("reservar: cliente null", a, IllegalArgumentException.class,
                () -> a.reservar(null, JUE22, h(10, 0), 30));
        esperaExcepcionSinCambios("reservar: cliente vacío", a, IllegalArgumentException.class,
                () -> a.reservar("", JUE22, h(10, 0), 30));
        esperaExcepcionSinCambios("reservar: cliente solo espacios", a, IllegalArgumentException.class,
                () -> a.reservar("  ", JUE22, h(10, 0), 30));
        esperaExcepcionSinCambios("reservar: fecha null", a, IllegalArgumentException.class,
                () -> a.reservar("Zoe", null, h(10, 0), 30));
        esperaExcepcionSinCambios("reservar: inicio null", a, IllegalArgumentException.class,
                () -> a.reservar("Zoe", JUE22, null, 30));
        esperaExcepcionSinCambios("reservar: antes de la apertura", a, IllegalArgumentException.class,
                () -> a.reservar("Zoe", JUE22, h(8, 0), 30));
        esperaExcepcionSinCambios("reservar: pasa del cierre", a, IllegalArgumentException.class,
                () -> a.reservar("Zoe", JUE22, h(13, 45), 30));
        esperaExcepcionSinCambios("reservar: duración 0", a, IllegalArgumentException.class,
                () -> a.reservar("Zoe", JUE22, h(10, 0), 0));
        esperaExcepcionSinCambios("reservar: duración negativa", a, IllegalArgumentException.class,
                () -> a.reservar("Zoe", JUE22, h(10, 0), -10));
        esperaExcepcionSinCambios("reservar: vuelta a medianoche", a, IllegalArgumentException.class,
                () -> a.reservar("Zoe", JUE22, h(23, 30), 60));
        esperaExcepcionSinCambios("reservar: sábado", a, IllegalArgumentException.class,
                () -> a.reservar("Zoe", SAB24, h(10, 0), 30));

        // cancelar
        esperaExcepcionSinCambios("cancelar: cliente null", a, IllegalArgumentException.class,
                () -> a.cancelar(null, MAR20));
        esperaExcepcionSinCambios("cancelar: cliente vacío", a, IllegalArgumentException.class,
                () -> a.cancelar("", MAR20));
        esperaExcepcionSinCambios("cancelar: fecha null", a, IllegalArgumentException.class,
                () -> a.cancelar("Ana", null));
        esperaExcepcionSinCambios("cancelar: cliente sin cita ese día", a, IllegalArgumentException.class,
                () -> a.cancelar("Bruno", LUN19));
        esperaExcepcionSinCambios("cancelar: cliente inexistente", a, IllegalArgumentException.class,
                () -> a.cancelar("Nadie", MAR20));

        // retrasar
        esperaExcepcionSinCambios("retrasar: cliente null", a, IllegalArgumentException.class,
                () -> a.retrasar(null, MAR20, 5));
        esperaExcepcionSinCambios("retrasar: cliente vacío", a, IllegalArgumentException.class,
                () -> a.retrasar(" ", MAR20, 5));
        esperaExcepcionSinCambios("retrasar: fecha null", a, IllegalArgumentException.class,
                () -> a.retrasar("Ana", null, 5));
        esperaExcepcionSinCambios("retrasar: 0 minutos", a, IllegalArgumentException.class,
                () -> a.retrasar("Ana", MAR20, 0));
        esperaExcepcionSinCambios("retrasar: minutos negativos", a, IllegalArgumentException.class,
                () -> a.retrasar("Ana", MAR20, -5));
        esperaExcepcionSinCambios("retrasar: sin cita ese día", a, IllegalArgumentException.class,
                () -> a.retrasar("Bruno", LUN19, 5));

        // citaEn
        esperaExcepcionSinCambios("citaEn: posición negativa", a, IndexOutOfBoundsException.class,
                () -> a.citaEn(-1));
        esperaExcepcionSinCambios("citaEn: posición = numCitas", a, IndexOutOfBoundsException.class,
                () -> a.citaEn(a.numCitas()));
        esperaExcepcionSinCambios("citaEn: posición enorme", a, IndexOutOfBoundsException.class,
                () -> a.citaEn(1000));
        comprobar("citaEn: primera posición válida", a.citaEn(0) != null);
        comprobar("citaEn: última posición válida", a.citaEn(a.numCitas() - 1) != null);

        // citasDel, tieneCita, citaDe, numCitasDe
        esperaExcepcionSinCambios("citasDel: fecha null", a, IllegalArgumentException.class,
                () -> a.citasDel(null));
        esperaExcepcionSinCambios("tieneCita: cliente null", a, IllegalArgumentException.class,
                () -> a.tieneCita(null, MAR20));
        esperaExcepcionSinCambios("tieneCita: cliente vacío", a, IllegalArgumentException.class,
                () -> a.tieneCita("", MAR20));
        esperaExcepcionSinCambios("tieneCita: fecha null", a, IllegalArgumentException.class,
                () -> a.tieneCita("Ana", null));
        esperaExcepcionSinCambios("citaDe: sin cita", a, IllegalArgumentException.class,
                () -> a.citaDe("Bruno", LUN19));
        esperaExcepcionSinCambios("citaDe: cliente vacío", a, IllegalArgumentException.class,
                () -> a.citaDe("", MAR20));
        esperaExcepcionSinCambios("citaDe: fecha null", a, IllegalArgumentException.class,
                () -> a.citaDe("Ana", null));
        esperaExcepcionSinCambios("numCitasDe: cliente null", a, IllegalArgumentException.class,
                () -> a.numCitasDe(null));
        esperaExcepcionSinCambios("numCitasDe: cliente vacío", a, IllegalArgumentException.class,
                () -> a.numCitasDe("  "));
        comprobar("numCitasDe: cliente sin citas = 0", a.numCitasDe("Nadie") == 0);

        // hayHueco, primerHueco, primerDiaConHueco, tiempoLibre
        esperaExcepcionSinCambios("hayHueco: fecha null", a, IllegalArgumentException.class,
                () -> a.hayHueco(null, 30));
        esperaExcepcionSinCambios("hayHueco: duración 0", a, IllegalArgumentException.class,
                () -> a.hayHueco(MAR20, 0));
        esperaExcepcionSinCambios("hayHueco: duración negativa", a, IllegalArgumentException.class,
                () -> a.hayHueco(MAR20, -1));
        esperaExcepcionSinCambios("primerHueco: fecha null", a, IllegalArgumentException.class,
                () -> a.primerHueco(null, 30));
        esperaExcepcionSinCambios("primerHueco: duración 0", a, IllegalArgumentException.class,
                () -> a.primerHueco(MAR20, 0));
        esperaExcepcionSinCambios("primerHueco: no hay hueco (martes, 45 min)", a, IllegalArgumentException.class,
                () -> a.primerHueco(MAR20, 45));
        esperaExcepcionSinCambios("primerHueco: día no laborable", a, IllegalArgumentException.class,
                () -> a.primerHueco(SAB24, 30));
        esperaExcepcionSinCambios("primerDiaConHueco: fecha null", a, IllegalArgumentException.class,
                () -> a.primerDiaConHueco(null, 30));
        esperaExcepcionSinCambios("primerDiaConHueco: duración 0", a, IllegalArgumentException.class,
                () -> a.primerDiaConHueco(MAR20, 0));
        esperaExcepcionSinCambios("primerDiaConHueco: duración mayor que la jornada", a,
                IllegalArgumentException.class, () -> a.primerDiaConHueco(MAR20, Cita.JORNADA + 1));
        esperaExcepcionSinCambios("tiempoLibre: fecha null", a, IllegalArgumentException.class,
                () -> a.tiempoLibre(null));
        esperaExcepcionSinCambios("tiempoLibre: sábado", a, IllegalArgumentException.class,
                () -> a.tiempoLibre(SAB24));
        esperaExcepcionSinCambios("tiempoLibre: domingo", a, IllegalArgumentException.class,
                () -> a.tiempoLibre(DOM25));
    }

    // ---------------------------------------------------------------- postcondiciones

    private static void pruebasPostcondiciones() {
        Agenda a = agendaEjemplo();

        // citasDel devuelve una copia
        List<Cita> delMartes = a.citasDel(MAR20);
        String antes = instantanea(a);
        delMartes.clear();
        comprobar("citasDel devuelve una copia: modificarla no cambia la agenda", antes.equals(instantanea(a)));
        comprobar("citasDel de un día sin citas = lista vacía", a.citasDel(JUE22).isEmpty());
        comprobar("citasDel en fin de semana = lista vacía", a.citasDel(SAB24).isEmpty());
        comprobar("citasDel conserva el orden", clientes(a.citasDel(MAR20)).equals("Ana,Bruno,Carmen,David"));

        // Las consultas no modifican la agenda
        String s0 = instantanea(a);
        a.numCitas();
        a.citaEn(0);
        a.citasDel(MAR20);
        a.tieneCita("Ana", MAR20);
        a.citaDe("Ana", MAR20);
        a.numCitasDe("Ana");
        a.estaLibre(MAR20, h(9, 40), 20);
        a.hayHueco(MAR20, 30);
        a.primerHueco(MAR20, 30);
        a.primerDiaConHueco(MAR20, 45);
        a.tiempoLibre(MAR20);
        comprobar("ninguna consulta modifica la agenda", s0.equals(instantanea(a)));

        // reservar: postcondición cuando devuelve true
        Agenda b = new Agenda();
        comprobar("reservar true", b.reservar("Ana", MAR20, h(9, 0), 30));
        comprobar("reservar: numCitas +1", b.numCitas() == 1);
        comprobar("reservar: la cita tiene los datos pedidos",
                b.citaEn(0).getCliente().equals("Ana") && b.citaEn(0).getFecha().equals(MAR20)
                        && b.citaEn(0).getInicio().equals(h(9, 0)) && b.citaEn(0).getDuracion() == 30);
        comprobar("reservar: tieneCita pasa a ser true", b.tieneCita("Ana", MAR20));
        comprobar("reservar: el intervalo deja de estar libre", !b.estaLibre(MAR20, h(9, 0), 30));
        comprobar("reservar: tiempoLibre baja la duración", b.tiempoLibre(MAR20) == Cita.JORNADA - 30);

        // cancelar: postcondición
        int n = b.numCitas();
        b.cancelar("Ana", MAR20);
        comprobar("cancelar: numCitas -1", b.numCitas() == n - 1);
        comprobar("cancelar: tieneCita false", !b.tieneCita("Ana", MAR20));
        comprobar("cancelar: el hueco vuelve a estar libre", b.estaLibre(MAR20, h(9, 0), 30));
        comprobar("cancelar: agenda vacía otra vez", b.numCitas() == 0);

        // retrasar: postcondición
        Agenda c = new Agenda();
        c.reservar("Ana", MAR20, h(9, 0), 30);
        c.reservar("Luis", MAR20, h(12, 0), 30);
        comprobar("retrasar true", c.retrasar("Ana", MAR20, 60));
        comprobar("retrasar: nuevo inicio = inicio + minutos", c.citaDe("Ana", MAR20).getInicio().equals(h(10, 0)));
        comprobar("retrasar: misma duración", c.citaDe("Ana", MAR20).getDuracion() == 30);
        comprobar("retrasar: mismo cliente y fecha",
                c.citaDe("Ana", MAR20).getCliente().equals("Ana") && c.citaDe("Ana", MAR20).getFecha().equals(MAR20));
        comprobar("retrasar: el otro no cambia", c.citaDe("Luis", MAR20).getInicio().equals(h(12, 0)));
        comprobar("retrasar: el hueco antiguo queda libre", c.estaLibre(MAR20, h(9, 0), 60));

        // tiempoLibre y toString
        comprobar("tiempoLibre de un lunes = 270 con la cita de Ana", a.tiempoLibre(LUN19) == 270);
        comprobar("tiempoLibre de un día sin citas = 300", a.tiempoLibre(JUE22) == 300);
        String texto = a.toString();
        comprobar("toString agrupa por día (mar 20)", texto.contains("2026-10-20"));
        comprobar("toString incluye los clientes", texto.contains("Bruno") && texto.contains("Elena"));
        comprobar("toString incluye horas de fin", texto.contains("13:30"));
        long lineasDeDia = texto.lines().filter(l -> l.startsWith("2026-")).count();
        comprobar("toString: una cabecera por día (3 días)", lineasDeDia == 3);
    }

    // ---------------------------------------------------------------- demostración

    /** Muestra por pantalla el ejemplo del enunciado con todas sus consultas. */
    private static void demostracion() {
        Agenda a = agendaEjemplo();
        System.out.println("=== EJEMPLO DEL ENUNCIADO ===");
        System.out.println("Reservas en este orden: Bruno (mar 20, 10:00, 60), Ana (mar 20, 9:00, 40),");
        System.out.println("David (mar 20, 12:30, 60), Carmen (mar 20, 11:15, 45), Ana (lun 19, 13:00, 30),");
        System.out.println("Elena (mié 21, 9:00, 300)");
        System.out.println();

        System.out.println("Agenda (" + a.numCitas() + " citas):");
        System.out.printf("%-4s %-8s %-12s %-7s %-9s %s%n", "Pos.", "Cliente", "Fecha", "Inicio", "Duración", "Fin");
        for (int i = 0; i < a.numCitas(); i++) {
            Cita c = a.citaEn(i);
            System.out.printf("%-4d %-8s %-12s %-7s %-9d %s%n",
                    i, c.getCliente(), c.getFecha(), c.getInicio(), c.getDuracion(), c.fin());
        }
        System.out.println();

        System.out.println("Agenda agrupada por día (toString):");
        System.out.println(a);

        System.out.println("Consultas:");
        System.out.println("  Número de citas de Ana: " + a.numCitasDe("Ana"));
        System.out.println("  Citas del martes 20: " + clientes(a.citasDel(MAR20)));
        System.out.println("  Minutos libres martes 20 / miércoles 21: "
                + a.tiempoLibre(MAR20) + " / " + a.tiempoLibre(MIE21));
        System.out.println("  ¿Libre martes 20 a las 9:40 durante 20 min? "
                + (a.estaLibre(MAR20, h(9, 40), 20) ? "Sí" : "No"));
        System.out.println("  ¿Libre martes 20 a las 10:30 durante 15 min? "
                + (a.estaLibre(MAR20, h(10, 30), 15) ? "Sí" : "No"));
        System.out.println("  ¿Libre sábado 24 a las 10:00 durante 30 min? "
                + (a.estaLibre(SAB24, h(10, 0), 30) ? "Sí" : "No (es sábado)"));
        System.out.println("  Primer hueco de 30 min el martes 20: " + a.primerHueco(MAR20, 30));
        System.out.println("  ¿Hay hueco de 45 min el martes 20? " + (a.hayHueco(MAR20, 45) ? "Sí" : "No"));
        System.out.println("  Primer día con hueco de 45 min a partir del martes 20: "
                + a.primerDiaConHueco(MAR20, 45) + " (" + a.primerDiaConHueco(MAR20, 45).getDayOfWeek() + ")");
        System.out.println("  Primer día con hueco de 30 min a partir del sábado 24: "
                + a.primerDiaConHueco(SAB24, 30) + " (" + a.primerDiaConHueco(SAB24, 30).getDayOfWeek() + ")");
        boolean ok = a.reservar("Ana", MAR20, h(13, 30), 30);
        System.out.println("  Reservar otra cita para Ana el martes 20: "
                + (ok ? "reservada" : "No es posible: ya tiene una ese día"));
        a.cancelar("Bruno", MAR20);
        System.out.println("  Primer hueco de 45 min el martes 20 tras cancelar la cita de Bruno: "
                + a.primerHueco(MAR20, 45));
        System.out.println();

        System.out.println("=== PRUEBAS AUTOMÁTICAS ===");
    }

    // ---------------------------------------------------------------- main

    public static void main(String[] args) {
        demostracion();
        pruebasCita();
        pruebaEjemplo();
        pruebasReservarYOrden();
        pruebasSolapamientos();
        pruebasFechas();
        pruebasHuecos();
        pruebasRetrasar();
        pruebasD2();
        pruebasPrecondiciones();
        pruebasPostcondiciones();

        System.out.println(total + " comprobaciones, " + fallos + " fallos");
        if (fallos == 0) {
            System.out.println("TODO OK");
        } else {
            System.exit(1);
        }
    }
}