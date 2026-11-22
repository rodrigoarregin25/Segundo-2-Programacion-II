package gestordecine;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;

/**
 * Reserva de un cliente para una funcion. Tiene un codigo unico (R + digitos al
 * azar), una lista de entradas (una por butaca) y puede estar confirmada o
 * cancelada.
 */
public class Reserva implements Serializable {

    /**
     * Identificador de versión utilizado para mantener la compatibilidad con
     * las reservas almacenadas en cine_app.dat.
     */
    private static final long serialVersionUID = -8933165786525315024L;

    private static final int CANTIDAD_DIGITOS = 5;

    private static HashSet<String> codigosUsados = new HashSet<>();

    private String codigoReserva;
    private boolean confirmada;
    private Cliente cliente;
    private Funcion funcion;
    private double importeTotal;
    private ArrayList<Entrada> entradas;

    /**
     * Que hace: crea una reserva confirmada para un cliente y una funcion
     * Parametros: cliente (Cliente), funcion (Funcion) Lanza:
     * IllegalArgumentException si el cliente o la funcion son nulos, o si la
     * funcion ya comenzo
     */
    public Reserva(Cliente cliente, Funcion funcion) {

        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo");
        }
        if (funcion == null) {
            throw new IllegalArgumentException("La funcion no puede estar vacia");
        }
        if (!funcion.getFechaHora().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("La funcion ya comenzo");
        }

        String codigoNuevo; // genero el codigo digito por digito, y repito si ya existe
        do {
            codigoNuevo = "R";
            for (int i = 0; i < CANTIDAD_DIGITOS; i++) {
                codigoNuevo = codigoNuevo + ThreadLocalRandom.current().nextInt(10);
            }
        } while (codigosUsados.contains(codigoNuevo));
        codigosUsados.add(codigoNuevo);

        // guardo los datos de la reserva (siempre arranca confirmada)
        this.cliente = cliente;
        this.funcion = funcion;
        this.confirmada = true;
        this.codigoReserva = codigoNuevo;
        this.entradas = new ArrayList<>();
    }

    /**
     * Que hace: dice si la reserva esta confirmada Retorna: true si esta
     * confirmada, false si esta cancelada
     */
    public boolean isConfirmada() {
        return confirmada;
    }

    /**
     * Que hace: cancela la reserva (queda en el historial, no se borra) Lanza:
     * IllegalArgumentException si la reserva ya estaba cancelada
     */
    public void cancelar() {
        if (!confirmada) {
            throw new IllegalArgumentException("Ya se encuentra cancelada la reserva");
        }
        this.confirmada = false;
    }

    /**
     * Que hace: agrega una entrada a la reserva y actualiza el importe total
     * Parametros: entrada (Entrada) Lanza: IllegalArgumentException si la
     * entrada es nula, si la reserva esta cancelada o si la butaca ya esta en
     * esta reserva
     */
    public void agregarEntrada(Entrada entrada) {

        if (entrada == null) {
            throw new IllegalArgumentException("La entrada no puede ser nula");
        }
        if (!confirmada) {
            throw new IllegalArgumentException("La reserva esta cancelada");

        }
        for (Entrada existente : entradas) {
            if (existente.getButaca().equals(entrada.getButaca())) {
                throw new IllegalArgumentException("La butaca ya esta en esta reserva");
            }
        }
        entradas.add(entrada); // la agrego y sumo su precio final al total
        importeTotal = importeTotal + entrada.calcularPrecioFinal();
    }

    /**
     * Que hace: devuelve el codigo de la reserva Retorna: el codigo (String)
     */
    public String getCodigoReserva() {
        return codigoReserva;
    }

    /**
     * Que hace: devuelve el cliente de la reserva Retorna: el cliente (Cliente)
     */
    public Cliente getCliente() {
        return cliente;
    }

    /**
     * Que hace: devuelve la funcion de la reserva Retorna: la funcion (Funcion)
     */
    public Funcion getFuncion() {
        return funcion;
    }

    /**
     * Que hace: devuelve el importe total de la reserva Retorna: el importe
     * total (double)
     */
    public double getImporteTotal() {
        return importeTotal;
    }

    /**
     * Que hace: devuelve una copia de la lista de entradas de la reserva
     * Retorna: las entradas (ArrayList de Entrada)
     */
    public ArrayList<Entrada> getEntradas() {
        return new ArrayList<>(entradas);
    }

    /**
     * Que hace: vuelve a cargar los codigos ya usados a partir de las reservas
     * guardadas, para que no se repitan al abrir el programa de nuevo
     * Parametros: reservasGuardadas (ArrayList de Reserva)
     */
    public static void recargarCodigos(ArrayList<Reserva> reservasGuardadas) {
        codigosUsados.clear();
        for (Reserva reserva : reservasGuardadas) {
            codigosUsados.add(reserva.getCodigoReserva());
        }
    }

}
