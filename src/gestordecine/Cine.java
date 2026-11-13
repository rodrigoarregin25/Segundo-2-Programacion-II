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

            for (Funcion existente : funciones) {
                if (existente.getIdFuncion() == funcion.getIdFuncion()) {
                    throw new RegistroDuplicadoException("Ya existe una funcion con ese ID");
                }
            }
            
            boolean salaEncontrada = false;
            for (Sala existe : salas){
                if (existe.getIdSala() == funcion.getSala())


        }

    }

}
