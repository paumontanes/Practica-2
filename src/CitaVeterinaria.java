import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.DayOfWeek;

public abstract class CitaVeterinaria extends Cita {

    private Resultado resultado;
    private String notas;

    private final Mascota mascota;
    private final Veterinario veterinario;

    protected CitaVeterinaria(Mascota mascota, Veterinario veterinario,
                              LocalDate fecha, LocalTime inicio, int duracion)
            throws ArgumentoException {

        super(mascota == null ? null : mascota.getId(),
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

    public final void comprobarNormas(LocalDateTime ahora)
            throws ArgumentoException {

        if (ahora == null) {
            throw new ArgumentoException("La fecha actual no puede ser nula");
        }

        if (getFecha().getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new ArgumentoException("No se admiten citas los domingos");
        }

        if (getInicio().isBefore(LocalTime.of(9, 0))
                || getFin().isAfter(LocalTime.of(20, 0))) {
            throw new ArgumentoException("La cita está fuera del horario de la clínica");
        }

        if (LocalDateTime.of(getFecha(), getInicio()).isBefore(ahora)) {
            throw new ArgumentoException("No se pueden reservar citas pasadas");
        }

        if (!veterinario.puedeAtender(especialidadRequerida())) {
            throw new ArgumentoException("El veterinario no tiene la especialidad requerida");
        }

        comprobarNormasPropias(ahora);
    }

    protected void comprobarNormasPropias(LocalDateTime ahora)
            throws ArgumentoException {
    }

    public final void terminar(Resultado resultado, String notas)
            throws ArgumentoException {

        if (resultado == null) {
            throw new ArgumentoException("El resultado no puede ser nulo");
        }

        if (this.resultado != null) {
            throw new ArgumentoException("La cita ya está terminada");
        }

        this.resultado = resultado;
        this.notas = notas;
    }

    public final int ingreso() {
        if (resultado == Resultado.ATENDIDA) {
            return precio();
        }

        return 0;
    }

    protected final String camposComunes() {
        return "mascota=" + mascota
                + ", veterinario=" + veterinario
                + ", resultado=" + resultado
                + ", notas=" + notas;
    }

    @Override
    public String toString() {
        return super.toString() + " " + camposComunes();
    }

    protected final String texto(String s, String que,
                                 boolean vacioPermitido)
            throws ArgumentoException {

        if (s == null) {
            throw new ArgumentoException(que + " no puede ser nulo");
        }

        String texto = s.trim();

        if (!vacioPermitido && texto.isEmpty()) {
            throw new ArgumentoException(que + " no puede estar vacío");
        }

        return texto;
    }
}