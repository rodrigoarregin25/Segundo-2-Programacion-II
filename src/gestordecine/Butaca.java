package gestordecine;

public class Butaca {

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

}
