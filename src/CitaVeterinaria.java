import java.time.LocalDate;
import java.time.LocalTime;

public abstract class CitaVeterinaria extends Cita {

    private Resultado resultado;
    private String notas;

    private final Mascota mascota;
    private final Veterinario veterinario;

    protected CitaVeterinaria(Mascota mascota, Veterinario veterinario,
                              LocalDate fecha, LocalTime inicio, int duracion)
            throws ArgumentoException {

        super(mascota == null ? null : mascota.getPropietario(),
                fecha, inicio, duracion);

        if (mascota == null) {
            throw new ArgumentoException("La mascota no puede ser nula");
        }

        if (veterinario == null) {
            throw new ArgumentoException("El veterinario no puede ser nulo");
        }

        this.mascota = mascota;
        this.veterinario = veterinario;
        this.resultado = null;
        this.notas = null;
    }

    public final Mascota getMascota() {
        return mascota;
    }

    public final Veterinario getVeterinario() {
        return veterinario;
    }

    public final Resultado getResultado() {
        return resultado;
    }

    public final String getNotas() {
        return notas;
    }

    public abstract int precio();

    public abstract String getTipo();

    public abstract Especialidad especialidadRequerida();

    public abstract String descripcion();
}
