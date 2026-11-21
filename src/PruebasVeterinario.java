import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Pruebas de Veterinario (y de ClinicaException). Se ejecuta con main().
 * No usa assert (que está desactivado por defecto): lanza AssertionError con comprobar().
 */
public class PruebasVeterinario {

    /** Acción que puede lanzar una excepción comprobada. */
    private interface Accion {
        void ejecutar() throws Exception;
    }

    private static void comprobar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new AssertionError("FALLA: " + mensaje);
        }
    }

    /** Comprueba que la acción lanza exactamente ArgumentoException. */
    private static void esperaArgumentoException(String mensaje, Accion accion) {
        try {
            accion.ejecutar();
        } catch (ArgumentoException e) {
            return;
        } catch (Exception e) {
            throw new AssertionError("FALLA: " + mensaje + " lanzó " + e.getClass().getSimpleName());
        }
        throw new AssertionError("FALLA: " + mensaje + " no lanzó ArgumentoException");
    }

    public static void main(String[] args) throws Exception {
        pruebasCreacion();
        pruebasPuedeAtender();
        pruebasAgenda();
        pruebasArgumentosInvalidos();
        pruebasSerializacion();
        pruebasClinicaException();
        System.out.println("Todas las pruebas de Veterinario han pasado.");
    }

    // ------------------------------------------------------------ creación y getters

    private static void pruebasCreacion() throws Exception {
        Veterinario laura = new Veterinario("V1", "Laura Martín", Especialidad.GENERAL);
        comprobar(laura.getCodigo().equals("V1"), "getCodigo");
        comprobar(laura.getNombre().equals("Laura Martín"), "getNombre");
        comprobar(laura.getEspecialidad() == Especialidad.GENERAL, "getEspecialidad");
        comprobar(laura.toString().equals("V1 - Laura Martín (GENERAL)"), "toString: " + laura);

        // los espacios sobrantes se recortan
        Veterinario pablo = new Veterinario("  V2 ", " Pablo Ortega  ", Especialidad.CIRUGIA);
        comprobar(pablo.getCodigo().equals("V2"), "el código se recorta");
        comprobar(pablo.getNombre().equals("Pablo Ortega"), "el nombre se recorta");
    }

    // ------------------------------------------------------------ regla de especialidades

    private static void pruebasPuedeAtender() throws Exception {
        Veterinario laura = new Veterinario("V1", "Laura Martín", Especialidad.GENERAL);
        Veterinario pablo = new Veterinario("V2", "Pablo Ortega", Especialidad.CIRUGIA);
        Veterinario elena = new Veterinario("V3", "Elena Sanz", Especialidad.EXOTICOS);

        comprobar(laura.puedeAtender(Especialidad.GENERAL), "General atiende General");
        comprobar(!laura.puedeAtender(Especialidad.CIRUGIA), "General no atiende Cirugía");
        comprobar(!laura.puedeAtender(Especialidad.EXOTICOS), "General no atiende Exóticos");

        comprobar(pablo.puedeAtender(Especialidad.GENERAL), "Cirugía atiende General");
        comprobar(pablo.puedeAtender(Especialidad.CIRUGIA), "Cirugía atiende Cirugía");
        comprobar(!pablo.puedeAtender(Especialidad.EXOTICOS), "Cirugía no atiende Exóticos");

        comprobar(elena.puedeAtender(Especialidad.GENERAL), "Exóticos atiende General");
        comprobar(!elena.puedeAtender(Especialidad.CIRUGIA), "Exóticos no atiende Cirugía");
        comprobar(elena.puedeAtender(Especialidad.EXOTICOS), "Exóticos atiende Exóticos");

        comprobar(!laura.puedeAtender(null), "con null no puede atender");
        comprobar(!pablo.puedeAtender(null), "con null no puede atender (Cirugía)");
    }

    // ------------------------------------------------------------ agenda propia

    private static void pruebasAgenda() throws Exception {
        Veterinario laura = new Veterinario("V1", "Laura Martín", Especialidad.GENERAL);
        Veterinario pablo = new Veterinario("V2", "Pablo Ortega", Especialidad.CIRUGIA);

        comprobar(laura.getAgenda() != null, "tiene agenda");
        comprobar(laura.getAgenda() == laura.getAgenda(), "getAgenda devuelve siempre la misma");
        comprobar(laura.getAgenda() != pablo.getAgenda(), "cada veterinario tiene su agenda");
        comprobar(laura.getAgenda().getApertura().equals(LocalTime.of(9, 0)), "apertura 9:00");
        comprobar(laura.getAgenda().getCierre().equals(LocalTime.of(20, 0)), "cierre 20:00");
        comprobar(laura.getAgenda().getNumCitas() == 0, "la agenda empieza vacía");

        // una cita en la agenda de Laura no aparece en la de Pablo
        LocalDate lunes = LocalDate.of(2026, 10, 19);
        laura.getAgenda().reservar(new CitaPrueba("M001", lunes, LocalTime.of(9, 0), 30));
        comprobar(laura.getAgenda().getNumCitas() == 1, "la cita está en la agenda de Laura");
        comprobar(pablo.getAgenda().getNumCitas() == 0, "la agenda de Pablo sigue vacía");

        // el horario es 9:00-20:00: se admite una cita a las 19:30 y no una a las 8:30
        laura.getAgenda().reservar(new CitaPrueba("M002", lunes, LocalTime.of(19, 30), 30));
        comprobar(laura.getAgenda().getNumCitas() == 2, "cita que termina justo a las 20:00");
        esperaArgumentoException("cita antes de las 9:00",
                () -> laura.getAgenda().reservar(new CitaPrueba("M003", lunes, LocalTime.of(8, 30), 30)));
        esperaArgumentoException("cita que acaba después de las 20:00",
                () -> laura.getAgenda().reservar(new CitaPrueba("M004", lunes, LocalTime.of(19, 45), 30)));
    }

    // ------------------------------------------------------------ argumentos inválidos

    private static void pruebasArgumentosInvalidos() {
        esperaArgumentoException("código null",
                () -> new Veterinario(null, "x", Especialidad.GENERAL));
        esperaArgumentoException("código vacío",
                () -> new Veterinario("", "x", Especialidad.GENERAL));
        esperaArgumentoException("código en blanco",
                () -> new Veterinario("   ", "x", Especialidad.GENERAL));
        esperaArgumentoException("nombre null",
                () -> new Veterinario("V9", null, Especialidad.GENERAL));
        esperaArgumentoException("nombre vacío",
                () -> new Veterinario("V9", "", Especialidad.GENERAL));
        esperaArgumentoException("nombre en blanco",
                () -> new Veterinario("V9", "  ", Especialidad.GENERAL));
        esperaArgumentoException("especialidad null",
                () -> new Veterinario("V9", "x", null));
    }

    // ------------------------------------------------------------ Serializable

    private static void pruebasSerializacion() throws Exception {
        Veterinario original = new Veterinario("V2", "Pablo Ortega", Especialidad.CIRUGIA);
        LocalDate martes = LocalDate.of(2026, 10, 20);
        original.getAgenda().reservar(new CitaPrueba("M001", martes, LocalTime.of(9, 0), 120));

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream salida = new ObjectOutputStream(bytes)) {
            salida.writeObject(original);
        }
        Veterinario copia;
        try (ObjectInputStream entrada =
                     new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            copia = (Veterinario) entrada.readObject();
        }

        comprobar(copia != original, "la copia es otro objeto");
        comprobar(copia.getCodigo().equals("V2"), "código tras cargar");
        comprobar(copia.getNombre().equals("Pablo Ortega"), "nombre tras cargar");
        comprobar(copia.getEspecialidad() == Especialidad.CIRUGIA, "especialidad tras cargar");
        comprobar(copia.getAgenda().getNumCitas() == 1, "la agenda conserva sus citas");
        comprobar(copia.getAgenda().tieneCita("M001", martes), "la cita de M001 sigue ahí");
    }

    // ------------------------------------------------------------ ClinicaException

    private static void pruebasClinicaException() {
        ClinicaException e = new ClinicaException("La clínica cierra los domingos");
        comprobar(e.getMessage().equals("La clínica cierra los domingos"), "mensaje de ClinicaException");
        // Que es comprobada (no RuntimeException) lo garantiza el compilador:
        // un método que la lance debe declararla con throws.
    }

    /** Subclase mínima de Cita, solo para llenar la agenda en las pruebas. */
    private static class CitaPrueba extends Cita {
        private static final long serialVersionUID = 1L;

        CitaPrueba(String cliente, LocalDate fecha, LocalTime inicio, int duracion)
                throws ArgumentoException {
            super(cliente, fecha, inicio, duracion);
        }

        @Override
        public Cita conInicio(LocalTime nuevoInicio) throws ArgumentoException {
            return new CitaPrueba(getCliente(), getFecha(), nuevoInicio, getDuracion());
        }
    }
}
