package gestordecine;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Cine implements Serializable {

    private final ArrayList<Pelicula> peliculas;
    private final ArrayList<Sala> salas;
    private final ArrayList<Funcion> funciones;
    private final ServicioDeReservas servicioDeReservas;


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

    public void editarPelicula(int idPelicula, String nuevoTitulo) {

        Pelicula pelicula = buscarPeliculaPorId(idPelicula);

        if (pelicula == null) {
            throw new IllegalArgumentException("La pelicula no existe");
        }

        pelicula.setTituloPelicula(nuevoTitulo);
    }

    public void desactivarPelicula(int idPelicula) {
        
        Pelicula pelicula = buscarPeliculaPorId(idPelicula);

        if (pelicula == null) {
            throw new IllegalArgumentException("La pelicula no existe");
        }

        pelicula.desactivar();

    }

}
