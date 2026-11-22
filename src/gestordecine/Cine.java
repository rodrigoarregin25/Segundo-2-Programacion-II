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

    public void registrarPelicula(Pelicula pelicula) throws RegistroDuplicadoException {

        if (pelicula == null) {
            throw new IllegalArgumentException("La pelicula no puede ser nula");
        }

        for (Pelicula existente : peliculas) {
            if (existente.getIdPelicula() == pelicula.getIdPelicula()) {
                throw new RegistroDuplicadoException("Ya existe una película con ese ID");
            }
        }
        peliculas.add(pelicula);
    }

    public void registrarSala(Sala sala) throws RegistroDuplicadoException {
        if (sala == null) {
            throw new IllegalArgumentException("La sala no puede ser nula");
        }

        for (Sala existente : salas) {
            if (existente.getIdSala() == sala.getIdSala()) {
                throw new RegistroDuplicadoException("Ya existe una sala con ese ID");
            }
            if (existente.getNombreSala().equalsIgnoreCase(sala.getNombreSala())) {
                throw new RegistroDuplicadoException("Ya existe una sala con ese nombre");
            }
        }

        salas.add(sala);
    }

    public void desactivarSala(int idSala) {

        Sala sala = buscarSalaPorId(idSala);

        if (sala == null) {
            throw new IllegalArgumentException("La sala no existe.");
        }

        if (!sala.isActiva()) {
            throw new IllegalStateException("La sala ya está desactivada.");
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

    public void programarFuncion(Funcion funcion) throws RegistroDuplicadoException {

        if (funcion == null) {
            throw new IllegalArgumentException("La funcion no puede ser nula");
        }

        boolean salaEncontrada = false;
        for (Sala existeSala : salas) {
            if (existeSala == funcion.getSala()) {
                salaEncontrada = true;
                break;
            }
        }
        if (!salaEncontrada) {
            throw new IllegalArgumentException("La sala no está registrada en el cine");
        }

        boolean peliculaEncontrada = false;
        for (Pelicula existePelicula : peliculas) {
            if (existePelicula == funcion.getPelicula()) {
                peliculaEncontrada = true;
                break;
            }
        }
        if (!peliculaEncontrada) {
            throw new IllegalArgumentException("La pelicula no esta registrada en el cine");

        }

        for (Funcion existente : funciones) {
            if (existente.getIdFuncion() == funcion.getIdFuncion()) {
                throw new RegistroDuplicadoException("Ya existe una funcion con ese ID");
            }

            if (existente.getSala() == funcion.getSala()) {

                if (funcion.getFechaHora().isBefore(existente.getFechaHoraFin()) && funcion.getFechaHoraFin().isAfter(existente.getFechaHora())) {
                    throw new IllegalArgumentException("Ya existe una funcion programada en ese horario");
                }
            }
        }

        if (!funcion.getPelicula().isActiva()) {
            throw new IllegalArgumentException("No se puede programar una función de una película inactiva");
        }

        funciones.add(funcion);
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
     * conservan su importe.
     * Parametros: idFuncion (int), nuevoPrecio (double)
     * Lanza: IllegalArgumentException si la funcion no existe o el precio no es valido
     */
    public void editarPrecioFuncion(int idFuncion, double nuevoPrecio) {
        Funcion funcion = buscarFuncionPorId(idFuncion);

        if (funcion == null) {
            throw new IllegalArgumentException("La funcion no existe");
        }

        funcion.setPrecio(nuevoPrecio);
    }

    /**
     * Que hace: elimina una funcion, solo si no tiene reservas
     * Parametros: idFuncion (int)
     * Lanza: IllegalArgumentException si la funcion no existe o tiene reservas
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

}
