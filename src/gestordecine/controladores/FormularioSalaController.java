package gestordecine.controladores;

import gestordecine.Cine;
import gestordecine.PersistenciaCine;
import gestordecine.Sala;
import gestordecine.controladores.PrincipalController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class FormularioSalaController {

    private Cine cine;
    private PersistenciaCine persistencia;
    private PrincipalController principalController;
    private Sala salaEnEdicion;

    @FXML
    private TextField campoNombre;
    @FXML
    private TextField campoFilas;
    @FXML
    private TextField campoDistribucion;
    @FXML
    private Label mensajeError;

    @FXML
    private Label tituloFormulario;
    @FXML
    private Button botonGuardar;

    public void configurar(Cine cine, PersistenciaCine persistencia,
            PrincipalController principalController) {

        this.cine = cine;
        this.persistencia = persistencia;
        this.principalController = principalController;
    }

    @FXML
    private void cancelar() {
        principalController.mostrarSalas();
    }

    @FXML
    private void guardarSala() {

        try {
            String nombre = campoNombre.getText().trim();

            // Trabajar sobre una copia del cine
            Cine cineTrabajo = cine.copiar();

            if (salaEnEdicion != null) {

                // EDITAR SALA EXISTENTE
                cineTrabajo.editarSala(
                        salaEnEdicion.getIdSala(),
                        nombre
                );

            } else {

                // REGISTRAR NUEVA SALA
                int filas = Integer.parseInt(
                        campoFilas.getText().trim()
                );

                int id = cineTrabajo.obtenerSiguienteIdSala();

                Sala sala = new Sala(
                        id,
                        nombre,
                        filas,
                        campoDistribucion.getText().trim()
                );

                cineTrabajo.registrarSala(sala);
            }

            // Guardar antes de actualizar el cine activo
            persistencia.guardar(cineTrabajo);

            principalController.actualizarCine(cineTrabajo);
            principalController.mostrarSalas();

        } catch (NumberFormatException e) {
            mostrarError("La cantidad de filas debe ser un número entero.");

        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());

        } catch (Exception e) {
            mostrarError("No se pudieron guardar los cambios.");
            e.printStackTrace();
        }
    }

    private void mostrarError(String mensaje) {

        mensajeError.setText(mensaje);
        mensajeError.setVisible(true);
        mensajeError.setManaged(true);
    }

    public void configurarEdicion(Sala sala) {

        this.salaEnEdicion = sala;

        campoNombre.setText(sala.getNombreSala());

        campoFilas.setText(
                String.valueOf(sala.getCantidadFilas())
        );

        campoDistribucion.setText(
                sala.getDistribucionButacas()
        );

        campoFilas.setDisable(true);
        campoDistribucion.setDisable(true);
        
        tituloFormulario.setText("Editar sala");
        botonGuardar.setText("Guardar cambios");
    }
}
