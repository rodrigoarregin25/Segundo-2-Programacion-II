package gestordecine;
import java.io.Serializable;

public class Butaca implements Serializable {

    private final char filaButaca;
    private final int numeroButaca;
    
   /**
    * Crea una butaca a partir de su fila y su numero. La fila se guarda siempre en mayúscula.
    * @param filaButaca letra de la fila, entre A Y Z
    * @param numeroButaca  Numero de la butaca dentro de la fila, mayor a cero.
    * @throws IllegalArgumentException Si la fila no es una netra entre A y Z o si el
    * numero no es mayor a cero.
    * 
    */

    public Butaca(char filaButaca, int numeroButaca) {
        filaButaca = Character.toUpperCase(filaButaca);

        if (filaButaca < 'A' || filaButaca > 'Z') {
            throw new IllegalArgumentException("La fila de la butaca debe ser una letra entre A y Z");
        }

        this.filaButaca = filaButaca;

        if (numeroButaca <= 0) {
            throw new IllegalArgumentException("El numero de butaca debe ser mayor a cero");
        }
        this.numeroButaca = numeroButaca;
    }
    
    /**
     * Devuelve la fila de la butaca
     * @return  la letra de la fila, en mayuscula. 
     */

    public char getFilaButaca() {
        return filaButaca;
    }
    
    /**
     * Devuelve el numero de la butaca dentro de su fila
     * @return numero de butaca
     */

    public int getNumeroButaca() {
        return numeroButaca;
    }
    
    /**
     * Arma el identificador de la butaca uniendo la fila y el numero
     * 
     * @return  el identificador como texto, por ejemplo "C7"
     */
    
    public String getIdentificador(){
         return "" + filaButaca + numeroButaca;
    }
    
    /**
     * Compara dos butacas por fila y numero
     * @param otroObjeto objeto con el que se compara la butaca.
     * @return True si es la misma fila y el mismo numero, false si no. 
     */
    @Override
    public boolean equals(Object otroObjeto) {
        if (this == otroObjeto) {
            return true;
        }
        if (otroObjeto == null || getClass() != otroObjeto.getClass()) {
            return false;
        }
        Butaca otraButaca = (Butaca) otroObjeto;
        return filaButaca == otraButaca.filaButaca && numeroButaca == otraButaca.numeroButaca;
    }
    
     /**
     * Calcula el hash de la butaca a partir de su identificador (fila + numero)
     * @return  el hash del identificador, por ejemplo el de "A5"
     */
    @Override
    public int hashCode() {
        return getIdentificador().hashCode();
    }

}
