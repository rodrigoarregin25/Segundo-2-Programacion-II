
package gestordecine.repositorios;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Repositorio genérico que permite realizar operaciones
 * comunes sobre colecciones de objetos.
 *
 * Trabaja sobre una lista existente, evitando duplicar
 * el almacenamiento de los elementos.
 *
 * @param <T> Tipo de elemento administrado por el repositorio.
 */
public class Repositorio<T> {

    private final List<T> elementos;

    /**
     * Inicializa el repositorio utilizando una lista existente.
     *
     * @param elementos Lista sobre la que se realizarán las operaciones.
     */
    public Repositorio(List<T> elementos) {

        if (elementos == null) {
            throw new IllegalArgumentException("La lista no puede ser nula");
        }

        this.elementos = elementos;
    }

    /**
     * Agrega un elemento a la colección.
     *
     * @param elemento Elemento que se desea agregar.
     * 
     * @throws IllegalArgumentException Si el elemento es nulo.
     */
    public void agregar(T elemento) {

        if (elemento == null) {
            throw new IllegalArgumentException("No se puede agregar un elemento nulo");
        }

        elementos.add(elemento);
    }

    /**
     * Busca el primer elemento que cumple una condición.
     *
     * @param condicion Condición utilizada para realizar la búsqueda.
     * 
     * @return Elemento encontrado o null si no existe.
     */
    public T buscar(Predicate<T> condicion) {

        if (condicion == null) {
            throw new IllegalArgumentException("La condición no puede ser nula");
        }

        for (T elemento : elementos) {
            if (condicion.test(elemento)) {
                return elemento;
            }
        }

        return null;
    }

    /**
     * Obtiene una copia de los elementos almacenados.
     *
     * @return Lista con los elementos del repositorio.
     */
    public List<T> listar() {
        return new ArrayList<>(elementos);
    }

    /**
     * Obtiene la cantidad de elementos almacenados.
     *
     * @return Cantidad de elementos del repositorio.
     */
    public int cantidad() {
        return elementos.size();
    }
}
