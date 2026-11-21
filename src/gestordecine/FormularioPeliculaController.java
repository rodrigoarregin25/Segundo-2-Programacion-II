package gestordecine;

import java.io.*;
import java.nio.file.*;
import java.util.UUID;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

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
    private TextField campoGenero;
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

    public void configurar(
            Cine cine,
            PersistenciaCine persistencia,
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

        campoDuracion.setText(
                String.valueOf(pelicula.getDuracionPelicula())
        );
        campoDuracion.setDisable(true);

        campoAnio.setText(String.valueOf(pelicula.getAnioEstreno()));
        campoGenero.setText(pelicula.getGenero());
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
    }

    @FXML
    private void cancelar() {
        principalController.mostrarPeliculas();
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

            vistaPoster.setImage(
                    new Image(archivo.toURI().toString())
            );
        }
    }

    @FXML
    private void guardarPelicula() {

        String nuevoPoster = null;
        boolean guardadoCompleto = false;

        try {
            String titulo = campoTitulo.getText().trim();

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
                        campoGenero.getText(),
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
                        campoGenero.getText(),
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

    private Cine copiarCine(Cine original)
            throws IOException, ClassNotFoundException {

        // Serializamos el Cine en memoria.
        ByteArrayOutputStream memoria = new ByteArrayOutputStream();

        try (ObjectOutputStream salida
                = new ObjectOutputStream(memoria)) {

            salida.writeObject(original);
        }

        // Lo recuperamos como una instancia independient
        try (ObjectInputStream entrada = new ObjectInputStream(
                new ByteArrayInputStream(memoria.toByteArray()))) {

            Cine copia = (Cine) entrada.readObject();

            copia.getServicioDeReservas().recargarCodigos();

            return copia;
        }
    }
}
