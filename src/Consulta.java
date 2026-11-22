import java.time.LocalDate;
import java.time.LocalTime;

public class Consulta extends CitaVeterinaria {

    public static final int PRECIO = 3500;
    private final String motivo;

    public Consulta(Mascota mascota, Veterinario veterinario,
                    LocalDate fecha, LocalTime inicio, String motivo)
            throws ArgumentoException {

        super(mascota, veterinario, fecha, inicio, duracionDe(mascota));
        this.motivo = texto(motivo, "El motivo", false);
    }

    private static int duracionDe(Mascota mascota) throws ArgumentoException {
        if (mascota == null) {
            throw new ArgumentoException("La mascota no puede ser nula");
        }
        return mascota.getEspecie().getDuracionConsulta();
    }
}

