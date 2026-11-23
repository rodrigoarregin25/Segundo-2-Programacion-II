package gestordecine;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import gestordecine.repositorios.Repositorio;

/**
 *
 * Representa el sistema principal del cine.
 *
 * Administra las peliculas, salas y funciones, permitiendo su registro,
 * busqueda, modificacion y desactivacion.
 *
 * Tambien coordina la gestion de reservas mediante ServicioDeReservas, valida
 * las reglas de negocio y permite generar una copia del estado del cine para
 * realizar modificaciones antes de persistirlas.
 */
public class Cine implements Serializable {

    private final ArrayList<Pelicula> peliculas;
    private final ArrayList<Sala> salas;
    private final ArrayList<Funcion> funciones;
    private final ServicioDeReservas servicioDeReservas;
    private static final long serialVersionUID = -7996707478867040710L;

    public Cine() {
        this.peliculas = new ArrayList<>();
        this.salas = new ArrayList<>();
        this.funciones = new ArrayList<>();
        this.servicioDeReservas = new ServicioDeReservas();
    }

    /**
     * @return Una nueva lista con las películas registradas.
     */
    public ArrayList<Pelicula> getPeliculas() {
        return new ArrayList<>(peliculas);
    }

    /**
     * @return Una nueva lista con las salas registradas.
     */
    public ArrayList<Sala> getSalas() {
        return new ArrayList<>(salas);
    }

    /**
     * @return Una nueva lista con las funciones registradas.
     */
    public ArrayList<Funcion> getFunciones() {
        return new ArrayList<>(funciones);
    }

    /**
     * @return El servicio de reservas asociado al cine.
     */
    public ServicioDeReservas getServicioDeReservas() {
        return servicioDeReservas;
    }

    /**
     * Crea una copia independiente del estado actual del cine mediante
     * serialización y deserialización en memoria.
     *
     * Permite realizar modificaciones sobre la copia sin afectar al objeto
     * original hasta que los cambios sean validados y persistidos.
     *
     * @return Una copia del objeto Cine con su estado actual.
     * @throws IOException Si ocurre un error durante la serialización o
     * deserialización del objeto.
     * @throws ClassNotFoundException Si no se encuentra alguna clase necesaria
     * para reconstruir el objeto.
     */
    public Cine copiar() throws IOException, ClassNotFoundException {

        ByteArrayOutputStream memoria = new ByteArrayOutputStream();

        try (ObjectOutputStream salida
                = new ObjectOutputStream(memoria)) {

            salida.writeObject(this);
        }

        try (ObjectInputStream entrada
                = new ObjectInputStream(
                        new ByteArrayInputStream(memoria.toByteArray()))) {

            Cine copia = (Cine) entrada.readObject();

            copia.getServicioDeReservas().recargarCodigos();

            return copia;
        }
    }

    /**
     * Registra una película en el cine, comprobando que su ID no se encuentre
     * asociado a otra película.
     *
     * @param pelicula Película que se desea registrar.
     * @throws IllegalArgumentException Si la película es nula.
     * @throws RegistroDuplicadoException Si ya existe una película con el mismo
     * identificador.
     */
    public void registrarPelicula(Pelicula pelicula)
            throws RegistroDuplicadoException {

        if (pelicula == null) {
            throw new IllegalArgumentException("La pelicula no puede ser nula");
        }

        Repositorio<Pelicula> repositorio = new Repositorio<>(peliculas);

        Pelicula existente = repositorio.buscar(
                p -> p.getIdPelicula() == pelicula.getIdPelicula()
        );

        if (existente != null) {
            throw new RegistroDuplicadoException(
                    "Ya existe una película con ese ID"
            );
        }

        repositorio.agregar(pelicula);
    }

    /**
     * Registra una sala en el cine. Verifica que no exista otra sala con el
     * mismo ID o nombre.
     *
     * @param sala Sala que se desea registrar.
     *
     * @throws IllegalArgumentException Si la sala es nula.
     * @throws RegistroDuplicadoException Si existe una sala con el mismo
     * identificador o nombre.
     */
    public void registrarSala(Sala sala) throws RegistroDuplicadoException {

        if (sala == null) {
            throw new IllegalArgumentException("La sala no puede ser nula");
        }

        Repositorio<Sala> repositorio = new Repositorio<>(salas);

        // Verificar si existe una sala con el mismo identificador.
        Sala salaMismoId = repositorio.buscar(
                s -> s.getIdSala() == sala.getIdSala()
        );

        if (salaMismoId != null) {
            throw new RegistroDuplicadoException(
                    "Ya existe una sala con ese ID"
            );
        }

        // Verificar si existe una sala con el mismo nombre.
        Sala salaMismoNombre = repositorio.buscar(
                s -> s.getNombreSala().equalsIgnoreCase(sala.getNombreSala())
        );

        if (salaMismoNombre != null) {
            throw new RegistroDuplicadoException(
                    "Ya existe una sala con ese nombre"
            );
        }

        // Registrar la sala una vez superadas las validaciones.
        repositorio.agregar(sala);
    }

    /**
     * Desactiva una sala en el cine. Verifica que la sala no tenga funciones
     * activas, que la sala exista y/o que la sala ya este desactivada.
     *
     * @param idSala Sala que se desea desactivar.
     *
     * @throws IllegalArgumentException Si la sala no existe.
     * @throws IllegalStateException Si el estado de la sala no es correcto.
     */
    public void desactivarSala(int idSala) {

        Sala sala = buscarSalaPorId(idSala);

        if (sala == null) {
            throw new IllegalArgumentException("La sala no existe.");
        }

        if (!sala.isActiva()) {
            throw new IllegalStateException("La sala ya está desactivada.");
        }

        for (Funcion funcion : funciones) {

            boolean mismaSala
                    = funcion.getSala().getIdSala() == idSala;

            boolean funcionPendiente
                    = funcion.getFechaHoraFin().isAfter(LocalDateTime.now());

            if (mismaSala && funcion.isActiva() && funcionPendiente) {
                throw new IllegalStateException(
                        "No se puede desactivar la sala porque tiene funciones pendientes o en curso."
                );
            }
        }

        sala.desactivar();
    }

    /**
     * Reactiva una sala en el cine. Verifica que la sala exista y/o que la sala
     * ya este activa.
     *
     * @param idSala Sala que se desea activar.
     *
     * @throws IllegalArgumentException Si la sala no existe.
     * @throws IllegalStateException Si el estado de la sala no es correcto.
     */
    public void reactivarSala(int idSala) {

        Sala sala = buscarSalaPorId(idSala);

        if (sala == null) {
            throw new IllegalArgumentException("La sala no existe.");
        }

        if (sala.isActiva()) {
            throw new IllegalStateException("La sala ya está activa.");
        }

        sala.reactivar();
    }

    /**
     * Programa una nueva función en el cine.
     *
     * Verifica que la sala y la película estén registradas y activas, que no
     * exista otra función con el mismo ID y que no haya superposición de
     * horarios en la misma sala.
     *
     * Las funciones desactivadas no ocupan horarios.
     *
     * @param funcion Función que se desea programar.
     *
     * @throws IllegalArgumentException Si la función es nula, si la sala o
     * película no están registradas o activas, o si existe una superposición de
     * horarios.
     * @throws RegistroDuplicadoException Si ya existe una función con el mismo
     * identificador.
     */
    public void programarFuncion(Funcion funcion)
            throws RegistroDuplicadoException {

        if (funcion == null) {
            throw new IllegalArgumentException("La función no puede ser nula");
        }

        // Verificar que la sala pertenezca al cine.
        boolean salaEncontrada = false;

        for (Sala sala : salas) {
            if (sala == funcion.getSala()) {
                salaEncontrada = true;
                break;
            }
        }

        // Verificar que la sala este en el cine.
        if (!salaEncontrada) {
            throw new IllegalArgumentException(
                    "La sala no está registrada en el cine"
            );
        }

        // Verificar que la sala esté activa.
        if (!funcion.getSala().isActiva()) {
            throw new IllegalArgumentException(
                    "No se puede programar una función en una sala inactiva"
            );
        }

        // Verificar que la película pertenezca al cine.
        boolean peliculaEncontrada = false;

        for (Pelicula pelicula : peliculas) {
            if (pelicula == funcion.getPelicula()) {
                peliculaEncontrada = true;
                break;
            }
        }

        // Verificar que la pelicula corresponda al cine.
        if (!peliculaEncontrada) {
            throw new IllegalArgumentException(
                    "La película no está registrada en el cine"
            );
        }

        // Verificar que la película esté activa.
        if (!funcion.getPelicula().isActiva()) {
            throw new IllegalArgumentException(
                    "No se puede programar una función de una película inactiva"
            );
        }

        // Crear un repositorio sobre la lista de funciones existente.
        Repositorio<Funcion> repositorio = new Repositorio<>(funciones);

        // Verificar que el identificador no esté repetido.
        Funcion funcionDuplicada = repositorio.buscar(
                f -> f.getIdFuncion() == funcion.getIdFuncion()
        );

        if (funcionDuplicada != null) {
            throw new RegistroDuplicadoException(
                    "Ya existe una función con ese ID"
            );
        }

        // Verificar que no existan funciones superpuestas.
        for (Funcion existente : repositorio.listar()) {

            // Las funciones desactivadas no ocupan horarios.
            if (!existente.isActiva()) {
                continue;
            }

            // Solo interesa comparar funciones de la misma sala.
            if (existente.getSala() != funcion.getSala()) {
                continue;
            }

            boolean superposicion
                    = funcion.getFechaHora().isBefore(existente.getFechaHoraFin())
                    && funcion.getFechaHoraFin().isAfter(existente.getFechaHora());

            if (superposicion) {
                throw new IllegalArgumentException(
                        "Ya existe una función programada en ese horario"
                );
            }
        }

        // Registrar la función después de superar todas las validaciones.
        repositorio.agregar(funcion);
    }

    /**
     * Busca una pelicula por su ID interno
     *
     * @param id Id de la pelicula que se desea buscar.
     *
     * @return La pelicula encontrada, caso contrario null.
     */
    public Pelicula buscarPeliculaPorId(int id) {

        for (Pelicula pelicula : peliculas) {
            if (pelicula.getIdPelicula() == id) {
                return pelicula;
            }
        }

        return null;
    }

    /**
     * Busca una sala por su ID interno
     *
     * @param id Id de la sala que se desea buscar.
     *
     * @return La sala encontrada, caso contrario null.
     */
    public Sala buscarSalaPorId(int id) {

        for (Sala sala : salas) {
            if (sala.getIdSala() == id) {
                return sala;
            }
        }

        return null;
    }

    /**
     * Busca una funcion por su ID interno
     *
     * @param id ID de la funcion que se desea buscar.
     *
     * @return La funcion encontrada, caso contrario null.
     */
    public Funcion buscarFuncionPorId(int id) {

        for (Funcion funcion : funciones) {
            if (funcion.getIdFuncion() == id) {
                return funcion;
            }
        }

        return null;
    }

    /**
     * Busca funciones de una pelicula específica.
     *
     * @param idPelicula Id de la pelicula que se desea buscar.
     *
     * @return Un ArrayList de las funciones encontradas, en caso de no hacerlo
     * retorna un ArrayList vacio.
     */
    public ArrayList<Funcion> buscarFuncionesPorPelicula(int idPelicula) {
        ArrayList<Funcion> resultados = new ArrayList<>();

        for (Funcion funcion : funciones) {
            if (funcion.getPelicula().getIdPelicula() == idPelicula) {
                resultados.add(funcion);
            }
        }

        return resultados;
    }

    /**
     * Busca funciones de una sala específica.
     *
     * @param idSala Id de la sala sobre la que se quiere buscar.
     *
     * @return Un ArrayList de las funciones encontradas, en caso de no hacerlo
     * retorna un ArrayList vacio.
     */
    public ArrayList<Funcion> buscarFuncionesPorSala(int idSala) {
        ArrayList<Funcion> resultados = new ArrayList<>();

        for (Funcion funcion : funciones) {
            if (funcion.getSala().getIdSala() == idSala) {
                resultados.add(funcion);
            }
        }

        return resultados;
    }

    /**
     * Busca funciones de una fecha específica.
     *
     * @param fecha fecha que se quiere buscar la funcion.
     *
     * @return Un ArrayList de las funciones encontradas, en caso de no hacerlo
     * retorna un ArrayList vacio.
     *
     * @throws IllegalArgumentException Si la fecha es nula.
     */
    public ArrayList<Funcion> buscarFuncionesPorFecha(LocalDate fecha) {
        ArrayList<Funcion> resultados = new ArrayList<>();

        if (fecha == null) {
            throw new IllegalArgumentException("La fecha no puede ser nula");
        }

        for (Funcion funcion : funciones) {
            if (funcion.getFechaHora().toLocalDate().equals(fecha)) {
                resultados.add(funcion);
            }
        }

        return resultados;
    }

    /**
     * Busca funciones en fechas futuras a la actual.
     *
     * @return Un ArrayList de las funciones encontradas, en caso de no hacerlo
     * retorna un ArrayList vacio.
     *
     */
    public ArrayList<Funcion> buscarFuncionesFuturas() {
        ArrayList<Funcion> resultados = new ArrayList<>();

        LocalDateTime ahora = LocalDateTime.now();

        for (Funcion funcion : funciones) {
            if (funcion.getFechaHora().isAfter(ahora)) {
                resultados.add(funcion);
            }
        }

        return resultados;
    }

    /**
     * Modifica los datos de una película existente.
     *
     * Valida los nuevos valores creando una instancia temporal de Pelicula y,
     * si son correctos, actualiza los atributos de la película registrada.
     *
     * La duración y el identificador de la película no se modifican.
     *
     * @param idPelicula Id de la pelicula.
     * @param titulo Titulo de la pelicula.
     * @param anio Año de la pelicula.
     * @param generos Generos de la pelicula.
     * @param director Directores de la pelicula.
     * @param actores Actores de la pelicula.
     * @param sinopsis Sinopsis de la pelicula.
     * @param puntuacion Puntuacion de la pelicula.
     * @param clasificacion Clasificacion de la pelicula.
     * @param poster Poster de la pelicula.
     *
     * @throws IllegalArgumentException si la pelicula no existe
     */
    public void editarPelicula(
            int idPelicula,
            String titulo,
            int anio,
            ArrayList<Genero> generos,
            String director,
            String actores,
            String sinopsis,
            double puntuacion,
            String clasificacion,
            String poster) {

        Pelicula pelicula = buscarPeliculaPorId(idPelicula);

        if (pelicula == null) {
            throw new IllegalArgumentException("La pelicula no existe");
        }

        new Pelicula(
                pelicula.getIdPelicula(),
                titulo,
                pelicula.getDuracionPelicula(),
                anio,
                generos,
                director,
                actores,
                sinopsis,
                puntuacion,
                clasificacion,
                poster
        );

        pelicula.setTituloPelicula(titulo);
        pelicula.setAnioEstreno(anio);
        pelicula.setGeneros(generos);
        pelicula.setDirector(director);
        pelicula.setActores(actores);
        pelicula.setSinopsis(sinopsis);
        pelicula.setPuntuacion(puntuacion);
        pelicula.setClasificacion(clasificacion);
        pelicula.setPoster(poster);
    }

    /**
     * Desactiva una película registrada.
     *
     * Impide la operación si la película tiene funciones pendientes o en curso.
     *
     * @param idPelicula Id de la pelicula que se quiere desactivar.
     *
     * @throws IllegalArgumentException si la pelicula no existe.
     * @throws IllegalStateException si el estado actual de la pelicula no es
     * apto para su desactivacion.
     */
    public void desactivarPelicula(int idPelicula) {

        Pelicula pelicula = buscarPeliculaPorId(idPelicula);

        if (pelicula == null) {
            throw new IllegalArgumentException("La pelicula no existe");
        }

        for (Funcion funcion : funciones) {

            boolean mismaPelicula
                    = funcion.getPelicula().getIdPelicula() == idPelicula;

            boolean funcionPendiente
                    = funcion.getFechaHoraFin().isAfter(LocalDateTime.now());

            if (mismaPelicula && funcionPendiente) {
                throw new IllegalStateException(
                        "No se puede desactivar la película porque tiene "
                        + "funciones pendientes o en curso."
                );
            }
        }
        pelicula.desactivar();
    }

    /**
     * Permite reactivar una pelicula llamando a al método reactivar al pasar
     * las validaciones.
     *
     * @param idPelicula que se quiere reactivar.
     *
     * @throws IllegalArgumentException si la pelicula no existe.
     * @throws IllegalStateException si el estado actual de la pelicula no es
     * apto para su reativacion.
     */
    public void reactivarPelicula(int idPelicula) {

        Pelicula pelicula = buscarPeliculaPorId(idPelicula);

        if (pelicula == null) {
            throw new IllegalArgumentException("La película no existe.");
        }

        if (pelicula.isActiva()) {
            throw new IllegalStateException("La película ya está activa.");
        }

        pelicula.reactivar();
    }

    /**
     * Permite confirmar una reserva de funcion luego de sus validaciones.
     *
     * @param cliente Cliente de la reserva.
     * @param funcion Funcion que se quiere reservar.
     * @param entradas Array de entradas seleccionadas para la funcion.
     *
     * @return La reserva confirmada.
     *
     * @throws IllegalArgumentException si la funcion es nula o no existe.
     * @throws IllegalStateException si el estado de la funcion no es apto para
     * la reserva.
     */
    public Reserva confirmarReserva(Cliente cliente, Funcion funcion, ArrayList<Entrada> entradas) {
        if (funcion == null) {
            throw new IllegalArgumentException("La funcion no puede ser nula");
        }

        Funcion registrada = buscarFuncionPorId(funcion.getIdFuncion());

        if (registrada != funcion) {
            throw new IllegalArgumentException(
                    "La funcion no esta registrada en este cine");
        }

        if (!funcion.isActiva()) {
            throw new IllegalStateException("La funcion esta desactivada");
        }

        return servicioDeReservas.confirmarReserva(cliente, funcion, entradas);
    }

    /**
     * Permite obtener el Id de la siguiente pelicula a registrar.
     *
     * @return El mayor identificador existente incrementado en uno.
     */
    public int obtenerSiguienteIdPelicula() {
        int mayorId = 0;

        for (Pelicula pelicula : peliculas) {
            if (pelicula.getIdPelicula() > mayorId) {
                mayorId = pelicula.getIdPelicula();
            }
        }

        return mayorId + 1;
    }

    /**
     * Permite obtener el Id de la siguiente sala a registrar.
     *
     * @return El mayor identificador existente incrementado en uno.
     */
    public int obtenerSiguienteIdSala() {

        int mayorId = 0;

        for (Sala sala : salas) {
            if (sala.getIdSala() > mayorId) {
                mayorId = sala.getIdSala();
            }
        }

        return mayorId + 1;
    }

    /**
     * Permite editar una sala existente.
     *
     * @param idSala Id de la sala que se quiere editar.
     * @param nuevoNombre Nuevo nombre de la sala, es decir, luego de editar.
     *
     * @throws IllegalArgumentException si la sala no existe o el nombre ya
     * existe.
     */
    public void editarSala(int idSala, String nuevoNombre) {

        Sala sala = buscarSalaPorId(idSala);

        if (sala == null) {
            throw new IllegalArgumentException("La sala no existe");
        }

        for (Sala existente : salas) {
            if (existente.getIdSala() != idSala
                    && existente.getNombreSala().equalsIgnoreCase(nuevoNombre.trim())) {

                throw new IllegalArgumentException(
                        "Ya existe otra sala con ese nombre"
                );
            }
        }

        sala.setNombreSala(nuevoNombre);
    }

    /**
     * Modifica únicamente el precio de una función existente y activa.
     *
     * Permite actualizar el precio de futuras entradas sin alterar los importes
     * de las reservas realizadas anteriormente.
     *
     * @param idFuncion Identificador de la función que se desea modificar.
     * @param nuevoPrecio Nuevo precio que se aplicará a futuras compras.
     *
     * @throws IllegalArgumentException Si la función no existe o el precio
     * ingresado no es válido.
     * @throws IllegalStateException Si la función ya comenzó o está
     * desactivada.
     */
    public void editarPrecioFuncion(int idFuncion, double nuevoPrecio) {
        Funcion funcion = buscarFuncionPorId(idFuncion);

        if (funcion == null) {
            throw new IllegalArgumentException("La funcion no existe");
        }

        if (!funcion.getFechaHora().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException(
                    "No se puede editar una función que ya comenzó."
            );
        }

        if (!funcion.isActiva()) {
            throw new IllegalStateException(
                    "No se puede modificar una función desactivada."
            );
        }

        funcion.setPrecio(nuevoPrecio);
    }

    /**
     * Elimina una función únicamente si no tiene reservas asociadas,
     * independientemente de que estén confirmadas o canceladas.
     *
     * @param idFuncion Id de la funcion que se quiere eliminar.
     *
     * @throws IllegalArgumentException si la funcion no existe o tiene
     * reservas.
     */
    public void eliminarFuncion(int idFuncion) {
        Funcion funcion = buscarFuncionPorId(idFuncion);

        if (funcion == null) {
            throw new IllegalArgumentException("La funcion no existe");
        }

        if (servicioDeReservas.tieneReservas(funcion)) {
            throw new IllegalArgumentException(
                    "No se puede eliminar: la funcion tiene reservas");
        }

        funciones.remove(funcion);
    }

    /**
     * Desactiva una función sin eliminarla del sistema.
     *
     * Impide desactivar funciones en curso o funciones futuras que tengan
     * reservas confirmadas. Las reservas existentes permanecen registradas en
     * el historial.
     *
     * @param idFuncion Identificador de la función.
     *
     * @throws IllegalArgumentException Si la función no existe.
     * @throws IllegalStateException Si la función ya está desactivada, está en
     * curso o tiene reservas confirmadas pendientes.
     */
    public void desactivarFuncion(int idFuncion) {

        Funcion funcion = buscarFuncionPorId(idFuncion);

        if (funcion == null) {
            throw new IllegalArgumentException("La función no existe");
        }

        if (!funcion.isActiva()) {
            throw new IllegalStateException("La función ya está desactivada");
        }

        LocalDateTime ahora = LocalDateTime.now();

        // Impedir desactivar funciones que están en curso.
        if (!funcion.getFechaHora().isAfter(ahora)
                && funcion.getFechaHoraFin().isAfter(ahora)) {

            throw new IllegalStateException(
                    "No se puede desactivar una función que está en curso"
            );
        }

        // Verificar si tiene reservas confirmadas para una función futura.
        if (funcion.getFechaHora().isAfter(ahora)) {

            for (Reserva reserva : servicioDeReservas.getReservas()) {

                boolean mismaFuncion
                        = reserva.getFuncion().getIdFuncion() == idFuncion;

                boolean reservaConfirmada = reserva.isConfirmada();

                if (mismaFuncion && reservaConfirmada) {
                    throw new IllegalStateException(
                            "No se puede desactivar la función porque "
                            + "tiene reservas confirmadas."
                    );
                }
            }
        }

        funcion.desactivar();
    }

    /**
     * Reactiva una funcion desactivada Parametros: idFuncion
     *
     * @param idFuncion Id de la funcion que se quiere reactivar.
     *
     * @throws IllegalArgumentException si la funcion no existe.
     * @throws IllegalStateException si el estado de la funcion no es apto.
     */
    public void reactivarFuncion(int idFuncion) {

        Funcion funcion = buscarFuncionPorId(idFuncion);

        if (funcion == null) {
            throw new IllegalArgumentException("La función no existe");
        }

        if (funcion.isActiva()) {
            throw new IllegalStateException("La función ya está activa");
        }

        // Validar que la función todavía sea futura
        if (!funcion.getFechaHora().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException(
                    "No se puede reactivar una función que ya comenzó"
            );
        }

        // Validar que la sala esté activa
        if (!funcion.getSala().isActiva()) {
            throw new IllegalStateException(
                    "No se puede reactivar la función porque la sala está inactiva"
            );
        }

        // Validar que la película esté activa
        if (!funcion.getPelicula().isActiva()) {
            throw new IllegalStateException(
                    "No se puede reactivar la función porque la película está inactiva"
            );
        }

        // Comprobar que no existan funciones superpuestas
        for (Funcion existente : funciones) {

            if (existente.getIdFuncion() == idFuncion) {
                continue;
            }

            if (!existente.isActiva()) {
                continue;
            }

            boolean mismaSala
                    = existente.getSala().getIdSala()
                    == funcion.getSala().getIdSala();

            boolean horariosSuperpuestos
                    = funcion.getFechaHora().isBefore(existente.getFechaHoraFin())
                    && funcion.getFechaHoraFin().isAfter(existente.getFechaHora());

            if (mismaSala && horariosSuperpuestos) {
                throw new IllegalStateException(
                        "No se puede reactivar la función porque "
                        + "ya existe otra función programada en ese horario."
                );
            }
        }

        // Reactivar solamente si todas las validaciones fueron exitosas
        funcion.reactivar();
    }

    /**
     * Modifica los datos de una función futura y activa, conservando su ID.
     *
     * Impide modificar funciones con reservas asociadas y valida que la sala y
     * la película estén activas, que los nuevos datos sean correctos y que no
     * existan superposiciones de horarios.
     *
     * @param idFuncion Identificador de la función.
     * @param idSala Identificador de la sala.
     * @param idPelicula Identificador de la película.
     * @param precio Nuevo precio.
     * @param fechaHora Nueva fecha y hora de inicio.
     * @param formato Nuevo formato de proyección.
     * @param idioma Nuevo idioma.
     *
     * @throws IllegalArgumentException Si los datos no son válidos o existe una
     * superposición de horarios.
     * @throws IllegalStateException Si la función ya comenzó, está desactivada
     * o tiene reservas asociadas.
     */
    public void editarFuncion(int idFuncion, int idSala, int idPelicula,
            double precio, LocalDateTime fechaHora,
            String formato, String idioma) {

        Funcion actual = buscarFuncionPorId(idFuncion);

        if (actual == null) {
            throw new IllegalArgumentException("La función no existe");
        }

        if (!actual.getFechaHora().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException(
                    "No se puede editar una función que ya comenzó."
            );
        }

        if (servicioDeReservas.tieneReservas(actual)) {
            throw new IllegalStateException(
                    "No se puede modificar la programación de una función con reservas"
            );
        }

        if (!actual.isActiva()) {
            throw new IllegalStateException(
                    "No se puede reprogramar una función desactivada"
            );
        }

        Sala sala = buscarSalaPorId(idSala);
        Pelicula pelicula = buscarPeliculaPorId(idPelicula);

        if (sala == null || !sala.isActiva()) {
            throw new IllegalArgumentException("Seleccione una sala activa");
        }

        if (pelicula == null || !pelicula.isActiva()) {
            throw new IllegalArgumentException("Seleccione una película activa");
        }

        // El constructor valida fecha, formato, idioma y precio.
        Funcion propuesta = new Funcion(
                idFuncion, sala, pelicula, precio,
                fechaHora, formato, idioma
        );

        // Controlar superposiciones, excluyendo la función editada.
        for (Funcion existente : funciones) {

            if (existente.getIdFuncion() == idFuncion
                    || !existente.isActiva()) {
                continue;
            }

            if (existente.getSala().getIdSala() != idSala) {
                continue;
            }

            boolean superposicion
                    = propuesta.getFechaHora().isBefore(existente.getFechaHoraFin())
                    && propuesta.getFechaHoraFin().isAfter(existente.getFechaHora());

            if (superposicion) {
                throw new IllegalArgumentException(
                        "Ya existe otra función en esa sala y horario"
                );
            }
        }

        // Aplicar todos los cambios una vez validados.
        actual.actualizarDatos(propuesta);
    }

}
