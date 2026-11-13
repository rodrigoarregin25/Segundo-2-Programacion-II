package gestordecine;

import java.time.LocalDateTime;

public class Funcion {

    private final int idFuncion;
    private final Sala sala;
    private final Pelicula pelicula;
    private double precio;
    private final LocalDateTime fechaHora;
    private final String formatoPelicula;
    private final String idiomaPelicula;

    public Funcion(int idFuncion, Sala sala, Pelicula pelicula, double precio, LocalDateTime fechaHora, String formatoPelicula, String idiomaPelicula) {
        setPrecio(precio);

        if (idFuncion <= 0) {
            throw new IllegalArgumentException("El numero de funcion debe ser mayor a cero");
        }
        this.idFuncion = idFuncion;

        if (sala == null) {
            throw new IllegalArgumentException("La sala no puede ser nula");
        }
        this.sala = sala;

        if (pelicula == null) {
            throw new IllegalArgumentException("La pelicula no puede ser nula");
        }
        if (!pelicula.isActiva()) {
            throw new IllegalArgumentException("La pelicula no puede estar inactiva");
        }
        this.pelicula = pelicula;

        if (fechaHora == null) {
            throw new IllegalArgumentException("La fecha/hora de la funcion no puede ser nula");
        }
        if (!fechaHora.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("La función debe programarse en una fecha y hora futura");
        }
        this.fechaHora = fechaHora;

        if (formatoPelicula == null || formatoPelicula.isBlank()) {
            throw new IllegalArgumentException("El formato no puede estar vacio");
        }

        formatoPelicula = formatoPelicula.trim().toUpperCase();

        if (!formatoPelicula.equals("2D")
                && !formatoPelicula.equals("3D")
                && !formatoPelicula.equals("D-BOX")
                && !formatoPelicula.equals("4D")) {
            throw new IllegalArgumentException("Formato de pelicula invalido");
        }

        this.formatoPelicula = formatoPelicula;

        if (idiomaPelicula == null || idiomaPelicula.isBlank()) {
            throw new IllegalArgumentException("El idioma no puede estar vacio");
        }

        idiomaPelicula = idiomaPelicula.trim().toUpperCase();

        if (!idiomaPelicula.equals("INGLES")
                && !idiomaPelicula.equals("CASTELLANO")
                && !idiomaPelicula.equals("INGLES(SUBTITULADA)")) {
            throw new IllegalArgumentException("Idioma de pelicula invalido");
        }

        this.idiomaPelicula = idiomaPelicula;
    }

    public void setPrecio(double precio) {
        if (precio <= 0 || !Double.isFinite(precio)) {
            throw new IllegalArgumentException("El precio debe ser un numero positivo y valido");
        }
        this.precio = precio;
    }

    public int getIdFuncion() {
        return idFuncion;
    }

    public Sala getSala() {
        return sala;
    }

    public Pelicula getPelicula() {
        return pelicula;
    }

    public double getPrecio() {
        return precio;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public String getFormatoPelicula() {
        return formatoPelicula;
    }

    public String getIdiomaPelicula() {
        return idiomaPelicula;
    }

    public LocalDateTime getFechaHoraFin() {
        return fechaHora.plusMinutes(pelicula.getDuracionPelicula());
    }

}
