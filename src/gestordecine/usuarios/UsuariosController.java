package gestordecine.usuarios;

import gestordecine.Cine;
import gestordecine.Cliente;
import gestordecine.PrincipalController;
import gestordecine.sesion.SesionCliente;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import java.util.ArrayList;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import gestordecine.PersistenciaCine;
import gestordecine.Rol;
import javafx.scene.control.ChoiceDialog;
import java.io.IOException;
import java.util.Optional;

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

        this.principalController = principalController;

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
     * Permite modificar el rol del usuario seleccionado.
     *
     * Realiza la modificación sobre una copia del cine y guarda los cambios
     * únicamente si la operación es válida. Luego actualiza el listado de
     * usuarios.
     */
    @FXML
    private void cambiarRol() {

        Cliente seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarMensaje("Seleccioná un usuario de la tabla");
            return;
        }

        ChoiceDialog<Rol> dialogo = new ChoiceDialog<>(
                seleccionado.getRol(),
                Rol.values()
        );

        dialogo.setTitle("Cambiar rol");
        dialogo.setHeaderText(
                seleccionado.getNombre() + " " + seleccionado.getApellido()
        );
        dialogo.setContentText("Nuevo rol:");

        Optional<Rol> resultado = dialogo.showAndWait();

        if (resultado.isEmpty()) {
            return;
        }

        try {
            Cine cineTrabajo = cine.copiar();

            Cliente administrador = cineTrabajo.getServicioDeReservas()
                    .buscarClientePorDni(
                            SesionCliente.getClienteActual().getDni()
                    );

            cineTrabajo.getServicioDeReservas().cambiarRolUsuario(
                    seleccionado.getDni(),
                    resultado.get(),
                    administrador
            );

            persistencia.guardar(cineTrabajo);
            principalController.actualizarCine(cineTrabajo);

            this.cine = cineTrabajo;
            usuariosRegistrados = cineTrabajo.getServicioDeReservas().getClientes();

            filtrarUsuarios();

            mostrarMensaje("Rol actualizado correctamente");

        } catch (IOException | ClassNotFoundException e) {
            mostrarMensaje("Error al guardar: " + e.getMessage());

        } catch (Exception e) {
            mostrarMensaje(e.getMessage());
        }
    }
}
