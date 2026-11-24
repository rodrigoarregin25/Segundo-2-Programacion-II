package gestordecine;


/**
 * Géneros posibles de una película. Cada género guarda el texto con el que se
 * muestra en pantalla.
 */
public enum Genero {

    ACCION("Acción"),
    AVENTURA("Aventura"),
    ANIMACION("Animación"),
    CIENCIA_FICCION("Ciencia ficción"),
    COMEDIA("Comedia"),
    DRAMA("Drama"),
    FANTASIA("Fantasía"),
    TERROR("Terror"),
    SUSPENSO("Suspenso"),
    ROMANCE("Romance"),
    DOCUMENTAL("Documental"),
    MUSICAL("Musical"),
    CRIMEN("Crimen"),
    INFANTIL("Infantil");

    private final String descripcion;
    
     /**
     * Crea un género con el texto que se muestra al usuario.
     *
     * @param descripcion Nombre del género tal como se ve en pantalla.
     */

    Genero(String descripcion) {
        this.descripcion = descripcion;
    }
     /**
     * Devuelve el nombre del género para mostrarlo en pantalla, con tildes y
     * espacios, en lugar del nombre interno de la constante.
     *
     * @return La descripción del género, por ejemplo "Ciencia ficción".
     */
    @Override
    public String toString() {
        return descripcion;
    }
}