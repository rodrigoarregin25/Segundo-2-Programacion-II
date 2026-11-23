package gestordecine;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Representa una función cinematográfica programada en el cine.
 *
 * Contiene la película, sala, precio, fecha y hora, formato e idioma de
 * proyección.
 *
 * Implementa Desactivable para permitir su desactivación sin eliminarla del
 * sistema, conservando las referencias de las reservas existentes.
 *
 * Implementa Serializable para permitir su persistencia.
 */
public class Funcion implements Serializable, Desactivable {

    private static final long serialVersionUID = -8444399254319352013L;
    private final int idFuncion;
    private Sala sala;
    private Pelicula pelicula;
    private double precio;
    private LocalDateTime fechaHora;
    private String formatoPelicula;
    private String idiomaPelicula;
    private boolean desactivada; // false por defecto: en los datos guardados antes, todas las funciones siguen activas

    /**
     * Crea una función cinematográfica validando sus datos.
     *
     * Verifica que el identificador y el precio sean positivos, que la sala y
     * la película no sean nulas, que la película esté activa y que la fecha y
     * hora sean futuras.
     *
     * También valida que el formato y el idioma sean valores permitidos.
     *
     * @param idFuncion Identificador único de la función.
     * @param sala Sala donde se proyectará la película.
     * @param pelicula Película que se proyectará.
     * @param precio Precio base de las entradas.
     * @param fechaHora Fecha y hora de inicio de la función.
     * @param formatoPelicula Formato de proyección.
     * @param idiomaPelicula Idioma de la película.
     *
     * @throws IllegalArgumentException Si alguno de los datos ingresados no
     * cumple las validaciones.
     */
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

    /**
     * Modifica el precio base de la función.
     *
     * @param precio Nuevo precio de la función.
     *
     * @throws IllegalArgumentException Si el precio no es positivo o no es un
     * número finito.
     */
    public void setPrecio(double precio) {
        if (precio <= 0 || !Double.isFinite(precio)) {
            throw new IllegalArgumentException("El precio debe ser un numero positivo y valido");
        }
        this.precio = precio;
    }

    /**
     * @return Identificador de la función.
     */
    public int getIdFuncion() {
        return idFuncion;
    }

    /**
     * @return Sala asignada a la función.
     */
    public Sala getSala() {
        return sala;
    }

    /**
     * @return Película asignada a la función.
     */
    public Pelicula getPelicula() {
        return pelicula;
    }

    /**
     * @return Precio base de la función.
     */
    public double getPrecio() {
        return precio;
    }

    /**
     * @return Fecha y hora de inicio de la función.
     */
    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    /**
     * @return Formato de proyección de la película.
     */
    public String getFormatoPelicula() {
        return formatoPelicula;
    }

    /**
     * @return Idioma de proyección de la película.
     */
    public String getIdiomaPelicula() {
        return idiomaPelicula;
    }

    /**
     * Calcula la fecha y hora de finalización de la función sumando la duración
     * de la película a su horario de inicio.
     *
     * @return Fecha y hora de finalización de la función.
     */
    public LocalDateTime getFechaHoraFin() {
        return fechaHora.plusMinutes(pelicula.getDuracionPelicula());
    }

    /**
     * Indica si la función se encuentra activa.
     *
     * @return true si está activa, false si está desactivada.
     */
    @Override
    public boolean isActiva() {
        return !desactivada;
    }

    /**
     * Desactiva la función sin eliminarla del sistema.
     */
    @Override
    public void desactivar() {
        this.desactivada = true;
    }

    /**
     * Reactiva una función previamente desactivada.
     */
    @Override
    public void reactivar() {
        this.desactivada = false;
    }

    /**
     * Actualiza los datos de la función utilizando otra función cuyos valores
     * fueron previamente validados por Cine.
     *
     * Modifica la sala, película, fecha y hora, formato, idioma y precio,
     * conservando el identificador y el estado de activación.
     *
     * Permite mantener la identidad del objeto original y sus referencias.
     *
     * @param nuevosDatos Función que contiene los valores actualizados.
     */
    public void actualizarDatos(Funcion nuevosDatos) {
        this.sala = nuevosDatos.getSala();
        this.pelicula = nuevosDatos.getPelicula();
        this.fechaHora = nuevosDatos.getFechaHora();
        this.formatoPelicula = nuevosDatos.getFormatoPelicula();
        this.idiomaPelicula = nuevosDatos.getIdiomaPelicula();
        this.precio = nuevosDatos.getPrecio();
    }

}
