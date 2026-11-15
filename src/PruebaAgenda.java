import java.time.LocalDate;
import java.time.LocalTime;

public class PruebaAgenda {

    private static class CitaPrueba extends Cita {
        public CitaPrueba(String cliente, LocalDate fecha, LocalTime inicio, int duracion) throws ArgumentoException {
            super(cliente, fecha, inicio, duracion);
        }

        @Override
        public Cita conInicio(LocalTime nuevoInicio) throws ArgumentoException {
            return new CitaPrueba(getCliente(), getFecha(), nuevoInicio, getDuracion());
        }
    }

    public static void main(String[] args) throws ArgumentoException {
        Agenda agenda = new Agenda(LocalTime.of(9, 0), LocalTime.of(20, 0));
        LocalDate lunes = LocalDate.of(2026, 10, 19);
        LocalDate domingo = LocalDate.of(2026, 10, 25);

        Cita c1 = new CitaPrueba("M001", lunes, LocalTime.of(9, 0), 30);
        Cita c2 = new CitaPrueba("M002", lunes, LocalTime.of(10, 0), 45);
        Cita cDom = new CitaPrueba("M003", domingo, LocalTime.of(11, 0), 30);

        agenda.reservar(c1);
        agenda.reservar(c2);
        agenda.reservar(cDom);

        assert agenda.invariante() : "Fallo en invariante";
        assert agenda.getNumCitas() == 3 : "Error en getNumCitas";
        assert agenda.getCitasEntre(lunes, domingo).size() == 3 : "Error en getCitasEntre";

        agenda.retrasar("M001", lunes, 15);
        assert agenda.getCitaDe("M001", lunes).getInicio().equals(LocalTime.of(9, 15)) : "Error en retrasar";
        assert agenda.invariante() : "Fallo en invariante tras retrasar";

        assert agenda.primerHueco(lunes, 30).equals(LocalTime.of(10, 45)) : "Error en primerHueco";
        assert agenda.getTiempoLibre(lunes) == (660 - 75) : "Error en getTiempoLibre";

        boolean fallo = false;
        try {
            agenda.reservar(new CitaPrueba("M004", lunes, LocalTime.of(19, 45), 30));
        } catch (ArgumentoException e) {
            fallo = true;
        }
        assert fallo : "Debería lanzar ArgumentoException fuera de horario";

        System.out.println("Todas las pruebas de Agenda pasaron correctamente.");
    }
}
