/**
 * a
 */
package gestordecine;

import java.io.Serializable;


public class Pelicula implements Serializable {

    private final int idPelicula;
    private String tituloPelicula;
    private final int duracionPelicula;
    private boolean activa;

    public Pelicula(int idPelicula, String tituloPelicula, int duracionPelicula) {
        if (idPelicula <= 0) {
            throw new IllegalArgumentException("El ID de la pelicula debe ser mayor a cero");
        }

        this.idPelicula = idPelicula;
        
        if (duracionPelicula <= 0) {
            throw new IllegalArgumentException("La duración debe ser mayor a cero");
        }
        this.duracionPelicula = duracionPelicula;
        
        setTituloPelicula(tituloPelicula);
        this.activa = true;
    }

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

    public void setTituloPelicula(String tituloPelicula) {
        if (tituloPelicula == null || tituloPelicula.isBlank()) {
            throw new IllegalArgumentException("El titulo de la pelicula no puede estar vacio");
        }
        this.tituloPelicula = tituloPelicula.trim();
    }

    public void desactivar(){
        this.activa = false;
    }
}
