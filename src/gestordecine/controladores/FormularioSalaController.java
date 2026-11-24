package gestordecine.controladores;

import gestordecine.Cine;
import gestordecine.PersistenciaCine;
import gestordecine.Sala;
import gestordecine.controladores.PrincipalController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;


/**
 * Controlador del formulario de salas. Sirve para registrar una sala nueva o
 * para editar una existente, según se haya llamado o no a configurarEdicion.
 * Valida los datos y guarda los cambios en el archivo.
 */


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
    
    
    /**
     * Guarda los objetos que necesita el formulario para trabajar.
     *
     * @param cine Cine con las salas.
     * @param persistencia Objeto que guarda los datos en el archivo.
     * @param principalController Controlador de la ventana principal.
     */
    
    public void configurar(Cine cine, PersistenciaCine persistencia,
            PrincipalController principalController) {

        this.cine = cine;
        this.persistencia = persistencia;
        this.principalController = principalController;
    }

    /**
     * Cancela el formulario y vuelve a la lista de salas sin guardar nada.
     */    

    @FXML
    private void cancelar() {
        principalController.mostrarSalas();
    }
    
    
    /**
     * Registra una sala nueva o guarda los cambios de una existente.
     *
     * Trabaja sobre una copia del cine y recién cuando se guarda el archivo
     * reemplaza el cine activo, así no queda nada a medias si algo falla. Si
     * hay un error, lo muestra en pantalla.
     */    

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
    
    /**
     * Muestra un mensaje de error en el formulario.
     *
     * @param mensaje Texto del error que se quiere mostrar.
     */
    
    private void mostrarError(String mensaje) {

        mensajeError.setText(mensaje);
        mensajeError.setVisible(true);
        mensajeError.setManaged(true);
    }
    
    /**
     * Prepara el formulario para editar una sala: cambia los textos y carga
     * sus datos en los campos. Las filas y la distribución quedan
     * deshabilitadas porque no se pueden modificar; solo se puede cambiar el
     * nombre.
     *
     * @param sala Sala que se quiere editar.
     */    

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
