package gestordecine;

import java.io.Serializable;
import java.time.Year;
import java.util.ArrayList;

/**
 * Representa una película registrada en el cine.
 *
 * Almacena su información general, como título, duración, géneros, director,
 * actores, sinopsis, puntuación, clasificación y póster.
 *
 * Implementa Desactivable para permitir su desactivación sin eliminarla del
 * sistema y Serializable para conservar sus datos mediante persistencia.
 *
 * El identificador y la duración no pueden modificarse después de crear la
 * película.
 *
 */
public class Pelicula implements Serializable, Desactivable {

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

    /**
     * Crea una pelicula, validando sus datos.
     *
     * Verifica que el identificador y la duracion sean positivos.
     *
     * @param idPelicula Identificador unico de la pelicula.
     * @param tituloPelicula Titulo de la pelicula.
     * @param duracionPelicula Duracion de la pelicula en minutos.
     * @param anioEstreno Año de estreno de la pelicula.
     * @param generos Generos de la pelicula.
     * @param director Directores de la pelicula.
     * @param actores Actores de la pelicula.
     * @param sinopsis Sinopsis de la pelicula.
     * @param puntuacion Puntuacion de la pelicula.
     * @param clasificacion Clasificacion de la pelicula.
     * @param poster Poster de la pelicula.
     *
     * @throws IllegalArgumentException Si el identificador o la duración no son
     * positivos, o si alguno de los demás atributos no cumple las validaciones
     * establecidas.
     */
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

    /**
     * @return Id de la pelicula.
     */
    public int getIdPelicula() {
        return idPelicula;
    }

    /**
     * @return Titulo de la pelicula.
     */
    public String getTituloPelicula() {
        return tituloPelicula;
    }

    /**
     * @return Duracion de la pelicula.
     */
    public int getDuracionPelicula() {
        return duracionPelicula;
    }

    /**
     * Indica si la pelicula se encuentra activa.
     *
     * @return True si esta activa, false si no lo esta.
     */
    @Override
    public boolean isActiva() {
        return activa;
    }

    /**
     * @return Año de estreno de la pelicula.
     */
    public int getAnioEstreno() {
        return anioEstreno;
    }

    /**
     * @return directores de la pelicula.
     */
    public String getDirector() {
        return director;
    }

    /**
     * @return actores de la pelicula.
     */
    public String getActores() {
        return actores;
    }

    /**
     * @return sinopsis de la pelicula.
     */
    public String getSinopsis() {
        return sinopsis;
    }

    /**
     * @return puntuacion de la pelicula.
     */
    public double getPuntuacion() {
        return puntuacion;
    }

    /**
     * @return clasificacion edad de la pelicula.
     */
    public String getClasificacion() {
        return clasificacion;
    }

    /**
     * @return poster de la pelicula.
     */
    public String getPoster() {
        return poster;
    }

    /**
     * Obtiene los géneros asociados a la película.
     *
     * @return Una nueva lista con los géneros registrados, o una lista vacía si
     * no existen.
     */
    public ArrayList<Genero> getGeneros() {
        if (generos == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(generos);
    }

    /**
     * Modifica el titulo de la pelicula.
     *
     * @param tituloPelicula titulo de la pelicula.
     *
     * @throws IllegalArgumentException si el titulo de la pelicula esta vacio.
     */
    public void setTituloPelicula(String tituloPelicula) {
        if (tituloPelicula == null || tituloPelicula.isBlank()) {
            throw new IllegalArgumentException("El título de la película no puede estar vacío");
        }

        this.tituloPelicula = tituloPelicula.trim();
    }

    /**
     * Modifica el año de estreno de la pelicula.
     *
     * @param anioEstreno año de estreno de la pelicula.
     *
     * @throws Si el año es anterior a 1888 o supera en más de cinco años al año
     * actual.
     */
    public void setAnioEstreno(int anioEstreno) {
        if (anioEstreno < 1888 || anioEstreno > Year.now().getValue() + 5) {
            throw new IllegalArgumentException("El año de estreno no es válido");
        }

        this.anioEstreno = anioEstreno;
    }

    /**
     * Modifica el director de la pelicula.
     *
     * @param director Director de la película
     *
     * @throws IllegalArgumentException si el director de la pelicula esta
     * vacio.
     */
    public void setDirector(String director) {
        if (director == null || director.isBlank()) {
            throw new IllegalArgumentException("El director no puede estar vacío");
        }

        this.director = director.trim();
    }

    /**
     * Modifica los generos de la pelicula-
     *
     * @param generos generos de la pelicula.
     *
     * @throws IllegalArgumentException no se selecciona ningun genero o tiene
     * valores nulos.
     */
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

    /**
     * Modifica los actores de la pelicula.
     *
     * @param actores actores de la pelicula.
     *
     * @throws IllegalArgumentException si los actores estan vacios.
     */
    public void setActores(String actores) {
        if (actores == null || actores.isBlank()) {
            throw new IllegalArgumentException("Los actores no pueden estar vacíos");
        }

        this.actores = actores.trim();
    }

    /**
     * Modifica la sinopsis de la pelicula.
     *
     * @param sinopsis sinopsis de la pelicula.
     *
     * @throws IllegalArgumentException si la sinopsis esta vacia.
     */
    public void setSinopsis(String sinopsis) {
        if (sinopsis == null || sinopsis.isBlank()) {
            throw new IllegalArgumentException("La sinopsis no puede estar vacía");
        }

        this.sinopsis = sinopsis.trim();
    }

    /**
     * Modifica la puntuacion de la pelicula.
     *
     * @param puntuacion puntuacion de la pelicula.
     *
     * @throws IllegalArgumentException si la puntuacion de la pelicula es menor
     * a cero o mayor a 10.
     */
    public void setPuntuacion(double puntuacion) {
        if (!Double.isFinite(puntuacion) || puntuacion < 0 || puntuacion > 10) {
            throw new IllegalArgumentException("La puntuación debe estar entre 0 y 10");
        }

        this.puntuacion = puntuacion;
    }

    /**
     * Modifica la clasificacion de la pelicula.
     *
     * @param clasificacion Clasificación por edad de la película
     *
     * @throws IllegalArgumentException si la clasificacion de la pelicula esta
     * vacia.
     */
    public void setClasificacion(String clasificacion) {
        if (clasificacion == null || clasificacion.isBlank()) {
            throw new IllegalArgumentException("La clasificación no puede estar vacía");
        }

        this.clasificacion = clasificacion.trim();
    }

    /**
     * Modifica el poster de la pelicula.
     *
     * @param poster Ruta o nombre del archivo del póster
     *
     * @throws IllegalArgumentException si el poster de la pelicula esta vacio.
     */
    public void setPoster(String poster) {
        if (poster == null || poster.isBlank()) {
            throw new IllegalArgumentException("El póster no puede estar vacío");
        }

        this.poster = poster.trim();
    }

    /**
     * Desactiva la pelicula sin eliminarla del sistema.
     */
    @Override
    public void desactivar() {
        this.activa = false;
    }

    /**
     * Reactiva una pelicula previamente desactivada.
     */
    @Override
    public void reactivar() {
        this.activa = true;
    }
}
