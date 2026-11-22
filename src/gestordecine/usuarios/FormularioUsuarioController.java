package gestordecine.usuarios;

import gestordecine.Cine;
import gestordecine.Cliente;
import gestordecine.PersistenciaCine;
import gestordecine.PrincipalController;
import gestordecine.Rol;
import gestordecine.sesion.SesionCliente;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import gestordecine.ServicioDeReservas;

public class FormularioUsuarioController {

    private Cine cine;
    private PersistenciaCine persistencia;
    private PrincipalController principalController;
    private int dniUsuario;
    private boolean modoEdicion;

    @FXML
    private Label tituloFormulario;

    @FXML
    private TextField campoDni;

    @FXML
    private TextField campoNombre;

    @FXML
    private TextField campoApellido;

    @FXML
    private TextField campoEmail;

    @FXML
    private PasswordField campoContrasenia;

    @FXML
    private ComboBox<Rol> comboRol;

    @FXML
    private Label mensajeError;

    /**
     * Configura el formulario con los datos del usuario seleccionado.
     *
     * Verifica que exista una sesión administrativa y carga los valores
     * actuales sin mostrar la contraseña.
     *
     * @param cine Cine que contiene los usuarios registrados.
     * @param persistencia Servicio de persistencia del cine.
     * @param principalController Controlador principal.
     * @param usuario Usuario que se desea editar.
     */
    public void configurar(Cine cine, PersistenciaCine persistencia,
            PrincipalController principalController, Cliente usuario) {

        if (!SesionCliente.esAdministrador()) {
            throw new IllegalStateException(
                    "No tiene permisos para editar usuarios"
            );
        }

        if (usuario == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un usuario"
            );
        }

        this.cine = cine;
        this.persistencia = persistencia;
        this.principalController = principalController;
        this.dniUsuario = usuario.getDni();
        this.modoEdicion = true;

        tituloFormulario.setText("Editar usuario");

        campoDni.setText(String.valueOf(usuario.getDni()));
        campoNombre.setText(usuario.getNombre());
        campoApellido.setText(usuario.getApellido());
        campoEmail.setText(usuario.getEmail());
        campoContrasenia.clear();

        comboRol.getItems().setAll(Rol.values());
        comboRol.setValue(usuario.getRol());
    }

    /**
     * Guarda un usuario nuevo o modifica uno existente.
     *
     * Trabaja sobre una copia del cine para evitar cambios parciales si falla
     * una validación o el guardado de los datos.
     *
     * Solo permite realizar la operación a un administrador.
     */
    @FXML
    private void guardarUsuario() {

        ocultarError();

        try {

            Cine cineTrabajo = cine.copiar();

            ServicioDeReservas servicio = cineTrabajo.getServicioDeReservas();

            Cliente administrador = servicio.buscarClientePorDni(
                    SesionCliente.getClienteActual().getDni()
            );

            if (administrador == null
                    || !administrador.esAdministrador()
                    || !administrador.isActivo()) {

                throw new IllegalStateException(
                        "No tiene permisos para administrar usuarios"
                );
            }

            if (modoEdicion) {

                // EDITAR USUARIO EXISTENTE
                servicio.editarUsuario(
                        dniUsuario,
                        campoNombre.getText(),
                        campoApellido.getText(),
                        campoEmail.getText(),
                        campoContrasenia.getText(),
                        comboRol.getValue(),
                        administrador
                );

            } else {

                // REGISTRAR NUEVO USUARIO
                int dni;

                try {
                    dni = Integer.parseInt(campoDni.getText().trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "El DNI debe ser un número válido"
                    );
                }

                if (comboRol.getValue() == null) {
                    throw new IllegalArgumentException(
                            "Debe seleccionar un rol"
                    );
                }

                Cliente nuevoUsuario = new Cliente(
                        dni,
                        campoNombre.getText(),
                        campoApellido.getText(),
                        campoEmail.getText(),
                        campoContrasenia.getText(),
                        comboRol.getValue()
                );

                servicio.registrarCliente(nuevoUsuario);
            }

            // GUARDAR CAMBIOS
            persistencia.guardar(cineTrabajo);
            principalController.actualizarCine(cineTrabajo);

            // ACTUALIZAR SESIÓN SI SE EDITÓ EL PROPIO USUARIO
            if (modoEdicion) {

                Cliente clienteActual = SesionCliente.getClienteActual();

                if (clienteActual != null
                        && clienteActual.getDni() == dniUsuario) {

                    Cliente usuarioActualizado
                            = cineTrabajo.getServicioDeReservas()
                                    .buscarClientePorDni(dniUsuario);

                    SesionCliente.iniciarSesion(usuarioActualizado);
                }
            }

            principalController.actualizarMenuCuenta();
            principalController.mostrarUsuarios();

        } catch (IOException | ClassNotFoundException e) {
            mostrarError("Error al guardar los cambios: " + e.getMessage());

        } catch (Exception e) {
            mostrarError(e.getMessage());
        }
    }

    /**
     * Regresa a la administración de usuarios sin guardar cambios.
     */
    @FXML
    private void volver() {
        principalController.mostrarUsuarios();
    }

    /**
     * Muestra un error de validación o persistencia.
     *
     * @param mensaje Descripción del error.
     */
    private void mostrarError(String mensaje) {
        mensajeError.setText(mensaje);
        mensajeError.setVisible(true);
        mensajeError.setManaged(true);
    }

    /**
     * Oculta el mensaje de error del formulario.
     */
    private void ocultarError() {
        mensajeError.setText("");
        mensajeError.setVisible(false);
        mensajeError.setManaged(false);
    }

    /**
     * Configura el formulario para registrar un nuevo usuario.
     *
     * Permite ingresar todos los datos y seleccionar el rol. Solo puede
     * utilizarlo un administrador autenticado.
     *
     * @param cine Cine que contiene los usuarios registrados.
     * @param persistencia Servicio de persistencia del cine.
     * @param principalController Controlador principal de CINEMA.
     */
    public void configurarNuevo(Cine cine, PersistenciaCine persistencia,
            PrincipalController principalController) {

        if (!SesionCliente.esAdministrador()) {
            throw new IllegalStateException(
                    "No tiene permisos para registrar usuarios"
            );
        }

        this.cine = cine;
        this.persistencia = persistencia;
        this.principalController = principalController;
        this.modoEdicion = false;

        tituloFormulario.setText("Nuevo usuario");

        campoDni.setEditable(true);
        campoDni.clear();
        campoNombre.clear();
        campoApellido.clear();
        campoEmail.clear();
        campoContrasenia.clear();

        campoContrasenia.setPromptText("Ingresá una contraseña");

        comboRol.getItems().setAll(Rol.values());
        comboRol.setValue(Rol.CLIENTE);
    }
}
