package gestordecine.controladores;

import gestordecine.Cine;
import gestordecine.Genero;
import gestordecine.Pelicula;
import gestordecine.PersistenciaCine;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;

/**
 * Controlador del formulario de registro y edición de películas.
 *
 * Permite ingresar y modificar la información de una película, seleccionar sus
 * géneros y cargar una imagen como póster.
 *
 * Utiliza una copia del estado del cine para validar y realizar modificaciones
 * antes de persistirlas, evitando alterar el estado original si ocurre un error
 * durante el guardado.
 *
 * Se comunica con PrincipalController para actualizar el cine y gestionar la
 * navegación entre pantallas.
 *
 */
public class FormularioPeliculaController {

    // Datos del sistema y estado del formulario.
    private Cine cine;
    private PersistenciaCine persistencia;
    private PrincipalController principalController;
    private Pelicula peliculaEnEdicion;
    private File archivoPosterSeleccionado;

    // Relaciona cada género con su botón de selección.
    private final Map<Genero, ToggleButton> botonesGenero = new HashMap<>();

    // Componentes de la interfaz.
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

    /**
     * Inicializa el formulario después de cargar los elementos FXML.
     *
     * Genera los botones correspondientes a los géneros disponibles. JavaFX
     * ejecuta este método automáticamente al cargar la vista.
     */
    @FXML
    private void initialize() {
        cargarGeneros();
    }

    /**
     * Configura el formulario con las referencias necesarias para administrar
     * las películas.
     *
     * Recibe el cine actual, el objeto encargado de la persistencia y el
     * controlador principal utilizado para la navegación.
     *
     * @param cine Cine que contiene las películas registradas.
     * @param persistencia Objeto encargado de guardar el estado del cine.
     * @param principalController Controlador principal de la aplicación.
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
     * Configura el formulario para modificar una película existente.
     *
     * Carga los datos de la película en los campos correspondientes, muestra su
     * póster si el archivo existe y selecciona sus géneros.
     *
     * Impide modificar la duración de la película, conservando el valor
     * establecido al momento de su registro.
     *
     * @param pelicula Película que se desea editar.
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

        // Mostrar el póster actual si existe.
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

        // Limpiar los géneros seleccionados.
        for (ToggleButton boton : botonesGenero.values()) {
            boton.setSelected(false);
        }

        // Seleccionar los géneros de la película.
        for (Genero genero : pelicula.getGeneros()) {

            ToggleButton boton = botonesGenero.get(genero);

            if (boton != null) {
                boton.setSelected(true);
            }
        }
    }

    /**
     * Cancela el registro o la edición de una película.
     *
     * Regresa al listado de películas sin guardar los cambios ingresados en el
     * formulario.
     */
    @FXML
    private void cancelar() {
        principalController.mostrarPeliculas();
    }

    /**
     * Abre un selector de archivos para elegir el póster de la película.
     *
     * Permite seleccionar imágenes JPG, JPEG o PNG. Si el usuario selecciona un
     * archivo, guarda su referencia, muestra su nombre y actualiza la vista
     * previa.
     *
     * Si se cancela la selección, conserva el estado anterior.
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
     * Registra una nueva película o modifica una existente.
     *
     * Obtiene los datos ingresados en el formulario y trabaja sobre una copia
     * del cine para evitar modificar directamente el estado original antes de
     * guardar.
     *
     * En una edición, conserva el identificador y la duración de la película,
     * actualizando los demás atributos.
     *
     * En un registro, genera el siguiente identificador disponible, valida los
     * datos y registra una nueva película.
     *
     * Si se selecciona un póster nuevo, lo copia a la carpeta correspondiente.
     * Si ocurre un error antes de completar el guardado, intenta eliminar
     * únicamente el nuevo archivo.
     *
     * Una vez persistidos los cambios, actualiza el cine activo y regresa al
     * listado de películas.
     *
     * Los errores de validación o persistencia se informan mediante el mensaje
     * de error del formulario.
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

            // Trabajar con una copia para no modificar
            // el cine original si falla la persistencia.
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

                // Validar y editar primero sobre la copia.
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

                // Copiar el póster solamente si cambió.
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

                // Validar los datos utilizando un nombre
                // temporal para el archivo del póster.
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

                // Copiar el archivo real.
                nuevoPoster = guardarPoster(id);

                pelicula.setPoster(nuevoPoster);

                cineTrabajo.registrarPelicula(pelicula);
            }

            // Persistir antes de reemplazar el cine activo.
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

            // Si no se pudo guardar, eliminar solamente
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
     * Genera los botones para seleccionar los géneros de una película.
     *
     * Recorre los valores de la enumeración Genero y crea un ToggleButton para
     * cada uno.
     *
     * Almacena la relación entre cada género y su botón en un mapa para poder
     * consultar posteriormente cuáles fueron seleccionados.
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
     * Obtiene los géneros seleccionados en el formulario.
     *
     * Recorre los valores de la enumeración Genero y comprueba el estado de los
     * botones asociados.
     *
     * @return Una lista con los géneros seleccionados, o una lista vacía si no
     * se seleccionó ninguno.
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

    /**
     * Copia el póster seleccionado a la carpeta de imágenes del proyecto.
     *
     * Si la carpeta no existe, la crea automáticamente. Genera un nombre de
     * archivo utilizando el identificador de la película y un UUID para evitar
     * nombres repetidos.
     *
     * Conserva la extensión PNG cuando corresponde; para los demás formatos
     * admitidos utiliza JPG.
     *
     * Si falla la copia, intenta eliminar el archivo de destino antes de
     * propagar la excepción.
     *
     * @param idPelicula Identificador de la película asociada al póster.
     * @return Nombre del archivo guardado, sin incluir la ruta completa.
     *
     * @throws IllegalArgumentException Si no se seleccionó un póster.
     * @throws IOException Si ocurre un error al crear la carpeta o copiar el
     * archivo.
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
}