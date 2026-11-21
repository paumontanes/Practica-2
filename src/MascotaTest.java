import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

public class MascotaTest {

    @Test
    public void testEnumerados() {
        assertEquals("Perro", Especie.PERRO.getNombre());
        assertEquals(30, Especie.PERRO.getDuracionConsulta());
        assertEquals(Especialidad.GENERAL, Especie.PERRO.getEspecialidad());

        assertEquals("Gato", Especie.GATO.getNombre());
        assertEquals(30, Especie.GATO.getDuracionConsulta());
        assertEquals(Especialidad.GENERAL, Especie.GATO.getEspecialidad());

        assertEquals("Exótico", Especie.EXOTICO.getNombre());
        assertEquals(45, Especie.EXOTICO.getDuracionConsulta());
        assertEquals(Especialidad.EXOTICOS, Especie.EXOTICO.getEspecialidad());
    }

    @Test
    public void testCreacionYGetters() throws ArgumentoException {
        LocalDate nac = LocalDate.of(2020, 3, 15);
        Mascota toby = new Mascota("M001", "Toby", "Ana García", Especie.PERRO, nac, 28000);

        assertEquals("M001", toby.getId());
        assertEquals("Toby", toby.getNombre());
        assertEquals("Ana García", toby.getPropietario());
        assertEquals(Especie.PERRO, toby.getEspecie());
        assertEquals(nac, toby.getNacimiento());
        assertEquals(28000, toby.getPeso());
        assertEquals(6, toby.getEdad(LocalDate.of(2026, 10, 16)));
    }

    @Test
    public void testArgumentosInvalidos() {
        LocalDate nac = LocalDate.of(2022, 6, 1);

        assertThrows(ArgumentoException.class, () -> new Mascota(null, "Misi", "Bruno", Especie.GATO, nac, 4000));
        assertThrows(ArgumentoException.class, () -> new Mascota("   ", "Misi", "Bruno", Especie.GATO, nac, 4000));
        assertThrows(ArgumentoException.class, () -> new Mascota("M002", null, "Bruno", Especie.GATO, nac, 4000));
        assertThrows(ArgumentoException.class, () -> new Mascota("M002", "Misi", "", Especie.GATO, nac, 4000));
        assertThrows(ArgumentoException.class, () -> new Mascota("M002", "Misi", "Bruno", null, nac, 4000));
        assertThrows(ArgumentoException.class, () -> new Mascota("M002", "Misi", "Bruno", Especie.GATO, null, 4000));
        assertThrows(ArgumentoException.class, () -> new Mascota("M002", "Misi", "Bruno", Especie.GATO, nac, 0));
        assertThrows(ArgumentoException.class, () -> new Mascota("M002", "Misi", "Bruno", Especie.GATO, nac, -500));
    }

    @Test
    public void testEdadInvalida() throws ArgumentoException {
        LocalDate nac = LocalDate.of(2022, 6, 1);
        Mascota misi = new Mascota("M002", "Misi", "Bruno López", Especie.GATO, nac, 4000);

        assertThrows(ArgumentoException.class, () -> misi.getEdad(null));
        assertThrows(ArgumentoException.class, () -> misi.getEdad(LocalDate.of(2021, 1, 1)));
    }
}
