import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

public class PruebasPolimorfismo {

    public static void main(String[] args) throws ArgumentoException {


    Mascota mascota = new Mascota("M001", "Toby", "Ana García",
            Especie.PERRO, LocalDate.of(2020, 3, 15), 28000);

    Veterinario veterinario = new Veterinario("V1", "Laura Martín",
            Especialidad.CIRUGIA);

    LocalDate fecha = LocalDate.of(2026, 12, 1);

    CitaVeterinaria consulta = new Consulta(mascota, veterinario,
            fecha, LocalTime.of(10, 0), "Revisión");

    CitaVeterinaria cirugia = new Cirugia(mascota, veterinario,
            fecha, LocalTime.of(11, 0), 60, "Esterilización");

        System.out.println(consulta.getTipo() + ": " + consulta.precio());
        System.out.println(cirugia.getTipo() + ": " + cirugia.precio());

        if (consulta.precio() != 3500 || cirugia.precio() != 13200) {
            throw new AssertionError("Error en los precios");
        }

        System.out.println("Prueba de precios correcta");

        Cita nuevaConsulta = consulta.conInicio(LocalTime.of(12, 0));
        Cita nuevaCirugia = cirugia.conInicio(LocalTime.of(12, 0));

        if (!(nuevaConsulta instanceof Consulta) || !(nuevaCirugia instanceof Cirugia)) {
            throw new AssertionError("Error en el polimorfismo");
        }

        if (!nuevaConsulta.getInicio().equals(LocalTime.of(12, 0)) ||
                !nuevaCirugia.getInicio().equals(LocalTime.of(12, 0))) {
            throw new AssertionError("Error en conInicio");
        }

        System.out.println("Prueba de polimorfismo correcta");

        LocalDateTime ahora = LocalDateTime.of(2026, 11, 30, 10, 0);

        consulta.comprobarNormas(ahora);
        cirugia.comprobarNormas(ahora);

        System.out.println("Prueba de normas correcta");

        boolean error = false;

        try {
            CitaVeterinaria cirugiaInvalida = new Cirugia(
                    mascota, veterinario, fecha,
                    LocalTime.of(13, 30), 60, "Operación");

            cirugiaInvalida.comprobarNormas(ahora);

        } catch (ArgumentoException e) {
            error = true;
        }

        if (!error) {
            throw new AssertionError("Se permitió una cirugía fuera de horario");
        }

        System.out.println("Prueba de cirugía fuera de horario correcta");

        error = false;

        try {
            cirugia.comprobarNormas(LocalDateTime.of(2026, 12, 1, 8, 0));
        } catch (ArgumentoException e) {
            error = true;
        }

        if (!error) {
            throw new AssertionError("Se permitió una cirugía programada el mismo día");
        }

        System.out.println("Prueba de antelación correcta");

        error = false;

        try {
            new Cirugia(mascota, veterinario, fecha,
                    LocalTime.of(10, 0), 45, "Operación");
        } catch (ArgumentoException e) {
            error = true;
        }

        if (!error) {
            throw new AssertionError("Se permitió una duración incorrecta");
        }

        System.out.println("Prueba de duración correcta");




}
}

