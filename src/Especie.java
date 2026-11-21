public enum Especie {
    PERRO("Perro", 30, Especialidad.GENERAL),
    GATO("Gato", 30, Especialidad.GENERAL),
    EXOTICO("Exótico", 45, Especialidad.EXOTICOS);

    private final String nombre;
    private final int duracionConsulta;
    private final Especialidad especialidad;

    private Especie(String nombre, int duracionConsulta, Especialidad especialidad) {
        this.nombre = nombre;
        this.duracionConsulta = duracionConsulta;
        this.especialidad = especialidad;
    }

    public String getNombre() {
        return nombre;
    }

    public int getDuracionConsulta() {
        return duracionConsulta;
    }

    public Especialidad getEspecialidad() {
        return especialidad;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
