
package gestordecine.sesion;

import gestordecine.Cine;
import gestordecine.Cliente;
import gestordecine.PersistenciaCine;
import gestordecine.PrincipalController;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controlador del formulario de acceso de CINEMA.
 *
 * Permite iniciar sesión o registrar clientes desde
 * cualquier pantalla, utilizando una sesión centralizada.
 *
 * @author Rodrigo Arregin
 */
public class AccesoController {

    private Cine cine;
    private PersistenciaCine persistencia;
    private PrincipalController principalController;

    private boolean registrando = false;

    @FXML
    private Label tituloAcceso;

    @FXML
    private Label mensajeError;

    @FXML
    private TextField campoDni;

    @FXML
    private PasswordField campoContrasenia;

    @FXML
    private TextField campoNombre;

    @FXML
    private TextField campoApellido;

    @FXML
    private TextField campoEmail;

    @FXML
    private VBox camposRegistro;

    @FXML
    private Button botonIngresar;

    @FXML
    private Button botonAlternar;

    /**
     * Recibe las dependencias necesarias para validar y guardar clientes.
     *
     * @param cine Cine con los clientes registrados.
     * @param persistencia Administrador del archivo de datos.
     * @param principalController Controlador de la ventana principal.
     */
    public void configurar(Cine cine, PersistenciaCine persistencia,
                           PrincipalController principalController) {

        this.cine = cine;
        this.persistencia = persistencia;
        this.principalController = principalController;
    }

    /**
     * Alterna el formulario entre inicio de sesión y registro.
     */
    @FXML
    private void alternarModo() {

        registrando = !registrando;

        camposRegistro.setVisible(registrando);
        camposRegistro.setManaged(registrando);

        tituloAcceso.setText(
                registrando ? "Crear cuenta" : "Iniciar sesión"
        );

        botonIngresar.setText(
                registrando ? "Crear cuenta" : "Ingresar"
        );

        botonAlternar.setText(
                registrando
                ? "Ya tengo cuenta, ingresar"
                : "No tengo cuenta, registrarme"
        );

        ocultarError();

        // Ajustar el tamaño de la ventana según el formulario visible.
        Stage ventana = (Stage) botonIngresar.getScene().getWindow();
        ventana.sizeToScene();
    }

    /**
     * Valida las credenciales o registra un nuevo cliente.
     *
     * Si el ingreso es correcto, inicia la sesión centralizada
     * y actualiza el menú principal de CINEMA.
     */
    @FXML
    private void ingresar() {

        ocultarError();

        try {

            int dni;

            try {
                dni = Integer.parseInt(campoDni.getText().trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "El DNI debe ser un número válido"
                );
            }

            Cliente cliente;

            if (registrando) {

                validarDatosRegistro();

                // Trabajar sobre una copia para proteger los datos originales.
                Cine cineTrabajo = cine.copiar();

                cliente = new Cliente(
                        dni,
                        campoNombre.getText().trim(),
                        campoApellido.getText().trim(),
                        campoEmail.getText().trim(),
                        campoContrasenia.getText()
                );

                cineTrabajo.getServicioDeReservas()
                        .registrarCliente(cliente);

                // Guardar antes de actualizar el cine principal.
                persistencia.guardar(cineTrabajo);
                principalController.actualizarCine(cineTrabajo);

            } else {

                cliente = cine.getServicioDeReservas()
                        .buscarClientePorDni(dni);

                if (cliente == null) {
                    throw new IllegalArgumentException(
                            "No existe un cliente con ese DNI"
                    );
                }

                if (!cliente.getContrasenia().equals(
                        campoContrasenia.getText())) {

                    throw new IllegalArgumentException(
                            "La contraseña es incorrecta"
                    );
                }
            }

            // Compartir la sesión con toda la aplicación.
            SesionCliente.iniciarSesion(cliente);

            principalController.actualizarMenuCuenta();

            // Cerrar el formulario después del ingreso exitoso.
            Stage ventana = (Stage) botonIngresar.getScene().getWindow();
            ventana.close();

        } catch (IOException | ClassNotFoundException e) {

            mostrarError(
                    "No se pudieron guardar los datos: " + e.getMessage()
            );

        } catch (IllegalArgumentException | IllegalStateException e) {

            mostrarError(e.getMessage());
        }
    }

    /**
     * Valida los campos adicionales requeridos para registrar un cliente.
     *
     * @throws IllegalArgumentException Si algún campo es inválido.
     */
    private void validarDatosRegistro() {

        if (campoNombre.getText().isBlank()
                || campoApellido.getText().isBlank()) {

            throw new IllegalArgumentException(
                    "Completá el nombre y el apellido"
            );
        }

        String email = campoEmail.getText().trim();

        if (!email.contains("@")
                || !email.contains(".")
                || email.indexOf('@') > email.lastIndexOf('.')) {

            throw new IllegalArgumentException(
                    "El correo electrónico no es válido"
            );
        }

        if (campoContrasenia.getText().length() < 4) {
            throw new IllegalArgumentException(
                    "La contraseña debe tener al menos 4 caracteres"
            );
        }
    }

    /**
     * Muestra un mensaje de error dentro del formulario.
     *
     * @param mensaje Descripción del error.
     */
    private void mostrarError(String mensaje) {
        mensajeError.setText(mensaje);
        mensajeError.setVisible(true);
        mensajeError.setManaged(true);
    }

    /**
     * Oculta el mensaje de error anterior.
     */
    private void ocultarError() {
        mensajeError.setVisible(false);
        mensajeError.setManaged(false);
    }
}
