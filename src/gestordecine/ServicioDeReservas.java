package gestordecine;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Gestiona todas las reservas y clientes del cine: los registra, los busca y
 * aplica las reglas que involucran a mas de una reserva.
 */
public class ServicioDeReservas implements Serializable {

    // Numero de version para poder leer los archivos .dat guardados antes
    private static final long serialVersionUID = -7137724024555080719L;

    // Cuenta del administrador inicial
    public static final int DNI_ADMINISTRADOR = 1;
    public static final String CONTRASENIA_ADMINISTRADOR = "admin1234";

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
     * Que hace: devuelve una copia de la lista de reservas Retorna: las
     * reservas (ArrayList de Reserva)
     */
    public ArrayList<Reserva> getReservas() {
        return new ArrayList<>(reservas);
    }

    /**
     * Que hace: devuelve una copia de la lista de clientes Retorna: los
     * clientes (ArrayList de Cliente)
     */
    public ArrayList<Cliente> getClientes() {
        return new ArrayList<>(clientes);
    }

    /**
     * Que hace: dice si una butaca ya esta ocupada en una funcion por una
     * reserva confirmada Parametros: funcion (Funcion), butaca (Butaca)
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
     * Que hace: registra un cliente nuevo Parametros: cliente (Cliente) Lanza:
     * IllegalArgumentException si el cliente es nulo o ya existe uno con ese
     * DNI
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
     * Que hace: busca un cliente por su DNI Parametros: dni (int) Retorna: el
     * cliente, o null si no existe ninguno con ese DNI
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
     * Que hace: se asegura de que exista al menos un administrador. Si no hay
     * ninguno, crea la cuenta inicial (DNI y contrasenia de las constantes). Si
     * ese DNI ya lo usa otro cliente, no toca nada. Retorna: true si creo el
     * administrador (hay que guardar los datos), false si no hizo falta
     */
    public boolean asegurarAdministrador() {
        for (Cliente cliente : clientes) {
            if (cliente.esAdministrador()) {
                return false;
            }
        }

        if (buscarClientePorDni(DNI_ADMINISTRADOR) != null) {
            return false;
        }

        clientes.add(new Cliente(DNI_ADMINISTRADOR, "Administrador", "Cinema",
                "admin@cinema.com", CONTRASENIA_ADMINISTRADOR, Rol.ADMINISTRADOR));
        return true;
    }

    /**
     * Que hace: devuelve las reservas que puede consultar un usuario. El
     * administrador ve todas; el cliente solo las propias. Parametros:
     * solicitante (Cliente que consulta) Retorna: las reservas visibles
     * (ArrayList de Reserva) Lanza: IllegalArgumentException si el solicitante
     * es nulo
     */
    public ArrayList<Reserva> buscarReservasVisibles(Cliente solicitante) {
        if (solicitante == null) {
            throw new IllegalArgumentException("Debe iniciar sesion para ver reservas");
        }

        if (solicitante.esAdministrador()) {
            return getReservas();
        }

        return buscarPorDni(solicitante.getDni());
    }

    /**
     * Que hace: cancela una reserva comprobando los permisos. El administrador
     * puede cancelar cualquiera; el cliente solo las suyas. Parametros: codigo
     * (String), solicitante (Cliente que pide cancelar) Lanza:
     * IllegalArgumentException si no hay sesion o la reserva no existe,
     * IllegalStateException si el cliente intenta cancelar una reserva ajena
     */
    public void cancelarReserva(String codigo, Cliente solicitante) {
        if (solicitante == null) {
            throw new IllegalArgumentException("Debe iniciar sesion para cancelar una reserva");
        }

        Reserva reserva = buscarPorCodigo(codigo);

        if (reserva != null
                && !solicitante.esAdministrador()
                && reserva.getCliente().getDni() != solicitante.getDni()) {
            throw new IllegalStateException(
                    "No tiene permisos para cancelar una reserva de otro cliente");
        }

        // el resto de las reglas (existe, ya cancelada, funcion comenzada) las controla la otra version
        cancelarReserva(codigo);
    }

    /**
     * Que hace: valida y confirma una reserva. Comprueba que el cliente este
     * registrado, que la funcion y la sala esten activas y que la funcion no
     * haya comenzado. Tambien que las butacas sean de la sala, no esten
     * repetidas ni ocupadas, y que el precio coincida con el de la funcion.
     * Parametros: cliente (Cliente), funcion (Funcion), entradas (ArrayList de
     * Entrada) Retorna: la reserva creada y confirmada (Reserva) Lanza:
     * IllegalArgumentException si algun dato o entrada es invalido,
     * IllegalStateException si la funcion o la sala no estan disponibles
     */
    public Reserva confirmarReserva(Cliente cliente, Funcion funcion, ArrayList<Entrada> entradas) {

        // validaciones de los datos
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo");
        }
        if (funcion == null) {
            throw new IllegalArgumentException("La funcion no puede ser nula");
        }
        // No permitir reservas para funciones desactivadas.
        if (!funcion.isActiva()) {
            throw new IllegalStateException(
                    "No se pueden realizar reservas para una función desactivada"
            );
        }

        // No permitir reservas de funciones que ya comenzaron.
        if (!funcion.getFechaHora().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException(
                    "No se pueden realizar reservas para una función que ya comenzó"
            );
        }

        // Verificar que la sala esté activa.
        if (!funcion.getSala().isActiva()) {
            throw new IllegalStateException(
                    "No se pueden realizar reservas en una sala desactivada"
            );
        }
        if (buscarClientePorDni(cliente.getDni()) == null) {
            throw new IllegalArgumentException("El cliente no esta registrado");
        }
        if (entradas == null || entradas.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos una butaca");
        }

        // cada butaca tiene que ser de la sala de la funcion y estar libre
        ArrayList<Butaca> butacasSeleccionadas = new ArrayList<>();
        for (Entrada entrada : entradas) {
            if (entrada == null) {
                throw new IllegalArgumentException("La entrada no puede ser nula");
            }
            Butaca butaca = entrada.getButaca();

            if (butaca == null) {
                throw new IllegalArgumentException(
                        "La entrada debe tener una butaca asignada"
                );
            }

            if (Double.compare(entrada.getPrecioBase(), funcion.getPrecio()) != 0) {
                throw new IllegalArgumentException(
                        "El precio base de la entrada no coincide con el precio de la funcion");
            }

            if (butacasSeleccionadas.contains(butaca)) {
                throw new IllegalArgumentException(
                        "La butaca " + butaca.getIdentificador()
                        + " esta repetida en la solicitud");
            }

            if (!funcion.getSala().getButacas().contains(butaca)) {
                throw new IllegalArgumentException("La butaca " + butaca.getIdentificador()
                        + " no pertenece a la sala de la funcion");
            }
            if (estaOcupada(funcion, butaca)) {
                throw new IllegalArgumentException("La butaca " + butaca.getIdentificador()
                        + " ya esta ocupada en esta funcion");
            }

            butacasSeleccionadas.add(butaca);
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
     * Que hace: busca una reserva por su codigo Parametros: codigo (String)
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
     * Que hace: busca todas las reservas de un cliente por su DNI Parametros:
     * dni (int) Retorna: las reservas del cliente (ArrayList de Reserva), vacia
     * si no tiene
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
     * Que hace: cancela la reserva con ese codigo, lo que libera sus butacas.
     * No se puede cancelar si la funcion ya comenzo. Parametros: codigo
     * (String) Lanza: IllegalArgumentException si no existe la reserva, si ya
     * estaba cancelada o si la funcion ya comenzo
     */
    public void cancelarReserva(String codigo) {
        Reserva reserva = buscarPorCodigo(codigo);
        if (reserva == null) {
            throw new IllegalArgumentException("No existe una reserva con ese codigo");
        }
        // si ya estaba cancelada, el mensaje lo da la propia reserva
        if (reserva.isConfirmada()
                && !reserva.getFuncion().getFechaHora().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "No se puede cancelar: la funcion ya comenzo");
        }
        reserva.cancelar();
    }

    /**
     * Que hace: arma el comprobante de una reserva con todos sus datos
     * Parametros: reserva (Reserva) Retorna: el texto del comprobante (String)
     * Lanza: IllegalArgumentException si la reserva es nula
     */
    public String generarComprobante(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva no puede ser nula");
        }

        Funcion funcion = reserva.getFuncion();
        Cliente cliente = reserva.getCliente();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

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
        texto = texto + "Funcion: " + funcion.getFechaHora().format(formato) + "\n";
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
     * Que hace: vuelve a cargar los codigos de reserva ya usados, a partir de
     * las reservas del servicio. Se llama despues de cargar los datos guardados
     */
    public void recargarCodigos() {
        Reserva.recargarCodigos(reservas);
    }

    /**
     * Que hace: dice si una funcion tiene alguna reserva, confirmada o
     * cancelada Parametros: funcion (Funcion) Retorna: true si tiene al menos
     * una reserva, false si no tiene ninguna
     */
    public boolean tieneReservas(Funcion funcion) {
        for (Reserva reserva : reservas) {
            if (reserva.getFuncion().getIdFuncion() == funcion.getIdFuncion()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calcula la cantidad de butacas disponibles para una función.
     *
     * Considera ocupadas únicamente las butacas pertenecientes a reservas
     * confirmadas. Las reservas canceladas no ocupan lugar.
     *
     * @param funcion Función cuya disponibilidad se desea consultar.
     * @return Cantidad de butacas libres.
     * @throws IllegalArgumentException Si la función es nula.
     */
    public int obtenerButacasDisponibles(Funcion funcion) {

        if (funcion == null) {
            throw new IllegalArgumentException("La función no puede ser nula");
        }

        int disponibles = 0;

        for (Butaca butaca : funcion.getSala().getButacas()) {

            if (!estaOcupada(funcion, butaca)) {
                disponibles++;
            }
        }

        return disponibles;
    }

    /**
     * Cambia el rol de un usuario registrado.
     *
     * Solo permite realizar la operación a un administrador. Impide que un
     * administrador modifique su propio rol y evita que el sistema se quede sin
     * administradores.
     *
     * @param dniUsuario DNI del usuario que se desea modificar.
     * @param nuevoRol Rol que se asignará al usuario.
     * @param solicitante Administrador que realiza la modificación.
     * @throws IllegalArgumentException Si el usuario no existe o el rol es
     * nulo.
     * @throws IllegalStateException Si el solicitante no tiene permisos o
     * intenta modificar su propio rol.
     */
    public void cambiarRolUsuario(int dniUsuario, Rol nuevoRol, Cliente solicitante) {

        // Validar permisos.
        if (solicitante == null || !solicitante.esAdministrador()) {
            throw new IllegalStateException(
                    "No tiene permisos para modificar roles"
            );
        }

        // Validar el nuevo rol.
        if (nuevoRol == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un rol válido"
            );
        }

        // Buscar al usuario.
        Cliente usuario = buscarClientePorDni(dniUsuario);

        if (usuario == null) {
            throw new IllegalArgumentException(
                    "El usuario seleccionado no existe"
            );
        }

        // Impedir que el administrador cambie su propio rol.
        if (usuario.getDni() == solicitante.getDni()) {
            throw new IllegalStateException(
                    "No puede modificar su propio rol"
            );
        }

        // Verificar que exista otro administrador si se quitan permisos.
        if (usuario.esAdministrador() && nuevoRol == Rol.CLIENTE) {

            int cantidadAdministradores = 0;

            for (Cliente cliente : clientes) {
                if (cliente.esAdministrador()) {
                    cantidadAdministradores++;
                }
            }

            if (cantidadAdministradores <= 1) {
                throw new IllegalStateException(
                        "No se puede quitar el rol al último administrador"
                );
            }
        }

        usuario.setRol(nuevoRol);
    }
}
