package gestordecine;

import java.io.Serializable;
import java.util.ArrayList;

public final class Sala implements Serializable {

    private final int idSala;
    private String nombreSala;
    private final int cantidadFilas;
    private final int butacasPorFila;
    private final ArrayList<Butaca> butacas;

    public Sala(int idSala, String nombreSala, int cantidadFilas, int butacasPorFila) {
        setNombreSala(nombreSala);

        if (idSala <= 0) {
            throw new IllegalArgumentException("El numero de sala debe ser mayor a cero");
        }
        this.idSala = idSala;

        if (cantidadFilas <= 0 || cantidadFilas > 26) {
            throw new IllegalArgumentException("La cantidad de filas debe estar entre 1 y 26");
        }
        this.cantidadFilas = cantidadFilas;

        if (butacasPorFila <= 0) {
            throw new IllegalArgumentException("Las butacas por fila deben ser mayor a cero");
        }
        this.butacasPorFila = butacasPorFila;
        this.butacas = new ArrayList<>();

        for (int i = 0; i < cantidadFilas; i++) {
            char fila = (char) ('A' + i);

            for (int k = 1; k <= butacasPorFila; k++) {
                Butaca nuevaButaca = new Butaca(fila, k);
                butacas.add(nuevaButaca);
            }
        }

    }

    public void setNombreSala(String nombreSala) {
        if (nombreSala == null || nombreSala.isBlank()) {
            throw new IllegalArgumentException("El nombre de la sala no puede estar vacio");
        }

        this.nombreSala = nombreSala.trim();
    }

    public int getIdSala() {
        return idSala;
    }

    public String getNombreSala() {
        return nombreSala;
    }

    public int getCantidadFilas() {
        return cantidadFilas;
    }

    public int getButacasPorFila() {
        return butacasPorFila;
    }

    public ArrayList<Butaca> getButacas() {
        return new ArrayList<>(butacas);
    }
    
}
