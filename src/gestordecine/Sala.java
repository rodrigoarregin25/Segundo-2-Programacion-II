package gestordecine;

import java.io.Serializable;
import java.util.ArrayList;


/**
 * Sala del cine. Guarda sus datos y la lista de butacas que la forman.
 * Implementa Desactivable, así que se puede dar de baja sin borrarla.
 */

public final class Sala implements Serializable, Desactivable {

    private static final long serialVersionUID = -6729183293122678294L;
    private final int idSala;
    private String nombreSala;
    private final int cantidadFilas;
    private final int butacasPorFila;
    private String distribucionButacas;
    private final ArrayList<Butaca> butacas;
    private boolean activa = true;
    
    
    /**
     * Crea una sala con la misma cantidad de butacas en todas las filas. Las
     * filas se nombran con letras (A, B, C...) y las butacas con números.
     *
     * @param idSala Identificador de la sala, mayor a cero.
     * @param nombreSala Nombre de la sala, que no puede estar vacío.
     * @param cantidadFilas Cantidad de filas, entre 1 y 26.
     * @param butacasPorFila Butacas de cada fila, mayor a cero.
     * @throws IllegalArgumentException Si el id no es mayor a cero.
     * @throws IllegalArgumentException Si el nombre está vacío.
     * @throws IllegalArgumentException Si las filas no están entre 1 y 26.
     * @throws IllegalArgumentException Si las butacas por fila no son mayores a
     * cero.
     */

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

        this.distribucionButacas = String.valueOf(butacasPorFila);

        for (int i = 0; i < cantidadFilas; i++) {
            char fila = (char) ('A' + i);

            for (int k = 1; k <= butacasPorFila; k++) {
                Butaca nuevaButaca = new Butaca(fila, k);
                butacas.add(nuevaButaca);
            }
        }

    }
    
     /**
     * Crea una sala indicando cómo se reparten las butacas en bloques, por
     * ejemplo "4-8-4" (cuatro, ocho y cuatro butacas separadas por pasillos).
     * La cantidad de butacas por fila es la suma de los bloques.
     *
     * @param idSala Identificador de la sala, mayor a cero.
     * @param nombreSala Nombre de la sala, que no puede estar vacío.
     * @param cantidadFilas Cantidad de filas, entre 1 y 26.
     * @param distribucionButacas Bloques de butacas separados por guiones,
     * por ejemplo "4-8-4".
     * @throws IllegalArgumentException Si la distribución no tiene el formato
     * correcto, si algún bloque no tiene butacas o si el total es demasiado
     * grande.
     * @throws IllegalArgumentException Si los demás datos no son válidos (ver
     * el otro constructor).
     */

    public Sala(int idSala, String nombreSala,
            int cantidadFilas, String distribucionButacas) {

        this(idSala, nombreSala, cantidadFilas,
                calcularTotalButacas(distribucionButacas));

        this.distribucionButacas = distribucionButacas.trim()
                .replaceAll("\\s+", "");
    }
    
    
    /**
     * Suma las butacas de todos los bloques de una distribución.
     *
     * @param distribucion Bloques de butacas separados por guiones, por
     * ejemplo "4-8-4".
     * @return El total de butacas de una fila.
     * @throws IllegalArgumentException Si la distribución es nula o no tiene el
     * formato de números separados por guiones.
     * @throws IllegalArgumentException Si algún bloque no tiene al menos una
     * butaca.
     * @throws IllegalArgumentException Si el total es demasiado grande.
     */

    private static int calcularTotalButacas(String distribucion) {

        if (distribucion == null
                || !distribucion.trim().matches("\\d+(\\s*-\\s*\\d+)*")) {

            throw new IllegalArgumentException(
                    "La distribución debe tener el formato 4-8-4."
            );
        }

        int total = 0;

        for (String bloque : distribucion.split("-")) {

            int cantidad;

            try {
                cantidad = Integer.parseInt(bloque.trim());
                total = Math.addExact(total, cantidad);
            } catch (ArithmeticException e) {
                throw new IllegalArgumentException(
                        "La cantidad de butacas es demasiado grande."
                );
            }

            if (cantidad <= 0) {
                throw new IllegalArgumentException(
                        "Cada bloque debe tener al menos una butaca."
                );
            }
        }

        return total;
    }
    
    /**
     * Cambia el nombre de la sala. Se le quitan los espacios de los extremos.
     *
     * @param nombreSala Nuevo nombre de la sala.
     * @throws IllegalArgumentException Si el nombre es nulo o está vacío.
     */

    public void setNombreSala(String nombreSala) {
        if (nombreSala == null || nombreSala.isBlank()) {
            throw new IllegalArgumentException("El nombre de la sala no puede estar vacio");
        }

        this.nombreSala = nombreSala.trim();
    }
    
    /**
     * Devuelve cómo se reparten las butacas de cada fila. En salas guardadas
     * antes de que existiera la distribución, devuelve la cantidad de butacas
     * por fila.
     *
     * @return La distribución, por ejemplo "4-8-4".
     */

    public String getDistribucionButacas() {

        if (distribucionButacas == null) {
            return String.valueOf(butacasPorFila);
        }

        return distribucionButacas;
    }
    
    /**
     * Devuelve el identificador de la sala.
     *
     * @return El id de la sala.
     */


    public int getIdSala() {
        return idSala;
    }

    /**
     * Devuelve el nombre de la sala.
     *
     * @return El nombre de la sala.
     */

    public String getNombreSala() {
        return nombreSala;
    }
    /**
     * Devuelve la cantidad de filas de la sala.
     *
     * @return La cantidad de filas.
     */
    public int getCantidadFilas() {
        return cantidadFilas;
    }
    
    /**
     * Devuelve la cantidad de butacas de cada fila.
     *
     * @return Las butacas por fila.
     */

    public int getButacasPorFila() {
        return butacasPorFila;
    }
    /**
     * Devuelve las butacas de la sala. Entrega una copia de la lista, para que
     * quien la use no pueda modificar las butacas de la sala.
     *
     * @return Una copia de la lista de butacas.
     */

    public ArrayList<Butaca> getButacas() {
        return new ArrayList<>(butacas);
    }

    /**
     * Indica si la sala está activa.
     *
     * @return true si la sala está activa, false si fue dada de baja.
     */
    public boolean isActiva() {
        return activa;
    }
    
    /**
     * Da de baja la sala sin borrarla, para no perder las funciones y
     * reservas que ya la usaron.
     */

    public void desactivar() {
        this.activa = false;
    }
    
    /**
     * Vuelve a dar de alta una sala que había sido desactivada.
     */

    public void reactivar() {
        this.activa = true;
    }

}