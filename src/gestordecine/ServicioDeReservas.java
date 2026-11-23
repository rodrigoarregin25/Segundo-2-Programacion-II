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
     * Devuelve una copia de la lista de reservas.
     *
     * @return Una copia de las reservas.
     */
    public ArrayList<Reserva> getReservas() {
        return new ArrayList<>(reservas);
    }

    /**
     * Devuelve una copia de la lista de clientes.
     *
     * @return Una copia de los clientes.
     */
    public ArrayList<Cliente> getClientes() {
        return new ArrayList<>(clientes);
    }


    /**
     * Dice si una butaca ya está ocupada en una función por una reserva
     * confirmada. Las reservas canceladas no cuentan.
     *
     * @param funcion Función que se consulta.
     * @param butaca Butaca que se quiere comprobar.
     * @return true si está ocupada, false si está libre.
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
     * Registra un cliente nuevo.
     *
     * @param cliente Cliente que se quiere registrar.
     * @throws IllegalArgumentException Si el cliente es nulo.
     * @throws IllegalArgumentException Si ya existe un cliente con ese DNI.
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
     * Busca un cliente por su DNI.
     *
     * @param dni DNI del cliente que se busca.
     * @return El cliente, o null si no existe ninguno con ese DNI.
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
     * Se asegura de que exista al menos un administrador. Si no hay ninguno,
     * crea la cuenta inicial (con el DNI y la contraseña de las constantes). Si
     * ese DNI ya lo usa otro cliente, no toca nada.
     *
     * @return true si creó el administrador (hay que guardar los datos), false
     * si no hizo falta.
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
     * Devuelve las reservas que puede consultar un usuario. El administrador
     * ve todas; el cliente solo las propias.
     *
     * @param solicitante Cliente que consulta las reservas.
     * @return Las reservas visibles para ese usuario.
     * @throws IllegalArgumentException Si el solicitante es nulo.
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
     * Cancela una reserva comprobando los permisos. El administrador puede
     * cancelar cualquiera; el cliente solo las suyas.
     *
     * @param codigo Código de la reserva que se quiere cancelar.
     * @param solicitante Cliente que pide la cancelación.
     * @throws IllegalArgumentException Si no hay sesión o la reserva no existe.
     * @throws IllegalStateException Si el cliente intenta cancelar una reserva
     * de otro cliente.
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
     * Valida y confirma una reserva. Comprueba que el cliente esté registrado,
     * que la función y la sala estén activas y que la función no haya
     * comenzado. También que las butacas sean de la sala, no estén repetidas
     * ni ocupadas, y que el precio coincida con el de la función.
     *
     * @param cliente Cliente que hace la reserva.
     * @param funcion Función que se reserva.
     * @param entradas Entradas con las butacas elegidas.
     * @return La reserva creada y confirmada.
     * @throws IllegalArgumentException Si algún dato o alguna entrada es
     * inválido.
     * @throws IllegalStateException Si la función o la sala no están
     * disponibles.
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
     * Busca una reserva por su código.
     *
     * @param codigo Código de la reserva que se busca.
     * @return La reserva, o null si no existe ninguna con ese código.
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
     * Busca todas las reservas de un cliente por su DNI.
     *
     * @param dni DNI del cliente.
     * @return Las reservas del cliente, o una lista vacía si no tiene.
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
     * Cancela la reserva con ese código, lo que libera sus butacas. No se puede
     * cancelar si la función ya comenzó.
     *
     * @param codigo Código de la reserva que se quiere cancelar.
     * @throws IllegalArgumentException Si no existe la reserva, si ya estaba
     * cancelada o si la función ya comenzó.
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
     * Arma el comprobante de una reserva con todos sus datos.
     *
     * @param reserva Reserva de la que se arma el comprobante.
     * @return El texto del comprobante.
     * @throws IllegalArgumentException Si la reserva es nula.
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
     * Vuelve a cargar los códigos de reserva ya usados, a partir de las
     * reservas del servicio. Se llama después de cargar los datos guardados.
     */
    
    
    public void recargarCodigos() {
        Reserva.recargarCodigos(reservas);
    }
    /**
     * Dice si una función tiene alguna reserva, confirmada o cancelada.
     *
     * @param funcion Función que se consulta.
     * @return true si tiene al menos una reserva, false si no tiene ninguna.
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

    /**
     * Modifica los datos de un usuario registrado.
     *
     * Solo los administradores pueden editar usuarios. El DNI permanece sin
     * cambios porque identifica al usuario. Si la nueva contraseña está vacía,
     * se conserva la actual.
     *
     * Impide que un administrador cambie su propio rol y que el sistema quede
     * sin administradores.
     *
     * @param dniUsuario DNI del usuario que se desea modificar.
     * @param nombre Nuevo nombre.
     * @param apellido Nuevo apellido.
     * @param email Nuevo correo electrónico.
     * @param nuevaContrasenia Nueva contraseña o texto vacío para conservarla.
     * @param nuevoRol Rol que se desea asignar.
     * @param solicitante Administrador que realiza la modificación.
     * @throws IllegalArgumentException Si los datos son inválidos.
     * @throws IllegalStateException Si el solicitante no tiene permisos.
     */
    public void editarUsuario(int dniUsuario, String nombre, String apellido,
            String email, String nuevaContrasenia, Rol nuevoRol,
            Cliente solicitante) {

        // VALIDAR PERMISOS
        if (solicitante == null || !solicitante.esAdministrador()) {
            throw new IllegalStateException(
                    "No tiene permisos para editar usuarios"
            );
        }

        // COMPROBAR QUE EL ADMINISTRADOR ESTÉ REGISTRADO
        Cliente administrador = buscarClientePorDni(solicitante.getDni());

        if (administrador == null || !administrador.esAdministrador()) {
            throw new IllegalStateException(
                    "El administrador no está registrado o no tiene permisos"
            );
        }

        // BUSCAR EL USUARIO
        Cliente usuario = buscarClientePorDni(dniUsuario);

        if (usuario == null) {
            throw new IllegalArgumentException(
                    "El usuario seleccionado no existe"
            );
        }

        if (nuevoRol == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un rol válido"
            );
        }

        // IMPEDIR QUE EL ADMINISTRADOR MODIFIQUE SU PROPIO ROL
        if (usuario.getDni() == administrador.getDni()
                && usuario.getRol() != nuevoRol) {

            throw new IllegalStateException(
                    "No puede modificar su propio rol"
            );
        }

        // IMPEDIR QUITAR EL ÚLTIMO ADMINISTRADOR
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

        // VALIDAR TODOS LOS DATOS ANTES DE MODIFICAR EL USUARIO
        // El constructor reutiliza las validaciones existentes.
        new Cliente(
                usuario.getDni(),
                nombre,
                apellido,
                email,
                nuevaContrasenia == null || nuevaContrasenia.isBlank()
                ? usuario.getContrasenia()
                : nuevaContrasenia,
                nuevoRol
        );

        // APLICAR LOS CAMBIOS
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setEmail(email);

        if (nuevaContrasenia != null && !nuevaContrasenia.isBlank()) {
            usuario.setContrasenia(nuevaContrasenia);
        }

        usuario.setRol(nuevoRol);
    }

    /**
     * Cambia el estado de un usuario registrado.
     *
     * Permite desactivar o reactivar usuarios sin eliminarlos. Solo un
     * administrador activo puede realizar esta operación. Impide desactivar la
     * propia cuenta o al último administrador activo del sistema.
     *
     * @param dniUsuario DNI del usuario que se desea modificar.
     * @param solicitante Administrador que realiza la operación.
     * @throws IllegalArgumentException Si el usuario no existe.
     * @throws IllegalStateException Si no tiene permisos o si la operación
     * dejaría al sistema sin administradores activos.
     */
    public void cambiarEstadoUsuario(int dniUsuario, Cliente solicitante) {

        // VALIDAR PERMISOS DEL ADMINISTRADOR
        if (solicitante == null) {
            throw new IllegalStateException(
                    "Debe iniciar sesión como administrador"
            );
        }

        Cliente administrador = buscarClientePorDni(solicitante.getDni());

        if (administrador == null
                || !administrador.esAdministrador()
                || !administrador.isActivo()) {

            throw new IllegalStateException(
                    "No tiene permisos para administrar usuarios"
            );
        }

        // BUSCAR EL USUARIO SELECCIONADO
        Cliente usuario = buscarClientePorDni(dniUsuario);

        if (usuario == null) {
            throw new IllegalArgumentException(
                    "El usuario seleccionado no existe"
            );
        }

        // DESACTIVAR USUARIO
        if (usuario.isActivo()) {

            // Impedir desactivar la propia cuenta.
            if (usuario.getDni() == administrador.getDni()) {
                throw new IllegalStateException(
                        "No puede desactivar su propia cuenta"
                );
            }

            // Impedir desactivar al último administrador activo.
            if (usuario.esAdministrador()) {

                int administradoresActivos = 0;

                for (Cliente cliente : clientes) {

                    if (cliente.esAdministrador() && cliente.isActivo()) {
                        administradoresActivos++;
                    }
                }

                if (administradoresActivos <= 1) {
                    throw new IllegalStateException(
                            "No se puede desactivar al último administrador activo"
                    );
                }
            }

            usuario.desactivar();

        } else {

            // REACTIVAR USUARIO
            usuario.reactivar();
        }
    }

}
