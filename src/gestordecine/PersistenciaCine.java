package gestordecine;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.AtomicMoveNotSupportedException;

public class PersistenciaCine {

    private final String rutaArchivo;

    public PersistenciaCine(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public boolean existeArchivo() {
        File archivo = new File(rutaArchivo);
        return archivo.isFile();
    }

    public Cine cargarOCrear() throws IOException, ClassNotFoundException {

        if (existeArchivo()) {
            return cargar();
        }

        return new Cine();
    }

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

    public Cine cargar() throws IOException, ClassNotFoundException {
        try (
                FileInputStream archivo = new FileInputStream(rutaArchivo); ObjectInputStream entrada = new ObjectInputStream(archivo)) {
            Cine cine = (Cine) entrada.readObject();

            cine.getServicioDeReservas().recargarCodigos();

            return cine;
        }
    }
}
