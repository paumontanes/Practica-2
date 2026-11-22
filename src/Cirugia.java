import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Cirugia extends CitaVeterinaria {

    public static final int PRECIO_HORA = 9000;
    public static final int ANESTESIA_POR_KG = 150;
    public static final LocalTime FIN_MANANA = LocalTime.of(14, 0);
    public static final int DURACION_MINIMA = 30;
    public static final int DURACION_MAXIMA = 240;

    private final String intervencion;

    public Cirugia(Mascota mascota, Veterinario veterinario,
                   LocalDate fecha, LocalTime inicio,
                   int duracion, String intervencion)
            throws ArgumentoException {

        super(mascota, veterinario, fecha, inicio, duracion);

        if (duracion < DURACION_MINIMA || duracion > DURACION_MAXIMA
                || duracion % 30 != 0) {
            throw new ArgumentoException("Duración de cirugía no válida");
        }

        this.intervencion = texto(intervencion, "La intervención", false);
    }

    public String getIntervencion() {
        return intervencion;
    }

    @Override
    public int precio() {
        return PRECIO_HORA * getDuracion() / 60
                + ANESTESIA_POR_KG * getMascota().getPeso() / 1000;
    }


    @Override
    public String getTipo() {
        return "Cirugía";
    }

    @Override
    public Especialidad especialidadRequerida() {
        return Especialidad.CIRUGIA;
    }

    @Override
    public String descripcion() {
        return "Cirugía de " + getMascota().getNombre() + ": " + intervencion;
    }

    @Override
    protected void comprobarNormasPropias(LocalDateTime ahora) throws ArgumentoException {
        if (!getFecha().isAfter(ahora.toLocalDate())) {
            throw new ArgumentoException("La cirugía debe programarse al menos el día anterior");
        }

        if (getDuracion() > ChronoUnit.MINUTES.between(getInicio(), FIN_MANANA)) {
            throw new ArgumentoException("La cirugía debe terminar antes de las 14:00");
        }
    }

    @Override
    public Cirugia conInicio(LocalTime nuevoInicio) throws ArgumentoException {
        return new Cirugia(getMascota(), getVeterinario(),
                getFecha(), nuevoInicio, getDuracion(), intervencion);
    }


}

