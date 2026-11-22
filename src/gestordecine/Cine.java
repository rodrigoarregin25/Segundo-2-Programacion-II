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

    public ArrayList<Pelicula> getPeliculas() {
        return new ArrayList<>(peliculas);
    }

    public ArrayList<Sala> getSalas() {
        return new ArrayList<>(salas);
    }

    public ArrayList<Funcion> getFunciones() {
        return new ArrayList<>(funciones);
    }

    public ServicioDeReservas getServicioDeReservas() {
        return servicioDeReservas;
    }

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

            if (mismaSala && funcionPendiente) {
                throw new IllegalStateException(
                        "No se puede desactivar la sala porque tiene "
                        + "funciones pendientes o en curso."
                );
            }
        }

        sala.desactivar();
    }

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

    public Pelicula buscarPeliculaPorId(int id) {

        for (Pelicula pelicula : peliculas) {
            if (pelicula.getIdPelicula() == id) {
                return pelicula;
            }

        }

        return null;
    }

    public Sala buscarSalaPorId(int id) {

        for (Sala sala : salas) {
            if (sala.getIdSala() == id) {
                return sala;
            }

        }

        return null;
    }

    public Funcion buscarFuncionPorId(int id) {

        for (Funcion funcion : funciones) {
            if (funcion.getIdFuncion() == id) {
                return funcion;
            }

        }

        return null;
    }

    public ArrayList<Funcion> buscarFuncionesPorPelicula(int idPelicula) {
        ArrayList<Funcion> resultados = new ArrayList<>();

        for (Funcion funcion : funciones) {
            if (funcion.getPelicula().getIdPelicula() == idPelicula) {
                resultados.add(funcion);
            }
        }

        return resultados;

    }

    public ArrayList<Funcion> buscarFuncionesPorSala(int idSala) {
        ArrayList<Funcion> resultados = new ArrayList<>();

        for (Funcion funcion : funciones) {
            if (funcion.getSala().getIdSala() == idSala) {
                resultados.add(funcion);
            }
        }

        return resultados;

    }

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
            throw new IllegalArgumentException("La funcion esta desactivada");
        }

        return servicioDeReservas.confirmarReserva(cliente, funcion, entradas);

    }

    public int obtenerSiguienteIdPelicula() {
        int mayorId = 0;

        for (Pelicula pelicula : peliculas) {
            if (pelicula.getIdPelicula() > mayorId) {
                mayorId = pelicula.getIdPelicula();
            }
        }

        return mayorId + 1;
    }

    public int obtenerSiguienteIdSala() {

        int mayorId = 0;

        for (Sala sala : salas) {
            if (sala.getIdSala() > mayorId) {
                mayorId = sala.getIdSala();
            }
        }

        return mayorId + 1;
    }

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
     * Que hace: cambia el precio de una funcion. Las reservas que ya existen
     * conservan su importe. Parametros: idFuncion (int), nuevoPrecio (double)
     * Lanza: IllegalArgumentException si la funcion no existe o el precio no es
     * valido
     */
    public void editarPrecioFuncion(int idFuncion, double nuevoPrecio) {
        Funcion funcion = buscarFuncionPorId(idFuncion);

        if (funcion == null) {
            throw new IllegalArgumentException("La funcion no existe");
        }

        funcion.setPrecio(nuevoPrecio);
    }

    /**
     * Que hace: elimina una funcion, solo si no tiene reservas Parametros:
     * idFuncion (int) Lanza: IllegalArgumentException si la funcion no existe o
     * tiene reservas
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
     * Que hace: desactiva una funcion (no se borra, las reservas quedan en el
     * historial) Parametros: idFuncion (int) Lanza: IllegalArgumentException si
     * la funcion no existe, IllegalStateException si ya estaba desactivada
     */
    public void desactivarFuncion(int idFuncion) {

        Funcion funcion = buscarFuncionPorId(idFuncion);

        if (funcion == null) {
            throw new IllegalArgumentException("La función no existe");
        }

        if (!funcion.isActiva()) {
            throw new IllegalStateException("La función ya está desactivada");
        }

        // Verificar si tiene reservas confirmadas para una función futura
        if (funcion.getFechaHora().isAfter(LocalDateTime.now())) {

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
     * Que hace: vuelve a activar una funcion desactivada Parametros: idFuncion
     * (int) Lanza: IllegalArgumentException si la funcion no existe,
     * IllegalStateException si ya estaba activa
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

}
