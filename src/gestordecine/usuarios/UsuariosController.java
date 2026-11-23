package gestordecine.usuarios;

import gestordecine.Cine;
import gestordecine.Cliente;
import gestordecine.PersistenciaCine;
import gestordecine.controladores.PrincipalController;
import gestordecine.sesion.SesionCliente;
import java.io.IOException;
import java.util.ArrayList;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * Controlador de la pantalla de administración de usuarios.
 *
 * Permite visualizar los usuarios registrados en CINEMA, buscar por DNI,
 * nombre, apellido o correo electrónico y filtrar los resultados según el rol.
 *
 * También permite registrar nuevos usuarios, modificar sus datos y
 * desactivarlos o reactivarlos.
 *
 * Verifica que exista una sesión administrativa antes de permitir el acceso a
 * la administración.
 *
 * Para modificar el estado de un usuario, trabaja sobre una copia del cine y
 * persiste los cambios antes de actualizar el estado de la aplicación.
 *
 * Se comunica con PrincipalController para gestionar la navegación y utiliza
 * ServicioDeReservas para administrar los usuarios registrados.
 */
public class UsuariosController {

    // Referencias al sistema.
    private PrincipalController principalController;
    private Cine cine;
    private PersistenciaCine persistencia;

    // Lista de usuarios utilizada para aplicar los filtros.
    private ArrayList<Cliente> usuariosRegistrados;

    // Tabla de usuarios.
    @FXML
    private TableView<Cliente> tablaUsuarios;

    @FXML
    private TableColumn<Cliente, Integer> columnaDni;

    @FXML
    private TableColumn<Cliente, String> columnaNombre;

    @FXML
    private TableColumn<Cliente, String> columnaApellido;

    @FXML
    private TableColumn<Cliente, String> columnaEmail;

    @FXML
    private TableColumn<Cliente, String> columnaRol;

    @FXML
    private TableColumn<Cliente, String> columnaEstado;

    // Componentes de búsqueda y filtrado.
    @FXML
    private TextField campoBusqueda;

    @FXML
    private ComboBox<String> filtroRol;

    @FXML
    private Label etiquetaCantidad;

    // Componentes de acciones y mensajes.
    @FXML
    private Button botonEstado;

    @FXML
    private Label mensajeEstado;

    /**
     * Configura la pantalla de administración de usuarios.
     *
     * Verifica que exista una sesión administrativa y asigna las referencias
     * del cine, la persistencia y el controlador principal.
     *
     * Configura las columnas de la tabla para mostrar los datos de cada
     * usuario, incluyendo su estado de activación.
     *
     * También carga los usuarios registrados, prepara los filtros de búsqueda y
     * rol, y establece los listeners que actualizan la tabla y las acciones
     * disponibles.
     *
     * @param cine Cine que contiene los usuarios registrados.
     * @param persistencia Objeto encargado de guardar los cambios.
     * @param principalController Controlador principal de CINEMA.
     * 
     * @throws IllegalStateException Si no existe una sesión administrativa.
     */
    public void configurar(
            Cine cine,
            PersistenciaCine persistencia,
            PrincipalController principalController) {

        if (!SesionCliente.esAdministrador()) {
            throw new IllegalStateException(
                    "No tiene permisos para administrar usuarios"
            );
        }

        this.cine = cine;
        this.persistencia = persistencia;
        this.principalController = principalController;

        // ESTADO
        columnaEstado.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        datos.getValue().isActivo()
                        ? "Activa" : "Inactiva"
                )
        );

        // Mostrar indicadores visuales según el estado.
        columnaEstado.setCellFactory(columna
                -> new TableCell<Cliente, String>() {

            @Override
            protected void updateItem(String estado, boolean vacio) {

                super.updateItem(estado, vacio);

                getStyleClass().removeAll(
                        "estado-activo", "estado-inactivo"
                );

                if (vacio || estado == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                boolean activo = estado.equals("Activa");

                setText(activo ? "A" : "✕");

                getStyleClass().add(
                        activo ? "estado-activo" : "estado-inactivo"
                );
            }
        });

        // DNI
        columnaDni.setCellValueFactory(datos
                -> new SimpleIntegerProperty(
                        datos.getValue().getDni()
                ).asObject()
        );

        // NOMBRE
        columnaNombre.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        datos.getValue().getNombre()
                )
        );

        // APELLIDO
        columnaApellido.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        datos.getValue().getApellido()
                )
        );

        // CORREO ELECTRÓNICO
        columnaEmail.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        datos.getValue().getEmail()
                )
        );

        // ROL
        columnaRol.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        datos.getValue().getRol().toString()
                )
        );

        // Obtener los usuarios registrados.
        usuariosRegistrados
                = cine.getServicioDeReservas().getClientes();

        // Actualizar el botón según el usuario seleccionado.
        tablaUsuarios.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, anterior, nuevo)
                        -> actualizarBotonEstado()
                );

        actualizarBotonEstado();

        // Configurar el filtro de roles.
        filtroRol.getItems().addAll(
                "Todos los roles",
                "Clientes",
                "Administradores"
        );

        filtroRol.getSelectionModel().selectFirst();

        // Aplicar filtros cuando cambia el texto de búsqueda.
        campoBusqueda.textProperty().addListener(
                (observable, anterior, nuevo)
                -> filtrarUsuarios()
        );

        // Aplicar filtros cuando cambia el rol seleccionado.
        filtroRol.valueProperty().addListener(
                (observable, anterior, nuevo)
                -> filtrarUsuarios()
        );

        // Mostrar inicialmente los usuarios.
        filtrarUsuarios();

        // Distribuir las columnas según el ancho disponible.
        tablaUsuarios.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );
    }

    /**
     * Abre el formulario para registrar un nuevo usuario.
     *
     * Utiliza PrincipalController para navegar hacia el formulario de registro.
     */
    @FXML
    private void nuevoUsuario() {

        principalController.mostrarFormularioNuevoUsuario();
    }

    /**
     * Abre el formulario de edición del usuario seleccionado.
     *
     * Comprueba que exista una selección en la tabla. Si no hay ningún usuario
     * seleccionado, muestra un mensaje informativo.
     *
     * Si existe una selección, utiliza PrincipalController para abrir el
     * formulario con los datos del usuario.
     */
    @FXML
    private void editarUsuario() {

        Cliente seleccionado
                = tablaUsuarios.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarMensaje("Seleccioná un usuario para editar");
            return;
        }

        principalController.mostrarFormularioEditarUsuario(
                seleccionado
        );
    }

    /**
     * Desactiva o reactiva el usuario seleccionado.
     *
     * Obtiene el usuario elegido en la tabla y comprueba que exista una
     * selección válida.
     *
     * Genera una copia del cine para realizar la operación sin modificar
     * directamente el estado original.
     *
     * Busca al administrador autenticado dentro de la copia y utiliza
     * ServicioDeReservas para cambiar el estado del usuario, aplicando las
     * reglas de negocio.
     *
     * Si la operación es válida, persiste los cambios, actualiza el cine activo
     * y recarga la tabla.
     *
     * Finalmente, vuelve a seleccionar el usuario modificado y actualiza el
     * texto del botón de estado.
     *
     * Si ocurre un error, muestra un mensaje dentro de la pantalla.
     */
    @FXML
    private void cambiarEstadoUsuario() {

        Cliente seleccionado
                = tablaUsuarios.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarMensaje("Seleccioná un usuario");
            return;
        }

        try {

            // Trabajar sobre una copia del cine.
            Cine cineTrabajo = cine.copiar();

            // Buscar al administrador autenticado dentro de la copia.
            Cliente administrador = cineTrabajo
                    .getServicioDeReservas()
                    .buscarClientePorDni(
                            SesionCliente.getClienteActual().getDni()
                    );

            // Cambiar el estado del usuario.
            cineTrabajo.getServicioDeReservas()
                    .cambiarEstadoUsuario(
                            seleccionado.getDni(),
                            administrador
                    );

            // Guardar los cambios antes de actualizar el cine activo.
            persistencia.guardar(cineTrabajo);

            principalController.actualizarCine(cineTrabajo);

            this.cine = cineTrabajo;

            // Actualizar la lista de usuarios registrados.
            usuariosRegistrados = cineTrabajo
                    .getServicioDeReservas()
                    .getClientes();

            // Actualizar la tabla aplicando los filtros actuales.
            filtrarUsuarios();

            // Buscar la versión actualizada del usuario.
            Cliente usuarioActualizado = cineTrabajo
                    .getServicioDeReservas()
                    .buscarClientePorDni(
                            seleccionado.getDni()
                    );

            // Mantener seleccionado el usuario modificado.
            tablaUsuarios.getSelectionModel().select(
                    usuarioActualizado
            );

            // Actualizar las celdas de la tabla.
            tablaUsuarios.refresh();

            actualizarBotonEstado();

            // Ocultar mensajes anteriores.
            mensajeEstado.setText("");
            mensajeEstado.setVisible(false);
            mensajeEstado.setManaged(false);

        } catch (IOException | ClassNotFoundException e) {

            mostrarMensaje(
                    "Error al guardar: " + e.getMessage()
            );

        } catch (Exception e) {

            mostrarMensaje(e.getMessage());
        }
    }

    /**
     * Regresa a la pantalla anterior mediante el controlador principal de
     * CINEMA.
     */
    @FXML
    private void volver() {

        principalController.volver();
    }

    /**
     * Filtra los usuarios registrados según el texto ingresado y el rol
     * seleccionado.
     *
     * Permite buscar coincidencias por DNI, nombre, apellido o correo
     * electrónico, sin distinguir mayúsculas de minúsculas en los campos de
     * texto.
     *
     * También permite limitar los resultados a clientes o administradores.
     *
     * Los criterios de búsqueda y rol se aplican conjuntamente.
     *
     * Finalmente, actualiza los usuarios mostrados en la tabla y la cantidad de
     * resultados encontrados.
     */
    private void filtrarUsuarios() {

        String busqueda
                = campoBusqueda.getText().trim().toLowerCase();

        String rolSeleccionado = filtroRol.getValue();

        ArrayList<Cliente> resultados = new ArrayList<>();

        for (Cliente cliente : usuariosRegistrados) {

            // BÚSQUEDA POR DATOS DEL USUARIO
            boolean coincideBusqueda
                    = String.valueOf(cliente.getDni()).contains(busqueda)
                    || cliente.getNombre().toLowerCase().contains(busqueda)
                    || cliente.getApellido().toLowerCase().contains(busqueda)
                    || cliente.getEmail().toLowerCase().contains(busqueda);

            if (!coincideBusqueda) {
                continue;
            }

            // FILTRO POR ROL
            if ("Clientes".equals(rolSeleccionado)
                    && !cliente.esCliente()) {
                continue;
            }

            if ("Administradores".equals(rolSeleccionado)
                    && !cliente.esAdministrador()) {
                continue;
            }

            resultados.add(cliente);
        }

        // Mostrar los usuarios que cumplen los filtros.
        tablaUsuarios.setItems(
                FXCollections.observableArrayList(resultados)
        );

        // Actualizar las celdas de la tabla.
        tablaUsuarios.refresh();

        // Mostrar la cantidad de resultados.
        etiquetaCantidad.setText(
                resultados.size() + " de "
                + usuariosRegistrados.size() + " usuarios"
        );
    }

    /**
     * Actualiza el botón de activación o desactivación según el usuario
     * seleccionado.
     *
     * Mantiene el estilo visual de peligro utilizado para las operaciones de
     * cambio de estado.
     *
     * Si no existe una selección, deshabilita el botón.
     *
     * Si el usuario está activo, muestra la opción "Desactivar usuario". En
     * caso contrario, muestra la opción "Restaurar usuario".
     */
    private void actualizarBotonEstado() {

        Cliente seleccionado
                = tablaUsuarios.getSelectionModel().getSelectedItem();

        // Mantener el estilo rojo.
        botonEstado.getStyleClass().remove(
                "boton-primario"
        );

        if (!botonEstado.getStyleClass().contains("boton-peligro")) {
            botonEstado.getStyleClass().add(
                    "boton-peligro"
            );
        }

        if (seleccionado == null) {

            botonEstado.setText("Desactivar usuario");
            botonEstado.setDisable(true);

            return;
        }

        botonEstado.setDisable(false);

        if (seleccionado.isActivo()) {

            botonEstado.setText("Desactivar usuario");

        } else {

            botonEstado.setText("Restaurar usuario");
        }
    }

    /**
     * Muestra un mensaje informativo dentro de la pantalla.
     *
     * Asigna el texto recibido a la etiqueta correspondiente y habilita su
     * visualización.
     *
     * @param mensaje Texto que se desea mostrar.
     */
    private void mostrarMensaje(String mensaje) {

        mensajeEstado.setText(mensaje);
        mensajeEstado.setVisible(true);
        mensajeEstado.setManaged(true);
    }
}
