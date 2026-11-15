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

        IO.println("Todas las pruebas del Hito 1 han pasado.");
    }
}
