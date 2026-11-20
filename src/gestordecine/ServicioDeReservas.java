package gestordecine;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Gestiona todas las reservas y clientes del cine: los registra, los busca
 * y aplica las reglas que involucran a mas de una reserva.
 */
public class ServicioDeReservas implements Serializable {

    private ArrayList<Reserva> reservas;
    private ArrayList<Cliente> clientes;

    /**
     * Que hace: crea el servicio con las listas de reservas y clientes vacias
     */
    public ServicioDeReservas() {
        this.reservas = new ArrayList<>();
        this.clientes = new ArrayList<>();
    }

    /**
     * Que hace: devuelve una copia de la lista de reservas
     * Retorna: las reservas (ArrayList de Reserva)
     */
    public ArrayList<Reserva> getReservas() {
        return new ArrayList<>(reservas);
    }

    /**
     * Que hace: devuelve una copia de la lista de clientes
     * Retorna: los clientes (ArrayList de Cliente)
     */
    public ArrayList<Cliente> getClientes() {
        return new ArrayList<>(clientes);
    }

    /**
     * Que hace: dice si una butaca ya esta ocupada en una funcion por una reserva confirmada
     * Parametros: funcion (Funcion), butaca (Butaca)
     * Retorna: true si esta ocupada, false si esta libre
     */
    public boolean estaOcupada(Funcion funcion, Butaca butaca) {
        for (Reserva reserva : reservas) {
            // solo cuentan las reservas confirmadas de esa funcion
            if (reserva.isConfirmada()
                    && reserva.getFuncion().getIdFuncion() == funcion.getIdFuncion()) {
                for (Entrada entrada : reserva.getEntradas()) {
                    if (entrada.getButaca().equals(butaca)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Que hace: registra un cliente nuevo
     * Parametros: cliente (Cliente)
     * Lanza: IllegalArgumentException si el cliente es nulo o ya existe uno con ese DNI
     */
    public void registrarCliente(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo");
        }
        if (buscarClientePorDni(cliente.getDni()) != null) {
            throw new IllegalArgumentException("Ya existe un cliente con ese DNI");
        }
        clientes.add(cliente);
    }

    /**
     * Que hace: busca un cliente por su DNI
     * Parametros: dni (int)
     * Retorna: el cliente, o null si no existe ninguno con ese DNI
     */
    public Cliente buscarClientePorDni(int dni) {
        for (Cliente cliente : clientes) {
            if (cliente.getDni() == dni) {
                return cliente;
            }
        }
        return null;
    }

    /**
     * Que hace: valida y confirma una reserva con sus entradas, y la guarda
     * Parametros: cliente (Cliente), funcion (Funcion), entradas (ArrayList de Entrada)
     * Retorna: la reserva creada (Reserva)
     * Lanza: IllegalArgumentException si el cliente no esta registrado, si no hay entradas,
     *        si alguna butaca no es de la sala de la funcion o si ya esta ocupada
     */
    public Reserva confirmarReserva(Cliente cliente, Funcion funcion, ArrayList<Entrada> entradas) {

        // validaciones de los datos
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo");
        }
        if (funcion == null) {
            throw new IllegalArgumentException("La funcion no puede ser nula");
        }
        if (buscarClientePorDni(cliente.getDni()) == null) {
            throw new IllegalArgumentException("El cliente no esta registrado");
        }
        if (entradas == null || entradas.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos una butaca");
        }

        // cada butaca tiene que ser de la sala de la funcion y estar libre
        for (Entrada entrada : entradas) {
            if (entrada == null) {
                throw new IllegalArgumentException("La entrada no puede ser nula");
            }
            Butaca butaca = entrada.getButaca();
            if (!funcion.getSala().getButacas().contains(butaca)) {
                throw new IllegalArgumentException("La butaca " + butaca.getIdentificador()
                        + " no pertenece a la sala de la funcion");
            }
            if (estaOcupada(funcion, butaca)) {
                throw new IllegalArgumentException("La butaca " + butaca.getIdentificador()
                        + " ya esta ocupada en esta funcion");
            }
        }

        // si todo esta bien, creo la reserva, le cargo las entradas y la guardo
        Reserva reserva = new Reserva(cliente, funcion);
        for (Entrada entrada : entradas) {
            reserva.agregarEntrada(entrada);
        }
        reservas.add(reserva);
        return reserva;
    }

    /**
     * Que hace: busca una reserva por su codigo
     * Parametros: codigo (String)
     * Retorna: la reserva, o null si no existe ninguna con ese codigo
     */
    public Reserva buscarPorCodigo(String codigo) {
        for (Reserva reserva : reservas) {
            if (reserva.getCodigoReserva().equals(codigo)) {
                return reserva;
            }
        }
        return null;
    }

    /**
     * Que hace: busca todas las reservas de un cliente por su DNI
     * Parametros: dni (int)
     * Retorna: las reservas del cliente (ArrayList de Reserva), vacia si no tiene
     */
    public ArrayList<Reserva> buscarPorDni(int dni) {
        ArrayList<Reserva> encontradas = new ArrayList<>();
        for (Reserva reserva : reservas) {
            if (reserva.getCliente().getDni() == dni) {
                encontradas.add(reserva);
            }
        }
        return encontradas;
    }

    /**
     * Que hace: cancela la reserva con ese codigo, lo que libera sus butacas
     * Parametros: codigo (String)
     * Lanza: IllegalArgumentException si no existe la reserva o ya estaba cancelada
     */
    public void cancelarReserva(String codigo) {
        Reserva reserva = buscarPorCodigo(codigo);
        if (reserva == null) {
            throw new IllegalArgumentException("No existe una reserva con ese codigo");
        }
        reserva.cancelar();
    }
    /**
     * Que hace: arma el comprobante de una reserva con todos sus datos
     * Parametros: reserva (Reserva)
     * Retorna: el texto del comprobante (String)
     * Lanza: IllegalArgumentException si la reserva es nula
     */
    public String generarComprobante(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva no puede ser nula");
        }

        Funcion funcion = reserva.getFuncion();
        Cliente cliente = reserva.getCliente();

        String estado = "Confirmada";
        if (!reserva.isConfirmada()) {
            estado = "Cancelada";
        }

        String texto = "COMPROBANTE DE RESERVA\n";
        texto = texto + "Codigo: " + reserva.getCodigoReserva() + "\n";
        texto = texto + "Estado: " + estado + "\n";
        texto = texto + "Cliente: " + cliente.getNombre() + " " + cliente.getApellido()
                + " (DNI " + cliente.getDni() + ")\n";
        texto = texto + "Pelicula: " + funcion.getPelicula().getTituloPelicula() + "\n";
        texto = texto + "Sala: " + funcion.getSala().getNombreSala() + "\n";
        texto = texto + "Funcion: " + funcion.getFechaHora() + "\n";
        texto = texto + "Butacas:\n";
        for (Entrada entrada : reserva.getEntradas()) {
            texto = texto + "  " + entrada.getButaca().getIdentificador()
                    + " - " + entrada.getTipo()
                    + " - $" + entrada.calcularPrecioFinal() + "\n";
        }
        texto = texto + "Importe total: $" + reserva.getImporteTotal();
        return texto;
    }
    
    /**
     * Que hace: vuelve a cargar los codigos de reserva ya usados, a partir de las
     *           reservas del servicio. Se llama despues de cargar los datos guardados
     */
    public void recargarCodigos() {
        Reserva.recargarCodigos(reservas);
    }

}