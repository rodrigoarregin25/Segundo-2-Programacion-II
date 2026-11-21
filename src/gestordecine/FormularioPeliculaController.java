package gestordecine;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

public class FormularioPeliculaController {

    private Cine cine;
    private PersistenciaCine persistencia;
    private PrincipalController principalController;
    private Pelicula peliculaEnEdicion;

    @FXML
    private Label tituloFormulario;

    @FXML
    private TextField campoTitulo;

    @FXML
    private TextField campoDuracion;

    @FXML
    private Label mensajeError;

    @FXML
    private void cancelar() {
        principalController.mostrarPeliculas();
    }

    @FXML
    private Button botonGuardar;

    @FXML
    private TextField campoAnio;
    @FXML
    private TextField campoGenero;
    @FXML
    private TextField campoDirector;
    @FXML
    private TextField campoActores;
    @FXML
    private TextArea campoSinopsis;
    @FXML
    private TextField campoPuntuacion;
    @FXML
    private TextField campoClasificacion;
    @FXML
    private TextField campoPoster;
    @FXML
    private ImageView vistaPoster;

    @FXML
    private Label nombrePoster;

    private File archivoPosterSeleccionado;

    @FXML
    private void guardarPelicula() {
        try {
            String titulo = campoTitulo.getText().trim();

            if (peliculaEnEdicion != null) {

                int anio = Integer.parseInt(campoAnio.getText().trim());

                double puntuacion = Double.parseDouble(
                        campoPuntuacion.getText().trim().replace(",", ".")
                );

                String poster = peliculaEnEdicion.getPoster();

                // Si seleccionamos una imagen nueva, reemplazamos el póster
                if (archivoPosterSeleccionado != null) {
                    poster = guardarPoster(peliculaEnEdicion.getIdPelicula());
                }

                cine.editarPelicula(
                        peliculaEnEdicion.getIdPelicula(),
                        titulo,
                        anio,
                        campoGenero.getText(),
                        campoDirector.getText(),
                        campoActores.getText(),
                        campoSinopsis.getText(),
                        puntuacion,
                        campoClasificacion.getText(),
                        poster
                );

            } else {

                // REGISTRAR NUEVA PELÍCULA
                int id = cine.obtenerSiguienteIdPelicula();

                int duracion = Integer.parseInt(
                        campoDuracion.getText().trim()
                );

                int anio = Integer.parseInt(
                        campoAnio.getText().trim()
                );

                double puntuacion = Double.parseDouble(
                        campoPuntuacion.getText().trim().replace(",", ".")
                );

                // VALIDAR QUE SE HAYA SELECCIONADO UN PÓSTER
                if (archivoPosterSeleccionado == null) {
                    throw new IllegalArgumentException(
                            "Debés seleccionar un póster para la película."
                    );
                }

                String extension = archivoPosterSeleccionado.getName()
                        .toLowerCase().endsWith(".png") ? ".png" : ".jpg";

                String nombreArchivoPoster = "pelicula_" + id + extension;

                // CREAR PELÍCULA
                Pelicula pelicula = new Pelicula(
                        id,
                        titulo,
                        duracion,
                        anio,
                        campoGenero.getText(),
                        campoDirector.getText(),
                        campoActores.getText(),
                        campoSinopsis.getText(),
                        puntuacion,
                        campoClasificacion.getText(),
                        nombreArchivoPoster
                );

                // COPIAR PÓSTER A LA CARPETA DEL PROYECTO
                guardarPoster(id);

                // REGISTRAR EN EL CINE
                cine.registrarPelicula(pelicula);
            }

            // GUARDAR PERSISTENCIA
            persistencia.guardar(cine);

            // VOLVER AL LISTADO
            principalController.mostrarPeliculas();

        } catch (NumberFormatException e) {
            mensajeError.setText(
                    "Revisá los valores numéricos de duración, año y puntuación."
            );

        } catch (IllegalArgumentException e) {
            mensajeError.setText(e.getMessage());

        } catch (Exception e) {
            mensajeError.setText("No se pudieron guardar los cambios.");
            e.printStackTrace();
        }
    }

    public void configurar(Cine cine, PersistenciaCine persistencia,
            PrincipalController principalController) {
        this.cine = cine;
        this.persistencia = persistencia;
        this.principalController = principalController;
    }

    public void configurarEdicion(Pelicula pelicula) {

        this.peliculaEnEdicion = pelicula;

        tituloFormulario.setText("Editar película");
        botonGuardar.setText("Guardar cambios");

        campoTitulo.setText(pelicula.getTituloPelicula());
        campoDuracion.setText(String.valueOf(pelicula.getDuracionPelicula()));
        campoDuracion.setDisable(true);

        campoAnio.setText(String.valueOf(pelicula.getAnioEstreno()));
        campoGenero.setText(pelicula.getGenero());
        campoDirector.setText(pelicula.getDirector());
        campoActores.setText(pelicula.getActores());
        campoSinopsis.setText(pelicula.getSinopsis());
        campoPuntuacion.setText(String.valueOf(pelicula.getPuntuacion()));
        campoClasificacion.setText(pelicula.getClasificacion());

        // Mostrar el póster actual
        if (pelicula.getPoster() != null && !pelicula.getPoster().isBlank()) {

            Path ruta = Paths.get("posters", pelicula.getPoster());

            if (Files.exists(ruta)) {
                vistaPoster.setImage(new Image(ruta.toUri().toString()));
                nombrePoster.setText(pelicula.getPoster());
            }
        }
    }

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

            Image imagen = new Image(archivo.toURI().toString());

            vistaPoster.setImage(imagen);
        }
    }

    private String guardarPoster(int idPelicula) throws IOException {

        if (archivoPosterSeleccionado == null) {
            throw new IllegalArgumentException(
                    "Debés seleccionar un póster para la película."
            );
        }

        Path carpetaPosters = Paths.get("posters");

        Files.createDirectories(carpetaPosters);

        String extension = archivoPosterSeleccionado.getName()
                .toLowerCase()
                .endsWith(".png") ? ".png" : ".jpg";

        String nombreArchivo = "pelicula_" + idPelicula + extension;

        Path destino = carpetaPosters.resolve(nombreArchivo);

        Files.copy(
                archivoPosterSeleccionado.toPath(),
                destino,
                StandardCopyOption.REPLACE_EXISTING
        );

        return nombreArchivo;
    }
}
