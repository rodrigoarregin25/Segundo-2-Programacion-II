package gestordecine;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

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
    private void guardarPelicula() {
        try {
            String titulo = campoTitulo.getText().trim();

            if (peliculaEnEdicion != null) {

                cine.editarPelicula(
                        peliculaEnEdicion.getIdPelicula(),
                        titulo
                );

            } else {

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
                        campoPoster.getText()
                );

                cine.registrarPelicula(pelicula);

                cine.registrarPelicula(pelicula);
            }

            persistencia.guardar(cine);

            principalController.mostrarPeliculas();

        } catch (NumberFormatException e) {
            mensajeError.setText("Revisá los valores numéricos de duración, año y puntuación.");

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

        campoTitulo.setText(pelicula.getTituloPelicula());

        campoDuracion.setText(
                String.valueOf(pelicula.getDuracionPelicula())
        );

        campoDuracion.setDisable(true);

        botonGuardar.setText("Guardar cambios");
    }

}
