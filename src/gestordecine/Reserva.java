package gestordecine;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;



/**
 * Reserva de un cliente para una funcion. Tiene un codigo unico (R + numero)
 * y puede estar confirmada o cancelada.
 */
public class Reserva implements Serializable {

    
    private static final int CANTIDAD_DIGITOS = 5;
    private static HashSet<String> codigosUsados = new HashSet<>();
    private String codigoReserva;
    private boolean confirmada;
    private Cliente cliente;
    private Funcion funcion;
    private double importeTotal;
    
   /**
 * Reserva de un cliente para una funcion. Tiene un codigo unico (R + digitos al azar)
 * y puede estar confirmada o cancelada.
 */
    
    public Reserva(Cliente cliente, Funcion funcion) {

    if (cliente == null) {
        throw new IllegalArgumentException("El cliente no puede ser nulo");
    }
    if (funcion == null) {
        throw new IllegalArgumentException("La funcion no puede estar vacia");
    }
    if (!funcion.getFechaHora().isAfter(LocalDateTime.now())){
        throw new IllegalArgumentException("La funcion ya comenzo");
    } 
    
    
    String codigoNuevo; // genero el codigo digito por digito, y repito si ya existe
    do{
        codigoNuevo = "R";
        for (int i= 0; i < CANTIDAD_DIGITOS; i++){
            codigoNuevo = codigoNuevo + ThreadLocalRandom.current().nextInt(10);
        }
    } while (codigosUsados.contains(codigoNuevo));
    codigosUsados.add(codigoNuevo);
    // guardo los datos de la reserva (siempre arranca confirmada)
    this.cliente = cliente;
    this.funcion = funcion;
    this.confirmada = true; 
    this.codigoReserva = codigoNuevo;
    
}

    /**
     * Que hace: dice si la reserva esta confirmada
     * Retorna: true si esta confirmada, false si esta cancelada
     */
    public boolean isConfirmada() {
        return confirmada;
    }
    /**
     * Que hace: cancela la reserva (queda en el historial, no se borra)
     * Lanza: IllegalArgumentException si la reserva ya estaba cancelada
     */
    public void cancelar() {
        if (!confirmada) {
            throw new IllegalArgumentException("Ya se encuentra cancelada la reserva");
        }
        this.confirmada = false;
    }
/**
 * Que hace: devuelve el codigo de la reserva
 * Retorna: el codigo (String)
 */
public String getCodigoReserva() {
    return codigoReserva;
}

/**
 * Que hace: devuelve el cliente de la reserva
 * Retorna: el cliente (Cliente)
 */
public Cliente getCliente() {
    return cliente;
}

/**
 * Que hace: devuelve la funcion de la reserva
 * Retorna: la funcion (Funcion)
 */
public Funcion getFuncion() {
    return funcion;
}

/**
 * Que hace: devuelve el importe total de la reserva
 * Retorna: el importe total (double)
 */
public double getImporteTotal() {
    return importeTotal;
}
}