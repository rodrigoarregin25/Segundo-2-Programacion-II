package gestordecine;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.AtomicMoveNotSupportedException;

/**
 * Se encarga de guardar y leer el cine en un archivo, usando serialización.
 * Es la única clase que toca el archivo de datos.
 */
public class PersistenciaCine {

    private final String rutaArchivo;
    
    /**
     * Crea el objeto de persistencia para un archivo.
     *
     * @param rutaArchivo Ruta del archivo donde se guardan los datos, por
     * ejemplo "cine_app.dat".
     */

    public PersistenciaCine(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    /**
     * Comprueba si el archivo de datos existe.
     *
     * @return true si el archivo existe, false si no.
     */
    public boolean existeArchivo() {
        File archivo = new File(rutaArchivo);
        return archivo.isFile();
    }

    /**
     * Carga el cine guardado, o crea uno nuevo si no hay archivo. Después se
     * asegura de que exista la cuenta administradora; si la tuvo que crear,
     * guarda el cine para que quede en el archivo.
     *
     * @return El cine listo para usar.
     * @throws IOException Si no se puede leer el archivo.
     * @throws ClassNotFoundException Si el archivo contiene una clase que el
     * programa no conoce.
     */
    public Cine cargarOCrear() throws IOException, ClassNotFoundException {

        Cine cine;

        if (existeArchivo()) {
            cine = cargar();
        } else {
            cine = new Cine();
        }

        // Si todavia no hay administrador, se crea la cuenta inicial
        if (cine.getServicioDeReservas().asegurarAdministrador()) {
            guardar(cine);
        }

        return cine;
    }
    
    /**
     * Guarda el cine completo en el archivo. Primero lo escribe en un archivo
     * temporal y después lo mueve al lugar del archivo final, para no dejar el
     * archivo a medias si algo falla en el medio.
     *
     * @param cine Cine que se quiere guardar.
     * @throws IOException Si no se puede escribir el archivo, o si el sistema
     * de archivos no permite reemplazarlo de forma atómica.
     */
    
    public void guardar(Cine cine) throws IOException {

        String rutaTemporal = rutaArchivo + ".tmp";

        try (
                FileOutputStream archivo = new FileOutputStream(rutaTemporal); ObjectOutputStream salida = new ObjectOutputStream(archivo)) {
            salida.writeObject(cine);
        }

        Path temporal = Path.of(rutaTemporal);
        Path destino = Path.of(rutaArchivo);

        try {
            Files.move(temporal, destino,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            throw new IOException(
                    "El sistema de archivos no permite reemplazar el archivo de forma atomica", e);
        }
    }
    /**
     * Lee el cine desde el archivo y vuelve a cargar los códigos de reserva ya
     * usados, para que no se repitan.
     *
     * @return El cine leído del archivo.
     * @throws IOException Si no se puede leer el archivo.
     * @throws ClassNotFoundException Si el archivo contiene una clase que el
     * programa no conoce.
     */
    public Cine cargar() throws IOException, ClassNotFoundException {
        try (
                FileInputStream archivo = new FileInputStream(rutaArchivo); ObjectInputStream entrada = new ObjectInputStream(archivo)) {
            Cine cine = (Cine) entrada.readObject();

            cine.getServicioDeReservas().recargarCodigos();

            return cine;
        }
    }
}