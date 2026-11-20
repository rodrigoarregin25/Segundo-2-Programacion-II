package gestordecine;
import java.io.Serializable;

public class Butaca implements Serializable {

    private final char filaButaca;
    private final int numeroButaca;

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

    public char getFilaButaca() {
        return filaButaca;
    }

    public int getNumeroButaca() {
        return numeroButaca;
    }
    
    public String getIdentificador(){
         return "" + filaButaca + numeroButaca;
    }
    
    /**
     * Que hace: compara dos butacas por fila y numero
     * Parametros: otroObjeto (Object)
     * Retorna: true si es la misma fila y el mismo numero, false si no
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
     * Que hace: calcula el hash de la butaca a partir de su identificador (fila + numero)
     * Retorna: el hash del identificador, por ejemplo el de "A5" (int)
     */
    @Override
    public int hashCode() {
        return getIdentificador().hashCode();
    }

}
