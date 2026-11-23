package gestordecine.controladores;

import gestordecine.Cine;
import gestordecine.DialogoCinema;
import gestordecine.PersistenciaCine;
import gestordecine.Reserva;
import gestordecine.ServicioDeReservas;
import gestordecine.VentanaComprobante;
import gestordecine.persistencia.ExportadorReservasCSV;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

/**
 * Controlador de la pantalla de administración de reservas del cine.
 *
 * Permite consultar todas las reservas registradas, buscar por código o DNI,
 * visualizar comprobantes y cancelar reservas confirmadas.
 *
 * También permite exportar el historial completo de reservas a un archivo CSV,
 * incluyendo tanto las confirmadas como las canceladas.
 *
 * Para cancelar una reserva, trabaja sobre una copia del cine y persiste los
 * cambios antes de actualizar el estado de la aplicación.
 *
 * Se comunica con PrincipalController para gestionar la navegación y utiliza
 * ServicioDeReservas para consultar y cancelar reservas.
 */
public class ReservasController {

    // Referencias al sistema.
    private Cine cine;
    private PersistenciaCine persistencia;
    private PrincipalController principalController;

    // Componentes de búsqueda.
    @FXML
    private ComboBox<String> comboCriterio;

    @FXML
    private TextField campoBusqueda;

    // Tabla de reservas.
    @FXML
    private TableView<Reserva> listaReservas;

    @FXML
    private TableColumn<Reserva, String> colEstado;

    @FXML
    private TableColumn<Reserva, String> colCodigo;

    @FXML
    private TableColumn<Reserva, String> colPelicula;

    @FXML
    private TableColumn<Reserva, String> colFecha;

    @FXML
    private TableColumn<Reserva, String> colSala;

    @FXML
    private TableColumn<Reserva, String> colCliente;

    @FXML
    private TableColumn<Reserva, String> colDni;

    @FXML
    private TableColumn<Reserva, String> colEntradas;

    @FXML
    private TableColumn<Reserva, String> colTotal;

    // Botones y mensajes.
    @FXML
    private Button botonComprobante;

    @FXML
    private Button botonCancelar;

    @FXML
    private Label mensajeError;

    /**
     * Inicializa los componentes de la pantalla de reservas.
     *
     * Configura los criterios de búsqueda, las columnas de la tabla y los
     * formatos utilizados para mostrar fechas e importes.
     *
     * También establece la representación visual del estado de cada reserva y
     * registra un listener para habilitar o deshabilitar las acciones según la
     * reserva seleccionada.
     *
     * JavaFX ejecuta este método automáticamente después de cargar los
     * componentes definidos en el archivo FXML.
     */
    @FXML
    private void initialize() {

        comboCriterio.getItems().addAll("Codigo", "DNI");
        comboCriterio.getSelectionModel().selectFirst();

        botonComprobante.setDisable(true);
        botonCancelar.setDisable(true);

        DateTimeFormatter formatoFecha
                = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // ESTADO
        colEstado.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        datos.getValue().isConfirmada()
                        ? "CONFIRMADA" : "CANCELADA"
                )
        );

        colEstado.setCellFactory(columna
                -> new TableCell<Reserva, String>() {

            @Override
            protected void updateItem(String estado, boolean vacia) {

                super.updateItem(estado, vacia);

                if (vacia || estado == null) {

                    setText(null);
                    setGraphic(null);

                    getStyleClass().removeAll(
                            "estado-activo", "estado-inactivo"
                    );

                    return;
                }

                boolean confirmada = estado.equals("CONFIRMADA");

                setText(confirmada ? "A" : "✕");

                getStyleClass().removeAll(
                        "estado-activo", "estado-inactivo"
                );

                getStyleClass().add(
                        confirmada ? "estado-activo" : "estado-inactivo"
                );
            }
        });

        // CÓDIGO
        colCodigo.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        datos.getValue().getCodigoReserva()
                )
        );

        // PELÍCULA
        colPelicula.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        datos.getValue()
                                .getFuncion()
                                .getPelicula()
                                .getTituloPelicula()
                )
        );

        // FECHA Y HORA
        colFecha.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        datos.getValue()
                                .getFuncion()
                                .getFechaHora()
                                .format(formatoFecha)
                )
        );

        // SALA
        colSala.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        datos.getValue()
                                .getFuncion()
                                .getSala()
                                .getNombreSala()
                )
        );

        // CLIENTE
        colCliente.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        datos.getValue().getCliente().getNombre()
                        + " "
                        + datos.getValue().getCliente().getApellido()
                )
        );

        // DNI
        colDni.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        String.valueOf(
                                datos.getValue().getCliente().getDni()
                        )
                )
        );

        // CANTIDAD DE ENTRADAS
        colEntradas.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        String.valueOf(
                                datos.getValue().getEntradas().size()
                        )
                )
        );

        // IMPORTE TOTAL
        colTotal.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        String.format(
                                Locale.of("es", "AR"),
                                "$%,.2f",
                                datos.getValue().getImporteTotal()
                        )
                )
        );

        // Actualizar las acciones según la reserva seleccionada.
        listaReservas.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, anterior, nueva)
                        -> mostrarSeleccion(nueva)
                );

        listaReservas.setPlaceholder(
                new Label("No hay reservas para mostrar")
        );

        // Distribuir las columnas dentro de la tabla.
        listaReservas.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );
    }

    /**
     * Configura la pantalla con las referencias necesarias.
     *
     * Recibe el cine actual y el administrador de persistencia. Posteriormente,
     * carga las reservas registradas en la tabla.
     *
     * @param cine Cine que contiene el servicio de reservas.
     * @param persistencia Objeto encargado de guardar el estado del cine.
     */
    public void configurar(Cine cine, PersistenciaCine persistencia) {

        this.cine = cine;
        this.persistencia = persistencia;

        buscar();
    }

    /**
     * Asigna el controlador principal de la aplicación.
     *
     * Permite navegar hacia otras pantallas y actualizar el estado del cine
     * después de realizar modificaciones.
     *
     * @param principalController Controlador principal de CINEMA.
     */
    public void setPrincipalController(
            PrincipalController principalController) {

        this.principalController = principalController;
    }

    /**
     * Busca reservas según el criterio seleccionado.
     *
     * Permite buscar una reserva por su código o consultar las reservas
     * asociadas a un DNI.
     *
     * Si el campo de búsqueda está vacío, muestra todas las reservas
     * registradas, sin importar su estado.
     *
     * Si el DNI no tiene un formato numérico válido, o no se encuentran
     * resultados, informa el error mediante una etiqueta dentro de la pantalla.
     */
    @FXML
    private void buscar() {

        ocultarError();

        ServicioDeReservas servicio = cine.getServicioDeReservas();

        String texto = campoBusqueda.getText().trim();

        ArrayList<Reserva> resultado;

        try {

            if (texto.isEmpty()) {

                // Mostrar todas las reservas.
                resultado = servicio.getReservas();

            } else if (comboCriterio.getSelectionModel()
                    .getSelectedIndex() == 0) {

                // Buscar por código de reserva.
                resultado = new ArrayList<>();

                Reserva encontrada = servicio.buscarPorCodigo(
                        texto.toUpperCase()
                );

                if (encontrada != null) {
                    resultado.add(encontrada);
                }

            } else {

                // Buscar reservas por DNI.
                resultado = servicio.buscarPorDni(
                        Integer.parseInt(texto)
                );
            }

        } catch (NumberFormatException e) {

            mostrarError("El DNI debe ser un numero");
            return;
        }

        mostrarLista(resultado);

        if (resultado.isEmpty() && !texto.isEmpty()) {
            mostrarError("No se encontraron reservas");
        }
    }

    /**
     * Restablece el listado completo de reservas.
     *
     * Limpia el campo de búsqueda y ejecuta nuevamente el método buscar() para
     * mostrar todos los registros.
     */
    @FXML
    private void verTodas() {

        campoBusqueda.clear();
        buscar();
    }

    /**
     * Muestra las reservas recibidas en la tabla.
     *
     * Convierte la lista en una colección observable, elimina la selección
     * anterior y deshabilita las acciones asociadas a una reserva.
     *
     * @param reservas Lista de reservas que se desean mostrar.
     */
    private void mostrarLista(ArrayList<Reserva> reservas) {

        listaReservas.setItems(
                FXCollections.observableArrayList(reservas)
        );

        listaReservas.getSelectionModel().clearSelection();

        mostrarSeleccion(null);
    }

    /**
     * Actualiza los botones según la reserva seleccionada.
     *
     * Permite visualizar el comprobante cuando existe una reserva seleccionada.
     *
     * El botón de cancelación solamente se habilita si la reserva seleccionada
     * está confirmada.
     *
     * @param reserva Reserva seleccionada, o null si no existe selección.
     */
    private void mostrarSeleccion(Reserva reserva) {

        botonComprobante.setDisable(
                reserva == null
        );

        botonCancelar.setDisable(
                reserva == null || !reserva.isConfirmada()
        );
    }

    /**
     * Muestra el comprobante de la reserva seleccionada.
     *
     * Utiliza VentanaComprobante para presentar una ventana emergente con los
     * datos de la reserva, incluyendo su estado actual, entradas e importe
     * total.
     *
     * Si no existe una reserva seleccionada, no realiza ninguna operación.
     */
    @FXML
    private void verComprobante() {

        Reserva seleccionada
                = listaReservas.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            return;
        }

        VentanaComprobante.mostrar(seleccionada);
    }

    /**
     * Cancela la reserva seleccionada después de solicitar confirmación.
     *
     * Presenta un diálogo que permite confirmar o rechazar la cancelación.
     *
     * Si se confirma, genera una copia del cine y utiliza ServicioDeReservas
     * para cancelar la reserva mediante su código.
     *
     * La reserva no se elimina del historial y sus butacas vuelven a estar
     * disponibles para futuras compras.
     *
     * Después de persistir los cambios, actualiza el cine activo, recarga la
     * tabla y vuelve a seleccionar la reserva cancelada.
     *
     * Si ocurre un error, lo informa dentro de la pantalla.
     */
    @FXML
    private void cancelarReserva() {

        ocultarError();

        Reserva seleccionada
                = listaReservas.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {

            mostrarError("Seleccione una reserva");
            return;
        }

        // Solicitar confirmación antes de cancelar.
        Alert confirmacion = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        confirmacion.setTitle("Cancelar reserva");

        confirmacion.setHeaderText(
                "¿Querés cancelar esta reserva?"
        );

        confirmacion.setContentText(
                "Código: " + seleccionada.getCodigoReserva()
                + "\nEsta operación liberará las butacas reservadas."
        );

        confirmacion.initOwner(
                listaReservas.getScene().getWindow()
        );

        // Aplicar el estilo visual de CINEMA.
        confirmacion.getDialogPane().getStylesheets().add(
                getClass().getResource(
                        "/gestordecine/estilos/estilos.css"
                ).toExternalForm()
        );

        confirmacion.getDialogPane()
                .getStyleClass()
                .add("alerta-cinema");

        // Personalizar el botón de confirmación.
        Button botonSi = (Button) confirmacion.getDialogPane()
                .lookupButton(ButtonType.OK);

        botonSi.setText("Sí, cancelar");

        botonSi.getStyleClass().add(
                "boton-alerta-cinema"
        );

        // Personalizar el botón para conservar la reserva.
        Button botonNo = (Button) confirmacion.getDialogPane()
                .lookupButton(ButtonType.CANCEL);

        botonNo.setText("Conservar");

        botonNo.getStyleClass().add(
                "boton-alerta-cinema"
        );

        ButtonType respuesta = confirmacion.showAndWait()
                .orElse(ButtonType.CANCEL);

        // No realizar cambios si el usuario no confirma.
        if (respuesta != ButtonType.OK) {
            return;
        }

        try {

            String codigo = seleccionada.getCodigoReserva();

            // Trabajar sobre una copia del cine.
            Cine cineTrabajo = cine.copiar();

            // Cancelar la reserva sobre la copia.
            cineTrabajo.getServicioDeReservas()
                    .cancelarReserva(codigo);

            // Persistir antes de actualizar el cine activo.
            persistencia.guardar(cineTrabajo);

            principalController.actualizarCine(cineTrabajo);

            this.cine = cineTrabajo;

            // Recargar la tabla de reservas.
            buscar();

            // Volver a seleccionar la reserva cancelada.
            for (Reserva reserva : listaReservas.getItems()) {

                if (reserva.getCodigoReserva().equals(codigo)) {

                    listaReservas.getSelectionModel()
                            .select(reserva);
                }
            }

        } catch (IOException | ClassNotFoundException e) {

            mostrarError(
                    "Error al guardar los datos: " + e.getMessage()
            );

        } catch (Exception e) {

            mostrarError(e.getMessage());
        }
    }

    /**
     * Abre la pantalla de registro de una nueva reserva.
     *
     * Utiliza PrincipalController para realizar la navegación hacia el
     * formulario correspondiente.
     */
    @FXML
    private void nuevaReserva() {

        principalController.mostrarNuevaReserva();
    }

    /**
     * Exporta todas las reservas del cine a un archivo CSV.
     *
     * Abre un selector de archivos para que el usuario indique el nombre y la
     * ubicación donde se guardará la exportación.
     *
     * Incluye tanto reservas confirmadas como canceladas, independientemente
     * del criterio de búsqueda utilizado en la pantalla.
     *
     * Si el usuario cancela el selector, no realiza ninguna operación.
     *
     * Si la exportación finaliza correctamente, muestra un mensaje de
     * confirmación. Si ocurre un error de escritura, lo informa mediante un
     * diálogo.
     *
     * No modifica las reservas ni el archivo de persistencia del cine.
     */
    @FXML
    private void exportarCSV() {

        ocultarError();

        // Configurar el selector de archivos.
        FileChooser selector = new FileChooser();

        selector.setTitle("Exportar reservas");

        selector.setInitialFileName("reservas.csv");

        selector.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Archivos CSV (*.csv)",
                        "*.csv"
                )
        );

        // Solicitar la ubicación del archivo.
        File archivo = selector.showSaveDialog(
                listaReservas.getScene().getWindow()
        );

        // No exportar si el usuario cancela.
        if (archivo == null) {
            return;
        }

        try {

            // Exportar todas las reservas registradas.
            ExportadorReservasCSV.exportar(
                    cine.getServicioDeReservas().getReservas(),
                    archivo.toPath()
            );

            DialogoCinema.mostrarExito(
                    listaReservas.getScene().getWindow(),
                    "Exportación finalizada",
                    "Las reservas se exportaron correctamente."
            );

        } catch (IOException e) {

            DialogoCinema.mostrarError(
                    listaReservas.getScene().getWindow(),
                    "Error al exportar reservas",
                    "No se pudo generar el archivo CSV: "
                    + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    /**
     * Regresa a la pantalla anterior mediante el controlador principal.
     */
    @FXML
    private void volver() {

        principalController.volver();
    }

    /**
     * Muestra un mensaje de error dentro de la pantalla.
     *
     * Asigna el texto recibido a la etiqueta correspondiente y permite que se
     * visualice en la interfaz.
     *
     * @param mensaje Texto del error que se desea mostrar.
     */
    private void mostrarError(String mensaje) {

        mensajeError.setText(mensaje);
        mensajeError.setVisible(true);
        mensajeError.setManaged(true);
    }

    /**
     * Oculta el mensaje de error de la pantalla.
     *
     * También evita que la etiqueta ocupe espacio dentro de la distribución de
     * la interfaz.
     */
    private void ocultarError() {

        mensajeError.setVisible(false);
        mensajeError.setManaged(false);
    }
}
