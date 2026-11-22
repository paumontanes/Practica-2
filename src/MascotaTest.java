import java.time.LocalDate;

public class MascotaTest {

    public static void main(String[] args) {
        try {
            testEnumerados();
            testCreacionYGetters();
            testArgumentosInvalidos();
            testEdadInvalida();
            System.out.println("Todas las pruebas de Mascota y enumerados han pasado correctamente.");
        } catch (Exception e) {
            System.err.println("Error en las pruebas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void testEnumerados() {
        comprobar("Perro".equals(Especie.PERRO.getNombre()), "Nombre de PERRO incorrecto");
        comprobar(Especie.PERRO.getDuracionConsulta() == 30, "Duración de PERRO incorrecta");
        comprobar(Especie.PERRO.getEspecialidad() == Especialidad.GENERAL, "Especialidad de PERRO incorrecta");

        comprobar("Gato".equals(Especie.GATO.getNombre()), "Nombre de GATO incorrecto");
        comprobar(Especie.GATO.getDuracionConsulta() == 30, "Duración de GATO incorrecta");
        comprobar(Especie.GATO.getEspecialidad() == Especialidad.GENERAL, "Especialidad de GATO incorrecta");

        comprobar("Exótico".equals(Especie.EXOTICO.getNombre()), "Nombre de EXOTICO incorrecto");
        comprobar(Especie.EXOTICO.getDuracionConsulta() == 45, "Duración de EXOTICO incorrecta");
        comprobar(Especie.EXOTICO.getEspecialidad() == Especialidad.EXOTICOS, "Especialidad de EXOTICO incorrecta");
    }

    public static void testCreacionYGetters() throws ArgumentoException {
        LocalDate nac = LocalDate.of(2020, 3, 15);
        Mascota toby = new Mascota("M001", "Toby", "Ana García", Especie.PERRO, nac, 28000);

        comprobar("M001".equals(toby.getId()), "ID incorrecto");
        comprobar("Toby".equals(toby.getNombre()), "Nombre incorrecto");
        comprobar("Ana García".equals(toby.getPropietario()), "Propietario incorrecto");
        comprobar(toby.getEspecie() == Especie.PERRO, "Especie incorrecta");
        comprobar(nac.equals(toby.getNacimiento()), "Nacimiento incorrecto");
        comprobar(toby.getPeso() == 28000, "Peso incorrecto");
        comprobar(toby.getEdad(LocalDate.of(2026, 10, 16)) == 6, "Edad incorrecta");
    }

    public static void testArgumentosInvalidos() {
        LocalDate nac = LocalDate.of(2022, 6, 1);

        comprobarExcepcionMascota(null, "Misi", "Bruno", Especie.GATO, nac, 4000, "id null");
        comprobarExcepcionMascota("   ", "Misi", "Bruno", Especie.GATO, nac, 4000, "id vacío");
        comprobarExcepcionMascota("M002", null, "Bruno", Especie.GATO, nac, 4000, "nombre null");
        comprobarExcepcionMascota("M002", "Misi", "", Especie.GATO, nac, 4000, "propietario vacío");
        comprobarExcepcionMascota("M002", "Misi", "Bruno", null, nac, 4000, "especie null");
        comprobarExcepcionMascota("M002", "Misi", "Bruno", Especie.GATO, null, 4000, "nacimiento null");
        comprobarExcepcionMascota("M002", "Misi", "Bruno", Especie.GATO, nac, 0, "peso 0");
        comprobarExcepcionMascota("M002", "Misi", "Bruno", Especie.GATO, nac, -500, "peso negativo");
    }

    public static void testEdadInvalida() throws ArgumentoException {
        LocalDate nac = LocalDate.of(2022, 6, 1);
        Mascota misi = new Mascota("M002", "Misi", "Bruno López", Especie.GATO, nac, 4000);

        boolean lanzoConNull = false;
        try {
            misi.getEdad(null);
        } catch (ArgumentoException e) {
            lanzoConNull = true;
        }
        comprobar(lanzoConNull, "getEdad(null) debería lanzar ArgumentoException");

        boolean lanzoFechaAnterior = false;
        try {
            misi.getEdad(LocalDate.of(2021, 1, 1));
        } catch (ArgumentoException e) {
            lanzoFechaAnterior = true;
        }
        comprobar(lanzoFechaAnterior, "getEdad anterior al nacimiento debería lanzar ArgumentoException");
    }

    private static void comprobar(boolean condicion, String mensajeError) {
        if (!condicion) {
            throw new AssertionError(mensajeError);
        }
    }

    private static void comprobarExcepcionMascota(String id, String nombre, String propietario,
            Especie especie, LocalDate nacimiento, int peso, String caso) {
        boolean lanzoExcepcion = false;
        try {
            new Mascota(id, nombre, propietario, especie, nacimiento, peso);
        } catch (ArgumentoException e) {
            lanzoExcepcion = true;
        }
        comprobar(lanzoExcepcion, "Debería lanzar ArgumentoException con: " + caso);
    }
}
