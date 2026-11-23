package gestordecine.usuarios;

import gestordecine.Cine;
import gestordecine.Cliente;
import gestordecine.PersistenciaCine;
import gestordecine.Rol;
import gestordecine.ServicioDeReservas;
import gestordecine.controladores.PrincipalController;
import gestordecine.sesion.SesionCliente;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controlador del formulario de administración de usuarios.
 *
 * Permite registrar nuevos usuarios y modificar los datos de usuarios
 * existentes, incluyendo DNI, nombre, apellido, correo electrónico,
 * contraseña y rol.
 *
 * Verifica que exista una sesión administrativa antes de permitir el acceso a
 * las operaciones de registro y edición.
 *
 * Trabaja sobre una copia del cine para evitar modificaciones parciales si
 * ocurre un error de validación o persistencia.
 *
 * Utiliza ServicioDeReservas para gestionar los usuarios registrados y
 * PrincipalController para actualizar el cine y navegar entre pantallas.
 */
public class FormularioUsuarioController {

    // Referencias al sistema.
    private Cine cine;
    private PersistenciaCine persistencia;
    private PrincipalController principalController;

    // Identifica al usuario que se está editando.
    private int dniUsuario;

    // Indica si el formulario se utiliza para registrar o editar.
    private boolean modoEdicion;

    // Componentes de la interfaz.
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
     * Configura el formulario para registrar un nuevo usuario.
     *
     * Comprueba que exista una sesión administrativa y asigna las referencias
     * del cine, la persistencia y el controlador principal.
     *
     * Establece el formulario en modo registro, limpia los campos y habilita el
     * ingreso del DNI.
     *
     * Carga los roles disponibles y selecciona CLIENTE como valor
     * predeterminado.
     *
     * @param cine Cine que contiene los usuarios registrados.
     * @param persistencia Objeto encargado de guardar los cambios.
     * @param principalController Controlador principal de CINEMA.
     *
     * @throws IllegalStateException Si no existe una sesión administrativa.
     */
    public void configurarNuevo(
            Cine cine,
            PersistenciaCine persistencia,
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

        campoContrasenia.setPromptText(
                "Ingresá una contraseña"
        );

        comboRol.getItems().setAll(Rol.values());
        comboRol.setValue(Rol.CLIENTE);
    }

    /**
     * Configura el formulario para editar un usuario existente.
     *
     * Verifica que exista una sesión administrativa y que el usuario recibido
     * no sea nulo.
     *
     * Asigna las referencias del sistema, conserva el DNI del usuario como
     * identificador de edición y carga sus datos actuales.
     *
     * La contraseña no se muestra por motivos de seguridad. El campo
     * correspondiente permanece vacío.
     *
     * También carga los roles disponibles y selecciona el rol actual del
     * usuario.
     *
     * @param cine Cine que contiene los usuarios registrados.
     * @param persistencia Objeto encargado de guardar los cambios.
     * @param principalController Controlador principal de CINEMA.
     * @param usuario Usuario que se desea editar.
     *
     * @throws IllegalStateException Si no existe una sesión administrativa.
     * @throws IllegalArgumentException Si el usuario recibido es nulo.
     */
    public void configurar(
            Cine cine,
            PersistenciaCine persistencia,
            PrincipalController principalController,
            Cliente usuario) {

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

        campoDni.setEditable(true);
        campoDni.setText(
                String.valueOf(usuario.getDni())
        );

        campoNombre.setText(usuario.getNombre());
        campoApellido.setText(usuario.getApellido());
        campoEmail.setText(usuario.getEmail());
        campoContrasenia.clear();

        comboRol.getItems().setAll(Rol.values());
        comboRol.setValue(usuario.getRol());
    }

    /**
     * Registra un nuevo usuario o modifica uno existente.
     *
     * Oculta los errores anteriores y crea una copia del cine para realizar las
     * modificaciones sin alterar directamente el estado actual del sistema.
     *
     * Comprueba que el usuario autenticado exista dentro de la copia, tenga rol
     * de administrador y se encuentre activo.
     *
     * En modo edición, utiliza ServicioDeReservas para corregir los datos del
     * usuario identificado por su DNI original, sin perder reservas.
     *
     * En modo registro, valida el formato del DNI, comprueba que se haya
     * seleccionado un rol y crea un nuevo Cliente.
     *
     * Una vez completadas las validaciones, guarda el cine, actualiza su
     * referencia en PrincipalController y, si corresponde, actualiza la sesión
     * del usuario editado.
     *
     * Finalmente, actualiza el menú de cuenta y regresa a la pantalla de
     * administración de usuarios.
     *
     * Si ocurre un error, informa el motivo dentro del formulario.
     */
    @FXML
    private void guardarUsuario() {

        ocultarError();

        try {

            // Leer el DNI tanto para altas como para correcciones.
            int nuevoDni;
            try {
                nuevoDni = Integer.parseInt(campoDni.getText().trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "El DNI debe ser un número válido"
                );
            }

            // Trabajar sobre una copia del cine.
            Cine cineTrabajo = cine.copiar();

            ServicioDeReservas servicio
                    = cineTrabajo.getServicioDeReservas();

            // Buscar al administrador dentro de la copia.
            Cliente administrador = servicio.buscarClientePorDni(
                    SesionCliente.getClienteActual().getDni()
            );

            // Verificar los permisos de administración.
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
                        nuevoDni,
                        campoNombre.getText(),
                        campoApellido.getText(),
                        campoEmail.getText(),
                        campoContrasenia.getText(),
                        comboRol.getValue(),
                        administrador
                );

            } else {

                // REGISTRAR NUEVO USUARIO
                if (comboRol.getValue() == null) {

                    throw new IllegalArgumentException(
                            "Debe seleccionar un rol"
                    );
                }

                Cliente nuevoUsuario = new Cliente(
                        nuevoDni,
                        campoNombre.getText(),
                        campoApellido.getText(),
                        campoEmail.getText(),
                        campoContrasenia.getText(),
                        comboRol.getValue()
                );

                servicio.registrarCliente(nuevoUsuario);
            }

            // Guardar los cambios antes de modificar el estado activo.
            persistencia.guardar(cineTrabajo);

            // Si el administrador corrigió su propio DNI, actualizar la
            // sesión ANTES de actualizarCine(), que la busca por DNI.
            if (modoEdicion) {
                Cliente clienteActual = SesionCliente.getClienteActual();
                if (clienteActual != null
                        && clienteActual.getDni() == dniUsuario) {
                    Cliente usuarioActualizado
                            = servicio.buscarClientePorDni(nuevoDni);
                    SesionCliente.iniciarSesion(usuarioActualizado);
                }
            }

            // Actualizar el cine activo con una sesión ya sincronizada.
            principalController.actualizarCine(cineTrabajo);

            // Actualizar el menú y volver al listado.
            principalController.actualizarMenuCuenta();
            principalController.mostrarUsuarios();

        } catch (IOException | ClassNotFoundException e) {

            mostrarError(
                    "Error al guardar los cambios: " + e.getMessage()
            );

        } catch (Exception e) {

            mostrarError(e.getMessage());
        }
    }

    /**
     * Regresa a la administración de usuarios sin guardar cambios.
     *
     * Utiliza PrincipalController para volver al listado.
     */
    @FXML
    private void volver() {

        principalController.mostrarUsuarios();
    }

    /**
     * Muestra un mensaje de error dentro del formulario.
     *
     * Asigna el texto recibido a la etiqueta correspondiente y habilita su
     * visualización en la interfaz.
     *
     * @param mensaje Descripción del error que se desea mostrar.
     */
    private void mostrarError(String mensaje) {

        mensajeError.setText(mensaje);
        mensajeError.setVisible(true);
        mensajeError.setManaged(true);
    }

    /**
     * Oculta el mensaje de error del formulario.
     *
     * Limpia su contenido y evita que la etiqueta ocupe espacio dentro de la
     * distribución visual.
     */
    private void ocultarError() {

        mensajeError.setText("");
        mensajeError.setVisible(false);
        mensajeError.setManaged(false);
    }
}
