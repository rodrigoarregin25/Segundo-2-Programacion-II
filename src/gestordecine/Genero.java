package gestordecine;

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
    CRIMEN("Crimen");

    private final String descripcion;

    Genero(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}