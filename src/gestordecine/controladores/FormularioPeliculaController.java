package gestordecine.controladores;

import gestordecine.Cine;
import gestordecine.Genero;
import gestordecine.Pelicula;
import gestordecine.PersistenciaCine;
import gestordecine.controladores.PrincipalController;
import java.io.*;
import java.nio.file.*;
import java.util.UUID;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.FlowPane;


/**
 * Controlador del formulario de películas. Sirve para registrar una película
 * nueva o para editar una existente, según se haya llamado o no a
 * configurarEdicion. Valida los datos, copia el póster a la carpeta posters y
 * guarda los cambios en el archivo.
 */

public class FormularioPeliculaController {

    private Cine cine;
    private PersistenciaCine persistencia;
    private PrincipalController principalController;
    private Pelicula peliculaEnEdicion;

    private File archivoPosterSeleccionado;

    @FXML
    private Label tituloFormulario;
    @FXML
    private Label mensajeError;
    @FXML
    private Label nombrePoster;

    @FXML
    private TextField campoTitulo;
    @FXML
    private TextField campoDuracion;
    @FXML
    private TextField campoAnio;
    @FXML
    private TextField campoDirector;
    @FXML
    private TextField campoActores;
    @FXML
    private TextField campoPuntuacion;
    @FXML
    private TextField campoClasificacion;

    @FXML
    private TextArea campoSinopsis;
    @FXML
    private Button botonGuardar;
    @FXML
    private ImageView vistaPoster;
    @FXML
    private FlowPane contenedorGeneros;

    private final Map<Genero, ToggleButton> botonesGenero = new HashMap<>();
    
    /**
     * Guarda los objetos que necesita el formulario para trabajar.
     *
     * @param cine Cine con las películas.
     * @param persistencia Objeto que guarda los datos en el archivo.
     * @param principalController Controlador de la ventana principal.
     */

    public void configurar(
            Cine cine,
            PersistenciaCine persistencia,
            PrincipalController principalController) {

        this.cine = cine;
        this.persistencia = persistencia;
        this.principalController = principalController;
    }
    
    /**
     * Prepara el formulario para editar una película: cambia los textos, carga
     * sus datos en los campos, muestra el póster y marca sus géneros. La
     * duración queda deshabilitada porque no se puede modificar.
     *
     * @param pelicula Película que se quiere editar.
     */
    
    public void configurarEdicion(Pelicula pelicula) {

        this.peliculaEnEdicion = pelicula;

        tituloFormulario.setText("Editar película");
        botonGuardar.setText("Guardar cambios");

        campoTitulo.setText(pelicula.getTituloPelicula());

        campoDuracion.setText(
                String.valueOf(pelicula.getDuracionPelicula())
        );
        campoDuracion.setDisable(true);

        campoAnio.setText(String.valueOf(pelicula.getAnioEstreno()));
        campoDirector.setText(pelicula.getDirector());
        campoActores.setText(pelicula.getActores());
        campoSinopsis.setText(pelicula.getSinopsis());

        campoPuntuacion.setText(
                String.valueOf(pelicula.getPuntuacion())
        );

        campoClasificacion.setText(pelicula.getClasificacion());

        if (pelicula.getPoster() != null
                && !pelicula.getPoster().isBlank()) {

            Path ruta = Paths.get("posters", pelicula.getPoster());

            if (Files.exists(ruta)) {
                vistaPoster.setImage(
                        new Image(ruta.toUri().toString())
                );
                nombrePoster.setText(pelicula.getPoster());
            }
        }
        // Limpiar los géneros seleccionados
        for (ToggleButton boton : botonesGenero.values()) {
            boton.setSelected(false);
        }

        // Marcar los géneros de la película
        for (Genero genero : pelicula.getGeneros()) {
            ToggleButton boton = botonesGenero.get(genero);

            if (boton != null) {
                boton.setSelected(true);
            }
        }
    }
    
    /**
     * Cancela el formulario y vuelve a la lista de películas sin guardar nada.
     */

    @FXML
    private void cancelar() {
        principalController.mostrarPeliculas();
    }
    
     /**
     * Abre el selector de archivos para elegir el póster (JPG o PNG). Si se
     * elige uno, guarda el archivo y lo muestra como vista previa.
     */

    @FXML
    private void seleccionarPoster() {

        FileChooser selector = new FileChooser();

        selector.setTitle("Seleccionar póster de película");

        selector.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Imágenes JPG y PNG",
                        "*.jpg", "*.jpeg", "*.png"
                )
        );

        File archivo = selector.showOpenDialog(
                vistaPoster.getScene().getWindow()
        );

        if (archivo != null) {
            archivoPosterSeleccionado = archivo;

            nombrePoster.setText(archivo.getName());

            vistaPoster.setImage(
                    new Image(archivo.toURI().toString())
            );
        }
    }
    
    /**
     * Registra una película nueva o guarda los cambios de una existente.
     *
     * Trabaja sobre una copia del cine y recién cuando se guarda el archivo
     * reemplaza el cine activo, así no queda nada a medias si algo falla. Si
     * hay un error, lo muestra en pantalla y borra el póster nuevo que se
     * hubiera copiado.
     */

    @FXML
    private void guardarPelicula() {

        String nuevoPoster = null;
        boolean guardadoCompleto = false;

        try {
            String titulo = campoTitulo.getText().trim();
            ArrayList<Genero> generos = obtenerGenerosSeleccionados();

            int anio = Integer.parseInt(
                    campoAnio.getText().trim()
            );

            double puntuacion = Double.parseDouble(
                    campoPuntuacion.getText().trim().replace(",", ".")
            );

            // Trabajamos con una copia para no modificar
            // el Cine original si falla la persistencia.
            Cine cineTrabajo = cine.copiar();

            if (peliculaEnEdicion != null) {

                // EDITAR PELÍCULA
                int id = peliculaEnEdicion.getIdPelicula();

                Pelicula pelicula = cineTrabajo.buscarPeliculaPorId(id);

                if (pelicula == null) {
                    throw new IllegalArgumentException(
                            "La película no existe."
                    );
                }

                // Validamos y editamos primero sobre la copia.
                cineTrabajo.editarPelicula(
                        id,
                        titulo,
                        anio,
                        generos,
                        campoDirector.getText(),
                        campoActores.getText(),
                        campoSinopsis.getText(),
                        puntuacion,
                        campoClasificacion.getText(),
                        pelicula.getPoster()
                );

                // Copiamos el póster solamente si cambió.
                if (archivoPosterSeleccionado != null) {
                    nuevoPoster = guardarPoster(id);
                    pelicula.setPoster(nuevoPoster);
                }

            } else {

                // REGISTRAR PELÍCULA
                int id = cineTrabajo.obtenerSiguienteIdPelicula();

                int duracion = Integer.parseInt(
                        campoDuracion.getText().trim()
                );

                if (archivoPosterSeleccionado == null) {
                    throw new IllegalArgumentException(
                            "Debés seleccionar un póster."
                    );
                }

                // Primero validamos todos los datos.
                // El nombre temporal es solo para validar.
                Pelicula pelicula = new Pelicula(
                        id,
                        titulo,
                        duracion,
                        anio,
                        generos,
                        campoDirector.getText(),
                        campoActores.getText(),
                        campoSinopsis.getText(),
                        puntuacion,
                        campoClasificacion.getText(),
                        "poster_pendiente.jpg"
                );

                // Ahora copiamos el archivo real.
                nuevoPoster = guardarPoster(id);

                pelicula.setPoster(nuevoPoster);

                cineTrabajo.registrarPelicula(pelicula);
            }

            // Persistimos antes de reemplazar el Cine activo.
            persistencia.guardar(cineTrabajo);

            guardadoCompleto = true;

            principalController.actualizarCine(cineTrabajo);

            principalController.mostrarPeliculas();

        } catch (NumberFormatException e) {

            mensajeError.setText(
                    "Revisá duración, año y puntuación."
            );

        } catch (IllegalArgumentException e) {

            mensajeError.setText(e.getMessage());

        } catch (Exception e) {

            mensajeError.setText(
                    "No se pudieron guardar los cambios."
            );

            e.printStackTrace();

        } finally {

            // Si no se pudo guardar, eliminamos solamente
            // la imagen nueva. La anterior queda intacta.
            if (!guardadoCompleto && nuevoPoster != null) {
                try {
                    Files.deleteIfExists(
                            Paths.get("posters", nuevoPoster)
                    );
                } catch (IOException e) {
                    System.err.println(
                            "No se pudo eliminar el póster temporal: "
                            + e.getMessage()
                    );
                }
            }
        }
    }
    
    /**
     * Copia el póster elegido a la carpeta posters con un nombre único (id de
     * la película más un código aleatorio), para que no se pisen entre sí.
     *
     * @param idPelicula Id de la película a la que pertenece el póster.
     * @return El nombre del archivo copiado.
     * @throws IOException Si no se puede crear la carpeta o copiar el archivo.
     * @throws IllegalArgumentException Si no se eligió ningún póster.
     */

    private String guardarPoster(int idPelicula) throws IOException {

        if (archivoPosterSeleccionado == null) {
            throw new IllegalArgumentException(
                    "Debés seleccionar un póster."
            );
        }

        Path carpetaPosters = Paths.get("posters");

        Files.createDirectories(carpetaPosters);

        String nombreOriginal
                = archivoPosterSeleccionado.getName().toLowerCase();

        String extension = nombreOriginal.endsWith(".png")
                ? ".png" : ".jpg";

        String nombreArchivo = "pelicula_" + idPelicula
                + "_" + UUID.randomUUID() + extension;

        Path destino = carpetaPosters.resolve(nombreArchivo);

        try {
            Files.copy(archivoPosterSeleccionado.toPath(), destino);
        } catch (IOException e) {
            Files.deleteIfExists(destino);
            throw e;
        }

        return nombreArchivo;
    }
    
    /**
     * Crea un botón para cada género y los agrega al formulario. Los guarda en
     * un mapa para poder marcarlos y leerlos después.
     */
    
    private void cargarGeneros() {
        contenedorGeneros.getChildren().clear();
        botonesGenero.clear();

        for (Genero genero : Genero.values()) {
            ToggleButton boton = new ToggleButton(genero.toString());
            boton.getStyleClass().add("chip-genero");

            botonesGenero.put(genero, boton);
            contenedorGeneros.getChildren().add(boton);
        }
    }
    
    /**
     * Se ejecuta sola cuando JavaFX termina de cargar el formulario. Carga los
     * botones de géneros.
     */
    @FXML
    private void initialize() {
        cargarGeneros();
    }
    
    /**
     * Devuelve los géneros que están marcados en el formulario.
     *
     * @return Lista con los géneros seleccionados (puede estar vacía).
     */
    private ArrayList<Genero> obtenerGenerosSeleccionados() {

        ArrayList<Genero> seleccionados = new ArrayList<>();

        for (Genero genero : Genero.values()) {
            ToggleButton boton = botonesGenero.get(genero);

            if (boton != null && boton.isSelected()) {
                seleccionados.add(genero);
            }
        }

        return seleccionados;
    }
}
