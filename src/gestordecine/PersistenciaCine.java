package gestordecine;

import java.io.*;

public class PersistenciaCine {

    private final String rutaArchivo;

    public PersistenciaCine(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public boolean existeArchivo() {
        File archivo = new File(rutaArchivo);
        return archivo.isFile();
    }

    public void guardar(Cine cine) throws IOException {

        try (
                FileOutputStream archivo = new FileOutputStream(rutaArchivo); ObjectOutputStream salida = new ObjectOutputStream(archivo)) {
            salida.writeObject(cine);
        }
    }

    public Cine cargar() throws IOException, ClassNotFoundException {
        try (
                FileInputStream archivo = new FileInputStream(rutaArchivo); ObjectInputStream entrada = new ObjectInputStream(archivo)) {
            return (Cine) entrada.readObject();
        }
    }
}
