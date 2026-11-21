package gestordecine;

import java.io.Serializable;
import java.time.Year;
import java.util.ArrayList;

public class Pelicula implements Serializable {

    private static final long serialVersionUID = 3768598950091524L;

    private final int idPelicula;
    private String tituloPelicula;
    private final int duracionPelicula;
    private boolean activa;
    private int anioEstreno;
    private ArrayList<Genero> generos;
    private String director;
    private String actores;
    private String sinopsis;
    private double puntuacion;
    private String clasificacion;
    private String poster;

    public Pelicula(int idPelicula, String tituloPelicula, int duracionPelicula,
            int anioEstreno, ArrayList<Genero> generos, String director,
            String actores, String sinopsis, double puntuacion,
            String clasificacion, String poster) {

        if (idPelicula <= 0) {
            throw new IllegalArgumentException("El ID de la pelicula debe ser mayor a cero");
        }

        if (duracionPelicula <= 0) {
            throw new IllegalArgumentException("La duración debe ser mayor a cero");
        }
        setGeneros(generos);

        this.idPelicula = idPelicula;
        this.duracionPelicula = duracionPelicula;
        this.activa = true;

        setTituloPelicula(tituloPelicula);
        setAnioEstreno(anioEstreno);
        setDirector(director);
        setActores(actores);
        setSinopsis(sinopsis);
        setPuntuacion(puntuacion);
        setClasificacion(clasificacion);
        setPoster(poster);
    }

    // GETTERS
    public int getIdPelicula() {
        return idPelicula;
    }

    public String getTituloPelicula() {
        return tituloPelicula;
    }

    public int getDuracionPelicula() {
        return duracionPelicula;
    }

    public boolean isActiva() {
        return activa;
    }

    public int getAnioEstreno() {
        return anioEstreno;
    }

    public String getDirector() {
        return director;
    }

    public String getActores() {
        return actores;
    }

    public String getSinopsis() {
        return sinopsis;
    }

    public double getPuntuacion() {
        return puntuacion;
    }

    public String getClasificacion() {
        return clasificacion;
    }

    public String getPoster() {
        return poster;
    }

    public ArrayList<Genero> getGeneros() {
        if (generos == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(generos);
    }

    // SETTERS
    public void setTituloPelicula(String tituloPelicula) {
        if (tituloPelicula == null || tituloPelicula.isBlank()) {
            throw new IllegalArgumentException("El título de la película no puede estar vacío");
        }

        this.tituloPelicula = tituloPelicula.trim();
    }

    public void setAnioEstreno(int anioEstreno) {
        if (anioEstreno < 1888 || anioEstreno > Year.now().getValue() + 5) {
            throw new IllegalArgumentException("El año de estreno no es válido");
        }

        this.anioEstreno = anioEstreno;
    }

    public void setDirector(String director) {
        if (director == null || director.isBlank()) {
            throw new IllegalArgumentException("El director no puede estar vacío");
        }

        this.director = director.trim();
    }

    public void setGeneros(ArrayList<Genero> generos) {

        if (generos == null || generos.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debés seleccionar al menos un género."
            );
        }

        if (generos.contains(null)) {
            throw new IllegalArgumentException(
                    "Los géneros no pueden contener valores nulos."
            );
        }

        this.generos = new ArrayList<>(generos);
    }

    public void setActores(String actores) {
        if (actores == null || actores.isBlank()) {
            throw new IllegalArgumentException("Los actores no pueden estar vacíos");
        }

        this.actores = actores.trim();
    }

    public void setSinopsis(String sinopsis) {
        if (sinopsis == null || sinopsis.isBlank()) {
            throw new IllegalArgumentException("La sinopsis no puede estar vacía");
        }

        this.sinopsis = sinopsis.trim();
    }

    public void setPuntuacion(double puntuacion) {
        if (!Double.isFinite(puntuacion) || puntuacion < 0 || puntuacion > 10) {
            throw new IllegalArgumentException("La puntuación debe estar entre 0 y 10");
        }

        this.puntuacion = puntuacion;
    }

    public void setClasificacion(String clasificacion) {
        if (clasificacion == null || clasificacion.isBlank()) {
            throw new IllegalArgumentException("La clasificación no puede estar vacía");
        }

        this.clasificacion = clasificacion.trim();
    }

    public void setPoster(String poster) {
        if (poster == null || poster.isBlank()) {
            throw new IllegalArgumentException("El póster no puede estar vacío");
        }

        this.poster = poster.trim();
    }

    // ESTADO
    public void desactivar() {
        this.activa = false;
    }

    public void reactivar() {
        this.activa = true;
    }
}
