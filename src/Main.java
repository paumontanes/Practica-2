import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class Main {

    private static class CitaPrueba extends Cita {
        CitaPrueba(String cliente, LocalDate fecha, LocalTime inicio, int duracion)
                throws ArgumentoException {
            super(cliente, fecha, inicio, duracion);
        }

        @Override
        public Cita conInicio(LocalTime nuevoInicio) throws ArgumentoException {
            return new CitaPrueba(getCliente(), getFecha(), nuevoInicio, getDuracion());
        }
    }

    /** Segundo tipo de cita, para comprobar que retrasar conserva el tipo concreto. */
    private static class CitaOtraPrueba extends Cita {
        CitaOtraPrueba(String cliente, LocalDate fecha, LocalTime inicio, int duracion)
                throws ArgumentoException {
            super(cliente, fecha, inicio, duracion);
        }

        @Override
        public Cita conInicio(LocalTime nuevoInicio) throws ArgumentoException {
            return new CitaOtraPrueba(getCliente(), getFecha(), nuevoInicio, getDuracion());
        }
    }

    /** Operación que puede lanzar ArgumentoException (para las pruebas de precondiciones). */
    private interface Accion {
        void ejecutar() throws ArgumentoException;
    }

    // Semana de las pruebas nuevas: lunes 19/10/2026 a domingo 25/10/2026.
    private static final LocalDate LUN = LocalDate.of(2026, 10, 19);
    private static final LocalDate MAR = LUN.plusDays(1);
    private static final LocalDate MIE = LUN.plusDays(2);
    private static final LocalDate JUE = LUN.plusDays(3);
    private static final LocalDate VIE = LUN.plusDays(4);
    private static final LocalDate SAB = LUN.plusDays(5);
    private static final LocalDate DOM = LUN.plusDays(6);

    private static void comprobar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new AssertionError(mensaje);
        }
    }

    public static void main(String[] args) throws ArgumentoException {
        LocalDate lunes = LocalDate.of(2026, 10, 19);
        LocalDate domingo = lunes.plusDays(6);
        LocalTime apertura = LocalTime.of(9, 0);
        LocalTime cierre = LocalTime.of(14, 0);
        Agenda agenda = new Agenda(apertura, cierre);

        Cita primera = new CitaPrueba("M001", lunes, apertura, 30);
        Cita segunda = new CitaPrueba("M002", lunes, LocalTime.of(10, 0), 45);
        Cita ultima = new CitaPrueba("M003", domingo, LocalTime.of(13, 30), 30);

        // Reservamos en desorden y añadimos una cita fuera del intervalo.
        agenda.reservar(ultima);
        agenda.reservar(segunda);
        agenda.reservar(primera);
        agenda.reservar(new CitaPrueba("M004", domingo.plusDays(1), apertura, 30));

        List<Cita> citas = agenda.getCitasEntre(lunes, domingo);
        comprobar(citas.size() == 3, "El intervalo debe incluir ambos días límite");
        comprobar(citas.get(0) == primera && citas.get(1) == segunda && citas.get(2) == ultima,
                "Citas incorrectas o desordenadas en el intervalo");
        comprobar(ultima.getFin().equals(cierre), "La cita del domingo debe acabar al cierre");

        citas.clear();
        comprobar(agenda.getNumCitas() == 4, "Modificar la lista alteró la agenda");

        // Comprobamos un retraso válido y el primer hueco que queda.
        agenda.retrasar("M001", lunes, 15);
        Cita retrasada = agenda.getCitaDe("M001", lunes);
        comprobar(retrasada.getInicio().equals(LocalTime.of(9, 15)), "Retraso incorrecto");
        comprobar(retrasada instanceof CitaPrueba, "El retraso cambió el tipo de cita");
        comprobar(primera.getInicio().equals(apertura), "Se modificó la cita original");
        comprobar(agenda.primerHueco(lunes, 30).equals(LocalTime.of(10, 45)),
                "Primer hueco incorrecto");

        // Un retraso con solapamiento debe dejar la cita intacta.
        boolean rechazo = false;
        try {
            agenda.retrasar("M001", lunes, 16);
        } catch (ArgumentoException e) {
            rechazo = true;
        }
        comprobar(rechazo, "Se permitió un retraso con solapamiento");
        comprobar(agenda.getCitaDe("M001", lunes) == retrasada,
                "El retraso fallido modificó la agenda");

        // Una cita con el cliente vacío debe rechazarse.
        rechazo = false;
        try {
            new CitaPrueba("", lunes, apertura, 30);
        } catch (ArgumentoException e) {
            rechazo = true;
        }
        comprobar(rechazo, "Se aceptó una cita con cliente vacío");

        // Una reserva solapada debe fallar sin añadir una cita.
        int numeroAntes = agenda.getNumCitas();
        rechazo = false;
        try {
            agenda.reservar(new CitaPrueba("M005", lunes, LocalTime.of(10, 15), 15));
        } catch (ArgumentoException e) {
            rechazo = true;
        }
        comprobar(rechazo, "Se aceptó una reserva solapada");
        comprobar(agenda.getNumCitas() == numeroAntes, "La reserva fallida alteró la agenda");

        comprobar(!agenda.estaLibre(lunes, LocalTime.of(23, 30), 60),
                "Se aceptó un tramo que cruza medianoche");

        // La fecha inicial debe ser anterior o igual a la fecha final.
        rechazo = false;
        try {
            agenda.getCitasEntre(domingo, lunes);
        } catch (ArgumentoException e) {
            rechazo = true;
        }
        comprobar(rechazo, "Se aceptó un intervalo de fechas invertido");

        agenda.cancelar("M002", lunes);
        comprobar(!agenda.tieneCita("M002", lunes), "La cita cancelada sigue en la agenda");

        // Pruebas adicionales: cada una usa su propia agenda.
        pruebasCita();
        pruebasConstructorAgenda();
        pruebasReservar();
        pruebasSolapes();
        pruebasHorario();
        pruebasClientes();
        pruebasCancelar();
        pruebasRetrasar();
        pruebasConsultas();
        pruebasHuecos();

        IO.println("Todas las pruebas del Hito 1 han pasado.");
    }

    // ---------------------------------------------------------------- ayudas

    private static LocalTime h(int hora, int minuto) {
        return LocalTime.of(hora, minuto);
    }

    /** Agenda con el horario de 9:00 a 14:00 (jornada de 300 minutos). */
    private static Agenda nuevaAgenda() throws ArgumentoException {
        return new Agenda(h(9, 0), h(14, 0));
    }

    private static Cita cita(String cliente, LocalDate fecha, LocalTime inicio, int duracion)
            throws ArgumentoException {
        return new CitaPrueba(cliente, fecha, inicio, duracion);
    }

    /** Clientes de la agenda en orden de posición, separados por comas. */
    private static String orden(Agenda agenda) throws ArgumentoException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < agenda.getNumCitas(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(agenda.getCitaEn(i).getCliente());
        }
        return sb.toString();
    }

    /** Clientes de una lista de citas, separados por comas. */
    private static String clientes(List<Cita> lista) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lista.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(lista.get(i).getCliente());
        }
        return sb.toString();
    }

    /** La acción debe lanzar ArgumentoException. */
    private static void esperaArgumentoException(String mensaje, Accion accion) {
        boolean rechazo = false;
        try {
            accion.ejecutar();
        } catch (ArgumentoException e) {
            rechazo = true;
        }
        comprobar(rechazo, mensaje + ": debía lanzar ArgumentoException");
    }

    /** La acción debe lanzar ArgumentoException y dejar la agenda exactamente igual. */
    private static void esperaArgumentoException(String mensaje, Agenda agenda, Accion accion)
            throws ArgumentoException {
        String antes = agenda.getNumCitas() + "|" + agenda;
        esperaArgumentoException(mensaje, accion);
        comprobar(antes.equals(agenda.getNumCitas() + "|" + agenda),
                mensaje + ": la agenda cambió");
        comprobar(agenda.invariante(), mensaje + ": se rompió el invariante");
    }

    // ---------------------------------------------------------------- Cita

    private static void pruebasCita() throws ArgumentoException {
        Cita c = cita("Ana", MAR, h(9, 0), 40);
        comprobar(c.getCliente().equals("Ana"), "getCliente");
        comprobar(c.getFecha().equals(MAR), "getFecha");
        comprobar(c.getInicio().equals(h(9, 0)), "getInicio");
        comprobar(c.getDuracion() == 40, "getDuracion");
        comprobar(c.getFin().equals(h(9, 40)), "getFin debe ser inicio + duración");
        comprobar(c.toString().equals("Ana 2026-10-20 09:00-09:40"), "toString de Cita");
        comprobar(cita("Luis", MAR, h(9, 0), 1).getFin().equals(h(9, 1)),
                "Cita de un minuto");
        comprobar(cita("Eva", MAR, h(9, 0), 300).getFin().equals(h(14, 0)),
                "Cita de toda la jornada");

        // conInicio: copia con otra hora, mismo tipo y la original intacta.
        Cita copia = c.conInicio(h(11, 0));
        comprobar(copia != c, "conInicio debe devolver otra cita");
        comprobar(copia.getInicio().equals(h(11, 0)), "conInicio: nuevo inicio");
        comprobar(copia.getCliente().equals("Ana") && copia.getFecha().equals(MAR)
                && copia.getDuracion() == 40, "conInicio debe conservar cliente, fecha y duración");
        comprobar(copia.getClass() == c.getClass(), "conInicio debe conservar el tipo");
        comprobar(c.getInicio().equals(h(9, 0)), "conInicio modificó la cita original");
        Cita otra = new CitaOtraPrueba("Eva", MAR, h(9, 0), 30);
        comprobar(otra.conInicio(h(10, 0)) instanceof CitaOtraPrueba,
                "conInicio debe devolver una cita del mismo tipo concreto");
        esperaArgumentoException("conInicio(null)", () -> c.conInicio(null));

        // Constructor: un caso por cada argumento imposible.
        esperaArgumentoException("Cita con cliente null", () -> cita(null, MAR, h(9, 0), 30));
        esperaArgumentoException("Cita con cliente vacío", () -> cita("", MAR, h(9, 0), 30));
        esperaArgumentoException("Cita con cliente solo espacios", () -> cita("   ", MAR, h(9, 0), 30));
        esperaArgumentoException("Cita con fecha null", () -> cita("Ana", null, h(9, 0), 30));
        esperaArgumentoException("Cita con inicio null", () -> cita("Ana", MAR, null, 30));
        esperaArgumentoException("Cita con duración 0", () -> cita("Ana", MAR, h(9, 0), 0));
        esperaArgumentoException("Cita con duración negativa", () -> cita("Ana", MAR, h(9, 0), -5));
    }

    // ---------------------------------------------------------------- constructor de Agenda

    private static void pruebasConstructorAgenda() throws ArgumentoException {
        Agenda a = nuevaAgenda();
        comprobar(a.getApertura().equals(h(9, 0)), "getApertura");
        comprobar(a.getCierre().equals(h(14, 0)), "getCierre");
        comprobar(a.getJornada() == 300, "getJornada de 9:00 a 14:00");
        comprobar(new Agenda(h(9, 0), h(20, 0)).getJornada() == 660, "getJornada de 9:00 a 20:00");
        comprobar(a.getNumCitas() == 0, "Una agenda nueva no tiene citas");
        comprobar(a.toString().equals("Agenda vacía"), "toString de una agenda vacía");
        comprobar(a.invariante(), "Invariante de una agenda vacía");

        esperaArgumentoException("Agenda con apertura null", () -> new Agenda(null, h(14, 0)));
        esperaArgumentoException("Agenda con cierre null", () -> new Agenda(h(9, 0), null));
        esperaArgumentoException("Agenda con apertura igual al cierre",
                () -> new Agenda(h(9, 0), h(9, 0)));
        esperaArgumentoException("Agenda con apertura posterior al cierre",
                () -> new Agenda(h(14, 0), h(9, 0)));
    }

    // ---------------------------------------------------------------- reservar y orden

    private static void pruebasReservar() throws ArgumentoException {
        Agenda a = nuevaAgenda();
        a.reservar(cita("B", MAR, h(10, 0), 30));
        comprobar(orden(a).equals("B"), "Reservar en una agenda vacía");
        a.reservar(cita("A", MAR, h(9, 0), 30));
        comprobar(orden(a).equals("A,B"), "Reservar al principio");
        a.reservar(cita("D", MAR, h(13, 0), 60));
        comprobar(orden(a).equals("A,B,D"), "Reservar al final (termina justo al cierre)");
        a.reservar(cita("C", MAR, h(11, 0), 30));
        comprobar(orden(a).equals("A,B,C,D"), "Reservar en medio");
        a.reservar(cita("X", MAR, h(9, 30), 30));
        comprobar(orden(a).equals("A,X,B,C,D"), "Cita contigua a la anterior y a la siguiente");

        // Orden por fecha y, dentro de cada fecha, por hora.
        a.reservar(cita("E", LUN, h(13, 0), 30));
        a.reservar(cita("F", MIE, h(9, 0), 30));
        a.reservar(cita("G", LUN, h(9, 0), 30));
        comprobar(orden(a).equals("G,E,A,X,B,C,D,F"),
                "Fecha anterior con hora posterior, fecha posterior con hora anterior y misma hora en días distintos");

        // La agenda abre todos los días: el fin de semana también se puede reservar.
        a.reservar(cita("S", SAB, h(10, 0), 30));
        a.reservar(cita("T", DOM, h(10, 0), 30));
        comprobar(orden(a).equals("G,E,A,X,B,C,D,F,S,T"), "Reservar sábado y domingo");
        comprobar(a.getNumCitas() == 10, "Número de citas tras las reservas");
        comprobar(a.invariante(), "Invariante tras reservar");

        // Se guarda la propia cita que se reserva.
        Cita k = cita("K", JUE, h(12, 0), 30);
        a.reservar(k);
        comprobar(a.getCitaDe("K", JUE) == k, "La agenda debe guardar la cita reservada");

        esperaArgumentoException("reservar(null)", a, () -> a.reservar(null));
    }

    private static void pruebasSolapes() throws ArgumentoException {
        Agenda a = nuevaAgenda();
        a.reservar(cita("A", MAR, h(10, 0), 60)); // 10:00-11:00

        esperaArgumentoException("Solape por el principio (9:30-10:30)", a,
                () -> a.reservar(cita("B", MAR, h(9, 30), 60)));
        esperaArgumentoException("Solape por el final (10:30-11:30)", a,
                () -> a.reservar(cita("C", MAR, h(10, 30), 60)));
        esperaArgumentoException("Cita contenida en otra (10:15-10:30)", a,
                () -> a.reservar(cita("D", MAR, h(10, 15), 15)));
        esperaArgumentoException("Cita que contiene a otra (9:00-12:00)", a,
                () -> a.reservar(cita("E", MAR, h(9, 0), 180)));
        esperaArgumentoException("Misma hora exacta (10:00-11:00)", a,
                () -> a.reservar(cita("F", MAR, h(10, 0), 60)));

        // Contiguas: la hora de fin no cuenta como ocupada.
        a.reservar(cita("G", MAR, h(9, 0), 60));  // 9:00-10:00
        a.reservar(cita("H", MAR, h(11, 0), 60)); // 11:00-12:00
        comprobar(orden(a).equals("G,A,H"), "Citas contiguas por ambos lados");
        comprobar(a.invariante(), "Invariante con citas contiguas");

        // El mismo tramo en otro día está libre.
        a.reservar(cita("I", MIE, h(10, 0), 60));
        comprobar(orden(a).equals("G,A,H,I"), "Mismo tramo en otro día");
    }

    private static void pruebasHorario() throws ArgumentoException {
        Agenda a = nuevaAgenda(); // 9:00 - 14:00
        esperaArgumentoException("Empieza antes de la apertura (8:59)", a,
                () -> a.reservar(cita("X", MAR, h(8, 59), 30)));
        esperaArgumentoException("Termina un minuto después del cierre (13:30 + 31)", a,
                () -> a.reservar(cita("X", MAR, h(13, 30), 31)));
        esperaArgumentoException("Empieza justo al cierre (14:00)", a,
                () -> a.reservar(cita("X", MAR, h(14, 0), 30)));
        esperaArgumentoException("Empieza después del cierre (15:00)", a,
                () -> a.reservar(cita("X", MAR, h(15, 0), 30)));
        esperaArgumentoException("Cruza la medianoche (23:30 + 60)", a,
                () -> a.reservar(cita("X", MAR, h(23, 30), 60)));
        esperaArgumentoException("Duración enorme", a,
                () -> a.reservar(cita("X", MAR, h(10, 0), Integer.MAX_VALUE)));

        // Casos límite válidos.
        a.reservar(cita("P", MAR, h(9, 0), 30));   // empieza justo a la apertura
        a.reservar(cita("Q", MAR, h(13, 0), 60));  // termina justo al cierre
        a.reservar(cita("R", MIE, h(9, 0), 300));  // toda la jornada
        comprobar(orden(a).equals("P,Q,R"), "Citas en los límites del horario");
        comprobar(a.invariante(), "Invariante con citas en los límites");
    }

    // ---------------------------------------------------------------- clientes (D2)

    private static void pruebasClientes() throws ArgumentoException {
        Agenda a = nuevaAgenda();
        a.reservar(cita("Ana García", MAR, h(9, 0), 30));

        esperaArgumentoException("Mismo cliente, mismo día, con hueco libre", a,
                () -> a.reservar(cita("Ana García", MAR, h(11, 0), 30)));
        esperaArgumentoException("Mismo cliente ignorando mayúsculas y minúsculas", a,
                () -> a.reservar(cita("ana garcía", MAR, h(11, 0), 30)));

        // El mismo cliente puede tener citas en días distintos.
        a.reservar(cita("ana garcía", MIE, h(9, 0), 30));
        comprobar(a.getNumCitas() == 2, "Un cliente con citas en dos días");
        comprobar(a.tieneCita("Ana García", MAR) && a.tieneCita("ANA GARCÍA", MIE),
                "tieneCita no distingue mayúsculas");
        comprobar(!a.tieneCita("Ana García", JUE), "tieneCita en un día sin cita");
        comprobar(a.getCitaDe("ana garcía", MAR).getInicio().equals(h(9, 0)),
                "getCitaDe no distingue mayúsculas");
        comprobar(a.getCitaDe("Ana García", MIE).getFecha().equals(MIE),
                "getCitaDe devuelve la cita de esa fecha");
        comprobar(a.getCitaDe("Ana García", MIE).getCliente().equals("ana garcía"),
                "La cita guarda el nombre tal como se escribió");

        a.reservar(cita("Bruno", MAR, h(11, 0), 30));
        comprobar(orden(a).equals("Ana García,Bruno,ana garcía"), "Otro cliente el mismo día");

        esperaArgumentoException("getCitaDe sin cita ese día", a, () -> a.getCitaDe("Bruno", MIE));
        esperaArgumentoException("getCitaDe de un cliente inexistente", a, () -> a.getCitaDe("Nadie", MAR));
        esperaArgumentoException("getCitaDe con cliente null", a, () -> a.getCitaDe(null, MAR));
        esperaArgumentoException("getCitaDe con cliente vacío", a, () -> a.getCitaDe("", MAR));
        esperaArgumentoException("getCitaDe con fecha null", a, () -> a.getCitaDe("Bruno", null));
        esperaArgumentoException("tieneCita con cliente null", a, () -> a.tieneCita(null, MAR));
        esperaArgumentoException("tieneCita con cliente vacío", a, () -> a.tieneCita("  ", MAR));
        esperaArgumentoException("tieneCita con fecha null", a, () -> a.tieneCita("Bruno", null));
    }

    // ---------------------------------------------------------------- cancelar

    private static void pruebasCancelar() throws ArgumentoException {
        Agenda a = nuevaAgenda();
        a.reservar(cita("A", MAR, h(9, 0), 30));
        a.reservar(cita("B", MAR, h(10, 0), 30));
        a.reservar(cita("C", MAR, h(11, 0), 30));
        a.reservar(cita("A", MIE, h(9, 0), 30));

        a.cancelar("B", MAR);
        comprobar(orden(a).equals("A,C,A"), "Cancelar conserva el orden del resto");
        comprobar(a.getNumCitas() == 3, "Cancelar quita una sola cita");
        comprobar(!a.tieneCita("B", MAR), "La cita cancelada ya no está");
        comprobar(a.estaLibre(MAR, h(10, 0), 30), "El hueco de la cita cancelada queda libre");
        comprobar(a.invariante(), "Invariante tras cancelar");

        // Se cancela solo la cita de ese día y no distingue mayúsculas.
        a.cancelar("a", MAR);
        comprobar(orden(a).equals("C,A"), "Cancelar solo la cita de ese día");
        comprobar(a.tieneCita("A", MIE) && !a.tieneCita("A", MAR), "La cita del otro día sigue");

        esperaArgumentoException("Cancelar una cita ya cancelada", a, () -> a.cancelar("B", MAR));
        esperaArgumentoException("Cancelar sin cita ese día", a, () -> a.cancelar("C", MIE));
        esperaArgumentoException("Cancelar un cliente inexistente", a, () -> a.cancelar("Nadie", MAR));
        esperaArgumentoException("Cancelar con cliente null", a, () -> a.cancelar(null, MAR));
        esperaArgumentoException("Cancelar con cliente vacío", a, () -> a.cancelar("", MAR));
        esperaArgumentoException("Cancelar con cliente solo espacios", a, () -> a.cancelar("   ", MAR));
        esperaArgumentoException("Cancelar con fecha null", a, () -> a.cancelar("C", null));
    }

    // ---------------------------------------------------------------- retrasar (D3)

    private static void pruebasRetrasar() throws ArgumentoException {
        Agenda a = nuevaAgenda();
        a.reservar(cita("A", MAR, h(9, 0), 30));                       // 9:00-9:30
        a.reservar(cita("B", MAR, h(10, 0), 30));                      // 10:00-10:30
        a.reservar(cita("C", MAR, h(13, 0), 60));                      // 13:00-14:00
        a.reservar(new CitaOtraPrueba("D", MIE, h(9, 0), 30));         // otro día y otro tipo
        Cita originalA = a.getCitaDe("A", MAR);

        // Retrasar hasta tocar a la siguiente cita es válido.
        a.retrasar("A", MAR, 30);
        Cita a2 = a.getCitaDe("A", MAR);
        comprobar(a2.getInicio().equals(h(9, 30)) && a2.getFin().equals(h(10, 0)),
                "Retrasar A 30 minutos");
        comprobar(a2.getDuracion() == 30, "Retrasar conserva la duración");
        comprobar(a2 != originalA && originalA.getInicio().equals(h(9, 0)),
                "Retrasar crea una cita nueva y no modifica la original");
        comprobar(orden(a).equals("A,B,C,D"), "Retrasar conserva el orden");
        comprobar(a.getNumCitas() == 4, "Retrasar conserva el número de citas");
        comprobar(a.invariante(), "Invariante tras retrasar");

        // Un minuto más pisa a la siguiente (D3: es un error y la agenda no cambia).
        esperaArgumentoException("Retrasar pisando a la siguiente", a, () -> a.retrasar("A", MAR, 1));

        // Retrasar B hasta tocar a C (13:00) es válido; un minuto más no.
        a.retrasar("B", MAR, 150);
        comprobar(a.getCitaDe("B", MAR).getFin().equals(h(13, 0)), "Retrasar B hasta tocar a C");
        esperaArgumentoException("Retrasar B pisando a C", a, () -> a.retrasar("B", MAR, 1));

        // C acaba al cierre: no se puede retrasar nada.
        esperaArgumentoException("Retrasar una cita que acaba al cierre", a, () -> a.retrasar("C", MAR, 1));
        esperaArgumentoException("Retrasar con minutos enormes", a,
                () -> a.retrasar("C", MAR, Integer.MAX_VALUE));
        esperaArgumentoException("Retrasar un día entero (1440 minutos)", a,
                () -> a.retrasar("A", MAR, 1440));

        // Última cita del día y de otro tipo: llega justo al cierre y conserva el tipo.
        a.retrasar("D", MIE, 270);
        Cita d2 = a.getCitaDe("D", MIE);
        comprobar(d2.getInicio().equals(h(13, 30)) && d2.getFin().equals(h(14, 0)),
                "Retrasar la última cita del día hasta el cierre");
        comprobar(d2 instanceof CitaOtraPrueba && d2.getClass() == CitaOtraPrueba.class,
                "Retrasar debe conservar el tipo de cita");
        esperaArgumentoException("Retrasar D más allá del cierre", a, () -> a.retrasar("D", MIE, 1));

        // Errores de precondición.
        esperaArgumentoException("Retrasar 0 minutos", a, () -> a.retrasar("A", MAR, 0));
        esperaArgumentoException("Retrasar minutos negativos", a, () -> a.retrasar("A", MAR, -5));
        esperaArgumentoException("Retrasar sin cita ese día", a, () -> a.retrasar("B", JUE, 5));
        esperaArgumentoException("Retrasar con cliente null", a, () -> a.retrasar(null, MAR, 5));
        esperaArgumentoException("Retrasar con cliente vacío", a, () -> a.retrasar("", MAR, 5));
        esperaArgumentoException("Retrasar con fecha null", a, () -> a.retrasar("A", null, 5));

        // La última cita de un día con la siguiente cita de otro día: solo cuenta el cierre.
        Agenda b = nuevaAgenda();
        b.reservar(cita("P", MAR, h(13, 0), 30));
        b.reservar(cita("Q", MIE, h(9, 0), 30));
        b.retrasar("P", MAR, 30);
        comprobar(b.getCitaDe("P", MAR).getFin().equals(h(14, 0)), "Retrasar P hasta el cierre");
        comprobar(b.getCitaDe("Q", MIE).getInicio().equals(h(9, 0)), "La cita de otro día no cambia");
        comprobar(orden(b).equals("P,Q") && b.invariante(), "Orden tras retrasar la última del día");
        esperaArgumentoException("Retrasar P más allá del cierre", b, () -> b.retrasar("P", MAR, 1));

        // Solo se retrasa la cita de ese día (y no se distingue mayúsculas).
        Agenda c = nuevaAgenda();
        c.reservar(cita("Ana", MAR, h(9, 0), 30));
        c.reservar(cita("Ana", MIE, h(9, 0), 30));
        c.retrasar("ana", MAR, 30);
        comprobar(c.getCitaDe("Ana", MAR).getInicio().equals(h(9, 30)), "Se retrasa la cita de ese día");
        comprobar(c.getCitaDe("Ana", MIE).getInicio().equals(h(9, 0)), "La cita del otro día no se retrasa");
    }

    // ---------------------------------------------------------------- consultas

    private static void pruebasConsultas() throws ArgumentoException {
        Agenda a = nuevaAgenda();
        a.reservar(cita("A", LUN, h(9, 0), 30));
        a.reservar(cita("B", MAR, h(9, 0), 30));
        a.reservar(cita("C", MAR, h(10, 0), 30));
        a.reservar(cita("D", MIE, h(9, 0), 30));
        a.reservar(cita("E", VIE, h(9, 0), 30));

        // getNumCitas y getCitaEn.
        comprobar(a.getNumCitas() == 5, "getNumCitas");
        comprobar(a.getCitaEn(0).getCliente().equals("A"), "getCitaEn en la primera posición");
        comprobar(a.getCitaEn(a.getNumCitas() - 1).getCliente().equals("E"),
                "getCitaEn en la última posición");
        esperaArgumentoException("getCitaEn(-1)", a, () -> a.getCitaEn(-1));
        esperaArgumentoException("getCitaEn(numCitas)", a, () -> a.getCitaEn(a.getNumCitas()));
        esperaArgumentoException("getCitaEn(1000)", a, () -> a.getCitaEn(1000));

        // getCitasDel.
        comprobar(clientes(a.getCitasDel(MAR)).equals("B,C"), "getCitasDel con orden");
        comprobar(a.getCitasDel(JUE).isEmpty(), "getCitasDel de un día sin citas");
        comprobar(a.getCitasDel(SAB).isEmpty(), "getCitasDel de un sábado sin citas");
        List<Cita> delMartes = a.getCitasDel(MAR);
        delMartes.clear();
        comprobar(a.getNumCitas() == 5 && clientes(a.getCitasDel(MAR)).equals("B,C"),
                "getCitasDel debe devolver una copia");
        esperaArgumentoException("getCitasDel(null)", a, () -> a.getCitasDel(null));

        // getCitasEntre: ambas fechas incluidas y en orden.
        comprobar(clientes(a.getCitasEntre(MAR, MIE)).equals("B,C,D"), "getCitasEntre incluye ambas fechas");
        comprobar(clientes(a.getCitasEntre(LUN, VIE)).equals("A,B,C,D,E"), "getCitasEntre de toda la semana");
        comprobar(clientes(a.getCitasEntre(MAR, MAR)).equals("B,C"), "getCitasEntre de un solo día");
        comprobar(a.getCitasEntre(JUE, JUE).isEmpty(), "getCitasEntre de un día sin citas");
        comprobar(a.getCitasEntre(SAB, DOM).isEmpty(), "getCitasEntre de un fin de semana sin citas");
        comprobar(clientes(a.getCitasEntre(JUE, DOM)).equals("E"), "getCitasEntre excluye lo anterior");
        List<Cita> entre = a.getCitasEntre(LUN, VIE);
        entre.clear();
        comprobar(a.getNumCitas() == 5, "getCitasEntre debe devolver una copia");
        esperaArgumentoException("getCitasEntre con fechas invertidas", a, () -> a.getCitasEntre(VIE, LUN));
        esperaArgumentoException("getCitasEntre con desde null", a, () -> a.getCitasEntre(null, VIE));
        esperaArgumentoException("getCitasEntre con hasta null", a, () -> a.getCitasEntre(LUN, null));

        // Ninguna consulta modifica la agenda.
        String antes = a.getNumCitas() + "|" + a;
        a.getNumCitas();
        a.getCitaEn(0);
        a.getCitasDel(MAR);
        a.getCitasEntre(LUN, VIE);
        a.tieneCita("A", LUN);
        a.getCitaDe("A", LUN);
        a.estaLibre(MAR, h(11, 0), 30);
        a.hayHueco(MAR, 30);
        a.primerHueco(MAR, 30);
        a.primerDiaConHueco(MAR, 30);
        a.getTiempoLibre(MAR);
        a.getApertura();
        a.getCierre();
        a.getJornada();
        comprobar(antes.equals(a.getNumCitas() + "|" + a), "Alguna consulta modificó la agenda");

        // toString agrupa las citas por día.
        String texto = a.toString();
        comprobar(texto.contains("2026-10-20 (TUESDAY)"), "toString: cabecera con fecha y día");
        comprobar(texto.contains("B") && texto.contains("E") && texto.contains("09:30"),
                "toString: clientes y horas");
        long cabeceras = texto.lines().filter(l -> l.startsWith("2026-")).count();
        comprobar(cabeceras == 4, "toString: una cabecera por cada día con citas");
    }

    // ---------------------------------------------------------------- huecos

    private static void pruebasHuecos() throws ArgumentoException {
        Agenda a = nuevaAgenda(); // 9:00 - 14:00

        // Día sin citas.
        comprobar(a.primerHueco(MAR, 60).equals(h(9, 0)), "Primer hueco de un día vacío = apertura");
        comprobar(a.hayHueco(MAR, 300), "Hay hueco de toda la jornada en un día vacío");
        comprobar(!a.hayHueco(MAR, 301), "No hay hueco mayor que la jornada");
        comprobar(a.getTiempoLibre(MAR) == 300, "Tiempo libre de un día vacío = jornada");

        // estaLibre en los límites del horario.
        comprobar(a.estaLibre(MAR, h(9, 0), 300), "Jornada completa libre");
        comprobar(!a.estaLibre(MAR, h(8, 59), 30), "estaLibre antes de la apertura");
        comprobar(!a.estaLibre(MAR, h(13, 30), 31), "estaLibre pasando del cierre");
        comprobar(!a.estaLibre(MAR, h(14, 0), 30), "estaLibre empezando al cierre");
        comprobar(!a.estaLibre(MAR, h(23, 30), 60), "estaLibre cruzando la medianoche");

        // Hueco al principio del día.
        a.reservar(cita("A", MAR, h(10, 0), 60)); // 10:00-11:00
        comprobar(a.primerHueco(MAR, 60).equals(h(9, 0)), "Hueco al principio del día");
        comprobar(a.primerHueco(MAR, 61).equals(h(11, 0)), "Si no cabe al principio, va después de la cita");

        // Hueco entre dos citas.
        a.reservar(cita("B", MAR, h(12, 0), 60)); // 12:00-13:00
        a.reservar(cita("C", MAR, h(9, 0), 60));  // 9:00-10:00
        comprobar(a.primerHueco(MAR, 60).equals(h(11, 0)), "Hueco entre dos citas");
        comprobar(a.estaLibre(MAR, h(11, 0), 60), "Hueco justo del tamaño pedido");
        comprobar(!a.estaLibre(MAR, h(11, 0), 61), "Un minuto más de lo que cabe");
        comprobar(!a.estaLibre(MAR, h(10, 59), 30), "Tramo que pisa el final de una cita");

        // Hueco al final del día y otro día no afectado.
        comprobar(a.estaLibre(MAR, h(13, 0), 60), "Hueco al final del día");
        comprobar(a.estaLibre(MIE, h(10, 0), 60), "Otro día no se ve afectado");
        comprobar(a.getTiempoLibre(MAR) == 120, "Tiempo libre con tres citas de 60 minutos");

        // Día sin hueco.
        a.reservar(cita("D", MAR, h(11, 0), 60));
        a.reservar(cita("E", MAR, h(13, 0), 60));
        comprobar(!a.hayHueco(MAR, 1), "Día lleno: no hay hueco de 1 minuto");
        comprobar(a.getTiempoLibre(MAR) == 0, "Día lleno: tiempo libre 0");
        comprobar(a.invariante(), "Invariante con el día lleno");
        esperaArgumentoException("primerHueco en un día lleno", a, () -> a.primerHueco(MAR, 1));

        // primerDiaConHueco: salta días llenos; la agenda abre todos los días.
        comprobar(a.primerDiaConHueco(MAR, 30).equals(MIE), "Salta el día lleno");
        comprobar(a.primerDiaConHueco(LUN, 30).equals(LUN), "Si el día tiene hueco, devuelve ese día");
        a.reservar(cita("F", MIE, h(9, 0), 300));
        comprobar(a.primerDiaConHueco(MAR, 30).equals(JUE), "Salta varios días llenos");
        a.reservar(cita("G", JUE, h(10, 0), 30));
        comprobar(a.primerDiaConHueco(MAR, 300).equals(VIE), "Hueco de jornada completa: salta el día con una cita");
        comprobar(a.primerDiaConHueco(SAB, 30).equals(SAB), "Un sábado vacío tiene hueco");

        // Cancelar reabre el hueco.
        a.cancelar("D", MAR);
        comprobar(a.primerHueco(MAR, 60).equals(h(11, 0)), "Cancelar reabre el hueco");
        comprobar(a.primerDiaConHueco(MAR, 60).equals(MAR), "Tras cancelar, el martes vuelve a tener hueco");

        // Errores de precondición.
        esperaArgumentoException("estaLibre con fecha null", a, () -> a.estaLibre(null, h(10, 0), 30));
        esperaArgumentoException("estaLibre con hora null", a, () -> a.estaLibre(MAR, null, 30));
        esperaArgumentoException("estaLibre con duración 0", a, () -> a.estaLibre(MAR, h(10, 0), 0));
        esperaArgumentoException("hayHueco con fecha null", a, () -> a.hayHueco(null, 30));
        esperaArgumentoException("hayHueco con duración 0", a, () -> a.hayHueco(MAR, 0));
        esperaArgumentoException("hayHueco con duración negativa", a, () -> a.hayHueco(MAR, -1));
        esperaArgumentoException("primerHueco con fecha null", a, () -> a.primerHueco(null, 30));
        esperaArgumentoException("primerHueco con duración 0", a, () -> a.primerHueco(MAR, 0));
        esperaArgumentoException("primerDiaConHueco con fecha null", a, () -> a.primerDiaConHueco(null, 30));
        esperaArgumentoException("primerDiaConHueco con duración 0", a, () -> a.primerDiaConHueco(MAR, 0));
        esperaArgumentoException("primerDiaConHueco con duración mayor que la jornada", a,
                () -> a.primerDiaConHueco(MAR, 301));
        esperaArgumentoException("getTiempoLibre con fecha null", a, () -> a.getTiempoLibre(null));
    }
}
