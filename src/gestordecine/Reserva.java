package gestordecine;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;

/**
 * Representa una reserva realizada por un cliente para una función del cine.
 *
 * Cada reserva posee un código único formado por la letra R y cinco dígitos
 * aleatorios, un cliente, una función y una lista de entradas asociadas a las
 * butacas seleccionadas.
 *
 * Permite agregar entradas, calcular el importe total y cancelar una reserva
 * sin eliminarla del sistema, conservando su información en el historial.
 *
 * Implementa Serializable para permitir su persistencia.
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
     * Crea una reserva confirmada para un cliente y una función futura.
     *
     * Genera un código de reserva que no se encuentre entre los códigos
     * utilizados e inicializa la lista de entradas vacía.
     *
     * @param cliente Cliente que realiza la reserva.
     * @param funcion Función para la cual se realiza la reserva.
     * @throws IllegalArgumentException Si el cliente o la función son nulos, o
     * si la función ya comenzó.
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

        String codigoNuevo;
        do {
            codigoNuevo = "R";
            for (int i = 0; i < CANTIDAD_DIGITOS; i++) {
                codigoNuevo = codigoNuevo + ThreadLocalRandom.current().nextInt(10);
            }
        } while (codigosUsados.contains(codigoNuevo));
        codigosUsados.add(codigoNuevo);

        this.cliente = cliente;
        this.funcion = funcion;
        this.confirmada = true;
        this.codigoReserva = codigoNuevo;
        this.entradas = new ArrayList<>();
    }

    /**
     * Indica si la reserva se encuentra confirmada.
     *
     * @return true si está confirmada, false si está cancelada.
     */
    public boolean isConfirmada() {
        return confirmada;
    }

    /**
     * Cancela la reserva sin eliminarla del sistema.
     *
     * La reserva permanece registrada para conservar el historial.
     *
     * @throws IllegalArgumentException Si la reserva ya se encuentra cancelada.
     */
    public void cancelar() {
        if (!confirmada) {
            throw new IllegalArgumentException("Ya se encuentra cancelada la reserva");
        }
        this.confirmada = false;
    }

    /**
     * Agrega una entrada a la reserva y actualiza el importe total.
     *
     * Verifica que la entrada no sea nula, que la reserva esté confirmada y que
     * la butaca no se encuentre repetida dentro de la misma reserva.
     *
     * El importe total se incrementa utilizando el precio final de la entrada,
     * incluyendo los descuentos que correspondan.
     *
     * @param entrada Entrada que se desea agregar a la reserva.
     * @throws IllegalArgumentException Si la entrada es nula, la reserva está
     * cancelada o la butaca ya se encuentra en la reserva.
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
        entradas.add(entrada);
        importeTotal = importeTotal + entrada.calcularPrecioFinal();
    }

    /**
     * @return Código único de la reserva.
     */
    public String getCodigoReserva() {
        return codigoReserva;
    }

    /**
     * @return Cliente asociado a la reserva.
     */
    public Cliente getCliente() {
        return cliente;
    }

    /**
     * @return Función correspondiente a la reserva.
     */
    public Funcion getFuncion() {
        return funcion;
    }

    /**
     * @return Importe total de las entradas agregadas a la reserva.
     */
    public double getImporteTotal() {
        return importeTotal;
    }

    /**
     * Obtiene las entradas asociadas a la reserva.
     *
     * @return Una nueva lista con las entradas registradas.
     */
    public ArrayList<Entrada> getEntradas() {
        return new ArrayList<>(entradas);
    }

    /**
     * Recarga los códigos de reserva utilizados a partir de las reservas
     * almacenadas previamente.
     *
     * Limpia el conjunto de códigos registrados en memoria y agrega los códigos
     * de las reservas recibidas para evitar que se repitan al generar nuevas
     * reservas.
     *
     * @param reservasGuardadas Lista de reservas cuyos códigos deben
     * recuperarse.
     */
    public static void recargarCodigos(ArrayList<Reserva> reservasGuardadas) {
        codigosUsados.clear();
        for (Reserva reserva : reservasGuardadas) {
            codigosUsados.add(reserva.getCodigoReserva());
        }
    }
}
