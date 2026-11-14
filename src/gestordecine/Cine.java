package gestordecine;

import java.util.ArrayList;

public class Cine {

    private final ArrayList<Pelicula> peliculas;
    private final ArrayList<Sala> salas;
    private final ArrayList<Funcion> funciones;

    public Cine() {
        this.peliculas = new ArrayList<>();
        this.salas = new ArrayList<>();
        this.funciones = new ArrayList<>();
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

}
