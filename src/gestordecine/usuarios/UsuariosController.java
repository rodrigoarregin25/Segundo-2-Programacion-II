package gestordecine.usuarios;

import gestordecine.Cine;
import gestordecine.Cliente;
import gestordecine.PrincipalController;
import gestordecine.sesion.SesionCliente;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleStringProperty;
import java.util.ArrayList;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import gestordecine.PersistenciaCine;
import java.io.IOException;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;

public class UsuariosController {

    private PrincipalController principalController;
    private Cine cine;
    private PersistenciaCine persistencia;

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
    private TextField campoBusqueda;

    @FXML
    private ComboBox<String> filtroRol;

    @FXML
    private Label etiquetaCantidad;

    @FXML
    private TableColumn<Cliente, String> columnaEstado;

    @FXML
    private Button botonEstado;

    private ArrayList<Cliente> usuariosRegistrados;

    /**
     * Configura la administración de usuarios.
     *
     * Comprueba los permisos, carga los usuarios registrados e inicializa los
     * filtros de búsqueda y rol.
     *
     * @param cine Cine que contiene los usuarios registrados.
     * @param principalController Controlador principal de CINEMA.
     * @throws IllegalStateException Si no hay un administrador autenticado.
     */
    public void configurar(Cine cine, PersistenciaCine persistencia, PrincipalController principalController) {

        if (!SesionCliente.esAdministrador()) {
            throw new IllegalStateException(
                    "No tiene permisos para administrar usuarios"
            );
        }

        this.cine = cine;
        this.persistencia = persistencia;
        this.principalController = principalController;

        // Mostrar A para usuarios activos y ✕ para inactivos.
        columnaEstado.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        datos.getValue().isActivo() ? "Activa" : "Inactiva"
                )
        );

        // Aplicar los mismos indicadores que en las otras tablas.
        columnaEstado.setCellFactory(columna -> new TableCell<Cliente, String>() {

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

        columnaDni.setCellValueFactory(datos
                -> new SimpleIntegerProperty(
                        datos.getValue().getDni()
                ).asObject()
        );

        columnaNombre.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        datos.getValue().getNombre()
                )
        );

        columnaApellido.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        datos.getValue().getApellido()
                )
        );

        columnaEmail.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        datos.getValue().getEmail()
                )
        );

        columnaRol.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        datos.getValue().getRol().toString()
                )
        );

        usuariosRegistrados = cine.getServicioDeReservas().getClientes();

        // Actualizar el botón según el usuario seleccionado.
        tablaUsuarios.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, anterior, nuevo) -> actualizarBotonEstado());

        actualizarBotonEstado();

        filtroRol.getItems().addAll(
                "Todos los roles",
                "Clientes",
                "Administradores"
        );

        filtroRol.getSelectionModel().selectFirst();

        campoBusqueda.textProperty().addListener(
                (observable, anterior, nuevo) -> filtrarUsuarios()
        );

        filtroRol.valueProperty().addListener(
                (observable, anterior, nuevo) -> filtrarUsuarios()
        );

        filtrarUsuarios();
        // Distribuir las columnas según el ancho disponible.
        tablaUsuarios.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );
    }

    /**
     * Regresa a la cartelera principal.
     */
    @FXML
    private void volver() {
        principalController.volver();
    }

    /**
     * Filtra los usuarios registrados por DNI, nombre, apellido, correo
     * electrónico y rol.
     *
     * Actualiza la tabla y la cantidad de usuarios encontrados.
     */
    private void filtrarUsuarios() {

        String busqueda = campoBusqueda.getText().trim().toLowerCase();
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

        tablaUsuarios.setItems(
                FXCollections.observableArrayList(resultados)
        );
        // Forzar la actualización de las celdas de la tabla.
        tablaUsuarios.refresh();

        etiquetaCantidad.setText(
                resultados.size() + " de "
                + usuariosRegistrados.size() + " usuarios"
        );
    }

    @FXML
    private Label mensajeEstado;

    /**
     * Muestra un mensaje informativo en la pantalla de usuarios.
     *
     * @param mensaje Texto que se desea mostrar.
     */
    private void mostrarMensaje(String mensaje) {
        mensajeEstado.setText(mensaje);
        mensajeEstado.setVisible(true);
        mensajeEstado.setManaged(true);
    }

    /**
     * Abre el formulario de edición del usuario seleccionado.
     */
    @FXML
    private void editarUsuario() {

        Cliente seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarMensaje("Seleccioná un usuario para editar");
            return;
        }

        principalController.mostrarFormularioEditarUsuario(seleccionado);
    }

    /**
     * Actualiza el botón de estado según el usuario seleccionado.
     *
     * Muestra Desactivar para usuarios activos y Restaurar para usuarios
     * inactivos, manteniendo el mismo estilo.
     */
    private void actualizarBotonEstado() {

        Cliente seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();

        // Mantener siempre el estilo rojo.
        botonEstado.getStyleClass().remove("boton-primario");

        if (!botonEstado.getStyleClass().contains("boton-peligro")) {
            botonEstado.getStyleClass().add("boton-peligro");
        }

        if (seleccionado == null) {
            botonEstado.setText("Eliminar usuario");
            botonEstado.setDisable(true);
            return;
        }

        botonEstado.setDisable(false);

        if (seleccionado.isActivo()) {
            botonEstado.setText("Eliminar usuario");
        } else {
            botonEstado.setText("Restaurar usuario");
        }
    }

    /**
     * Desactiva o reactiva el usuario seleccionado.
     *
     * Verifica los permisos mediante ServicioDeReservas y guarda los cambios
     * sobre una copia del cine.
     */
    @FXML
    private void cambiarEstadoUsuario() {

        Cliente seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarMensaje("Seleccioná un usuario");
            return;
        }

        try {

            Cine cineTrabajo = cine.copiar();

            Cliente administrador = cineTrabajo.getServicioDeReservas()
                    .buscarClientePorDni(
                            SesionCliente.getClienteActual().getDni()
                    );

            cineTrabajo.getServicioDeReservas().cambiarEstadoUsuario(
                    seleccionado.getDni(),
                    administrador
            );

            persistencia.guardar(cineTrabajo);
            principalController.actualizarCine(cineTrabajo);

            this.cine = cineTrabajo;
            usuariosRegistrados = cineTrabajo.getServicioDeReservas().getClientes();

            filtrarUsuarios();

            Cliente usuarioActualizado = cineTrabajo.getServicioDeReservas()
                    .buscarClientePorDni(seleccionado.getDni());

            tablaUsuarios.getSelectionModel().select(usuarioActualizado);

            // Actualizar todas las celdas después de cambiar el estado.
            tablaUsuarios.refresh();

            actualizarBotonEstado();

            mensajeEstado.setText("");
            mensajeEstado.setVisible(false);
            mensajeEstado.setManaged(false);

        } catch (IOException | ClassNotFoundException e) {
            mostrarMensaje("Error al guardar: " + e.getMessage());

        } catch (Exception e) {
            mostrarMensaje(e.getMessage());
        }
    }

    /**
     * Abre el formulario para registrar un nuevo usuario.
     */
    @FXML
    private void nuevoUsuario() {
        principalController.mostrarFormularioNuevoUsuario();
    }
}
