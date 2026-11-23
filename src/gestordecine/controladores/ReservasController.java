package gestordecine.controladores;

import gestordecine.Cine;
import gestordecine.DialogoCinema;
import gestordecine.PersistenciaCine;
import gestordecine.Reserva;
import gestordecine.ServicioDeReservas;
import gestordecine.VentanaComprobante;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import gestordecine.persistencia.ExportadorReservasCSV;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableCell;
import javafx.beans.property.SimpleStringProperty;
import java.io.File;
import javafx.stage.FileChooser;

public class ReservasController {

    private Cine cine;
    private PersistenciaCine persistencia;
    private PrincipalController principalController;

    @FXML
    private ComboBox<String> comboCriterio;
    @FXML
    private TextField campoBusqueda;
    @FXML
    private TableView<Reserva> listaReservas;
    @FXML
    private Label mensajeError;
    @FXML
    private Button botonComprobante;
    @FXML
    private Button botonCancelar;
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

    /**
     * Configura los criterios de búsqueda y las columnas de la tabla
     * administrativa de reservas.
     *
     * También actualiza los botones según la reserva seleccionada por el
     * administrador.
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

        colEstado.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        datos.getValue().isConfirmada()
                        ? "CONFIRMADA" : "CANCELADA"
                )
        );

        colEstado.setCellFactory(columna -> new TableCell<Reserva, String>() {

            @Override
            protected void updateItem(String estado, boolean vacia) {
                super.updateItem(estado, vacia);

                if (vacia || estado == null) {
                    setText(null);
                    setGraphic(null);
                    getStyleClass().removeAll("estado-activo", "estado-inactivo");
                    return;
                }

                boolean confirmada = estado.equals("CONFIRMADA");

                setText(confirmada ? "A" : "✕");

                getStyleClass().removeAll("estado-activo", "estado-inactivo");
                getStyleClass().add(confirmada ? "estado-activo" : "estado-inactivo");
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
                        datos.getValue().getFuncion()
                                .getPelicula().getTituloPelicula()
                )
        );

        // FECHA Y HORA
        colFecha.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        datos.getValue().getFuncion()
                                .getFechaHora().format(formatoFecha)
                )
        );

        // SALA
        colSala.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        datos.getValue().getFuncion()
                                .getSala().getNombreSala()
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

        // HABILITAR ACCIONES SEGÚN LA SELECCIÓN
        listaReservas.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, anterior, nueva)
                        -> mostrarSeleccion(nueva)
                );

        listaReservas.setPlaceholder(
                new Label("No hay reservas para mostrar")
        );

        // Distribuir las columnas para ocupar el ancho disponible.
        listaReservas.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );
    }

    /**
     * Que hace: recibe el cine y la persistencia y muestra todas las reservas.
     * Parametros: cine (el cine actual), persistencia (para guardar cambios).
     */
    public void configurar(Cine cine, PersistenciaCine persistencia) {
        this.cine = cine;
        this.persistencia = persistencia;
        buscar();
    }

    public void setPrincipalController(PrincipalController principalController) {
        this.principalController = principalController;
    }

    /**
     * Que hace: busca reservas por codigo o por DNI. Si el campo esta vacio,
     * muestra todas las reservas.
     */
    @FXML
    private void buscar() {
        ocultarError();

        ServicioDeReservas servicio = cine.getServicioDeReservas();
        String texto = campoBusqueda.getText().trim();
        ArrayList<Reserva> resultado;

        try {
            if (texto.isEmpty()) {
                resultado = servicio.getReservas();
            } else if (comboCriterio.getSelectionModel().getSelectedIndex() == 0) {
                resultado = new ArrayList<>();
                Reserva encontrada = servicio.buscarPorCodigo(texto.toUpperCase());
                if (encontrada != null) {
                    resultado.add(encontrada);
                }
            } else {
                resultado = servicio.buscarPorDni(Integer.parseInt(texto));
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

    @FXML
    private void verTodas() {
        campoBusqueda.clear();
        buscar();
    }

    /**
     * Que hace: muestra las reservas en la lista, una tarjeta por reserva.
     * Parametros: reservas (las reservas a mostrar).
     */
    private void mostrarLista(ArrayList<Reserva> reservas) {
        listaReservas.setItems(FXCollections.observableArrayList(reservas));
        listaReservas.getSelectionModel().clearSelection();
        mostrarSeleccion(null);
    }

    /**
     * Que hace: al elegir una reserva de la lista, habilita los botones. El
     * boton de cancelar solo se habilita si la reserva esta confirmada.
     * Parametros: reserva (la reserva elegida, null si no hay ninguna).
     */
    private void mostrarSeleccion(Reserva reserva) {
        botonComprobante.setDisable(reserva == null);
        botonCancelar.setDisable(reserva == null || !reserva.isConfirmada());
    }

    /**
     * Que hace: muestra el comprobante de la reserva seleccionada en una
     * ventana emergente con formato de ticket.
     */
    @FXML
    private void verComprobante() {
        Reserva seleccionada = listaReservas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            return;
        }

        VentanaComprobante.mostrar(seleccionada);
    }

    /**
     * Que hace: pide confirmacion y cancela la reserva seleccionada. La reserva
     * no se borra: queda como cancelada y sus butacas se liberan. Se trabaja
     * sobre una copia del cine.
     */
    @FXML
    private void cancelarReserva() {
        ocultarError();

        Reserva seleccionada = listaReservas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarError("Seleccione una reserva");
            return;
        }

        // Pedir confirmacion antes de cancelar (igual que en Mis reservas)
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);

        confirmacion.setTitle("Cancelar reserva");
        confirmacion.setHeaderText("¿Querés cancelar esta reserva?");
        confirmacion.setContentText(
                "Código: " + seleccionada.getCodigoReserva()
                + "\nEsta operación liberará las butacas reservadas."
        );

        confirmacion.initOwner(listaReservas.getScene().getWindow());

        // Mismo estilo oscuro que usan los otros carteles del sistema
        confirmacion.getDialogPane().getStylesheets().add(
                getClass().getResource("/gestordecine/estilos/estilos.css").toExternalForm()
        );
        confirmacion.getDialogPane().getStyleClass().add("alerta-cinema");

        Button botonSi = (Button) confirmacion.getDialogPane()
                .lookupButton(ButtonType.OK);
        botonSi.setText("Sí, cancelar");
        botonSi.getStyleClass().add("boton-alerta-cinema");

        Button botonNo = (Button) confirmacion.getDialogPane()
                .lookupButton(ButtonType.CANCEL);
        botonNo.setText("Conservar");
        botonNo.getStyleClass().add("boton-alerta-cinema");

        ButtonType respuesta = confirmacion.showAndWait()
                .orElse(ButtonType.CANCEL);

        if (respuesta != ButtonType.OK) {
            return;
        }

        try {
            String codigo = seleccionada.getCodigoReserva();

            Cine cineTrabajo = cine.copiar();
            cineTrabajo.getServicioDeReservas().cancelarReserva(codigo);

            persistencia.guardar(cineTrabajo);
            principalController.actualizarCine(cineTrabajo);
            this.cine = cineTrabajo;

            // Se vuelve a cargar la lista y se deja marcada la misma reserva
            buscar();
            for (Reserva reserva : listaReservas.getItems()) {
                if (reserva.getCodigoReserva().equals(codigo)) {
                    listaReservas.getSelectionModel().select(reserva);
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            // Error de datos (archivo / copia), distinto de un error de reglas
            mostrarError("Error al guardar los datos: " + e.getMessage());
        } catch (Exception e) {
            // Error de reglas de negocio
            mostrarError(e.getMessage());
        }
    }

    @FXML
    private void nuevaReserva() {
        principalController.mostrarNuevaReserva();
    }

    private void mostrarError(String mensaje) {
        mensajeError.setText(mensaje);
        mensajeError.setVisible(true);
        mensajeError.setManaged(true);
    }

    private void ocultarError() {
        mensajeError.setVisible(false);
        mensajeError.setManaged(false);
    }

    /**
     * Exporta todas las reservas registradas en el cine a un archivo CSV.
     *
     * Permite seleccionar el nombre y la ubicación del archivo. La exportación
     * incluye reservas confirmadas y canceladas, independientemente del filtro
     * aplicado en la pantalla.
     *
     * No modifica las reservas ni el archivo de persistencia del cine.
     *
     * @throws IOException Si ocurre un error al escribir el archivo, capturado
     * e informado dentro del método.
     */
    @FXML
    private void exportarCSV() {

        ocultarError();

        // Crear el selector de archivos.
        FileChooser selector = new FileChooser();

        selector.setTitle("Exportar reservas");
        selector.setInitialFileName("reservas.csv");

        // Permitir guardar archivos CSV.
        selector.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Archivos CSV (*.csv)", "*.csv"
                )
        );

        // Abrir el selector de ubicación.
        File archivo = selector.showSaveDialog(
                listaReservas.getScene().getWindow()
        );

        // Si el usuario cancela, no se realiza ninguna operación.
        if (archivo == null) {
            return;
        }

        try {

            // Exportar todas las reservas, no solamente las filtradas.
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
                    "No se pudo generar el archivo CSV: " + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    /**
     * Regresa a la cartelera principal.
     */
    @FXML
    private void volver() {
        principalController.volver();
    }
}
