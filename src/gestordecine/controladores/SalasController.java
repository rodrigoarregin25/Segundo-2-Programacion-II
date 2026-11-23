package gestordecine.controladores;

import gestordecine.Cine;
import gestordecine.DialogoCinema;
import gestordecine.PersistenciaCine;
import gestordecine.Sala;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class SalasController {

    private Cine cine;
    private PrincipalController principalController;
    private PersistenciaCine persistencia;

    public void configurar(Cine cine, PersistenciaCine persistencia) {

        this.cine = cine;
        this.persistencia = persistencia;

        ObservableList<Sala> salas
                = FXCollections.observableArrayList(cine.getSalas());

        FilteredList<Sala> salasFiltradas
                = new FilteredList<>(salas);

        tablaSalas.setItems(salasFiltradas);

        filtroEstado.valueProperty().addListener((observable, anterior, nuevo) -> {

            salasFiltradas.setPredicate(sala -> {

                if ("Activas".equals(nuevo)) {
                    return sala.isActiva();
                }

                if ("Inactivas".equals(nuevo)) {
                    return !sala.isActiva();
                }

                return true;
            });

            // Seleccionar la primera sala del nuevo filtro
            if (!salasFiltradas.isEmpty()) {
                tablaSalas.getSelectionModel().selectFirst();
            } else {
                tablaSalas.getSelectionModel().clearSelection();
                contenedorButacas.getChildren().clear();
                nombreSalaSeleccionada.setText("No hay salas disponibles.");
                cantidadButacas.setText("");
            }
        });

        // Mostrar inicialmente las salas activas
        salasFiltradas.setPredicate(Sala::isActiva);

        if (!salasFiltradas.isEmpty()) {
            tablaSalas.getSelectionModel().selectFirst();
        }
    }

    private static final double ANCHO_BUTACA = 28;
    private static final double ESPACIO_ENTRE_ELEMENTOS = 8;
    private static final double ANCHO_PASILLO = 26;
    private static final double ANCHO_ETIQUETA_FILA = 24;

    private void mostrarDistribucion(Sala sala) {

        nombreSalaSeleccionada.setText(
                sala.getNombreSala()
                + " | " + sala.getCantidadFilas() + " filas"
                + " | " + sala.getButacasPorFila() + " butacas por fila"
        );

        cantidadButacas.setText(
                "Capacidad total: " + sala.getButacas().size() + " butacas"
        );

        contenedorButacas.getChildren().clear();

        String[] bloques = sala.getDistribucionButacas().split("-");

        // PANTALLA CURVA
        StackPane pantallaCurva = crearPantallaCurva(sala);
        contenedorButacas.getChildren().add(pantallaCurva);

        // FILAS
        for (int i = 0; i < sala.getCantidadFilas(); i++) {

            char letraFila = (char) ('A' + i);

            HBox filaVisual = new HBox(ESPACIO_ENTRE_ELEMENTOS);
            filaVisual.setAlignment(Pos.CENTER);

            // Etiqueta izquierda
            Label etiquetaFilaIzq = new Label(String.valueOf(letraFila));
            etiquetaFilaIzq.getStyleClass().add("etiqueta-fila");
            etiquetaFilaIzq.setMinWidth(ANCHO_ETIQUETA_FILA);
            etiquetaFilaIzq.setPrefWidth(ANCHO_ETIQUETA_FILA);
            etiquetaFilaIzq.setMaxWidth(ANCHO_ETIQUETA_FILA);
            etiquetaFilaIzq.setAlignment(Pos.CENTER);

            filaVisual.getChildren().add(etiquetaFilaIzq);

            int numeroButaca = 1;

            for (int b = 0; b < bloques.length; b++) {

                int cantidadBloque = Integer.parseInt(bloques[b].trim());

                for (int j = 0; j < cantidadBloque; j++) {

                    Label butaca = new Label(String.valueOf(numeroButaca));
                    butaca.getStyleClass().add("butaca-visual");
                    butaca.setAlignment(Pos.CENTER);

                    butaca.setMinWidth(ANCHO_BUTACA);
                    butaca.setPrefWidth(ANCHO_BUTACA);
                    butaca.setMaxWidth(ANCHO_BUTACA);

                    filaVisual.getChildren().add(butaca);
                    numeroButaca++;
                }

                if (b < bloques.length - 1) {
                    Region pasillo = new Region();
                    pasillo.setMinWidth(ANCHO_PASILLO);
                    pasillo.setPrefWidth(ANCHO_PASILLO);
                    pasillo.setMaxWidth(ANCHO_PASILLO);

                    filaVisual.getChildren().add(pasillo);
                }
            }

            // Espaciador derecho para compensar la letra izquierda
            Region compensadorDerecho = new Region();
            compensadorDerecho.setMinWidth(ANCHO_ETIQUETA_FILA);
            compensadorDerecho.setPrefWidth(ANCHO_ETIQUETA_FILA);
            compensadorDerecho.setMaxWidth(ANCHO_ETIQUETA_FILA);

            filaVisual.getChildren().add(compensadorDerecho);

            contenedorButacas.getChildren().add(filaVisual);
        }
    }

    private double calcularAnchoBloqueButacas(Sala sala) {

        String[] bloques = sala.getDistribucionButacas().split("-");

        int totalButacas = 0;

        for (String bloque : bloques) {
            totalButacas += Integer.parseInt(bloque.trim());
        }

        int cantidadPasillos = bloques.length - 1;
        int totalElementos = totalButacas + cantidadPasillos;

        return (totalButacas * ANCHO_BUTACA)
                + (cantidadPasillos * ANCHO_PASILLO)
                + ((totalElementos - 1) * ESPACIO_ENTRE_ELEMENTOS);
    }

    @FXML
    private void initialize() {

    // COLUMNA ESTADO
        columnaEstado.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        datos.getValue().isActiva() ? "Activa" : "Inactiva"
                )
        );

        columnaEstado.setCellFactory(columna -> new TableCell<Sala, String>() {

            @Override
            protected void updateItem(String estado, boolean vacia) {

                super.updateItem(estado, vacia);

                getStyleClass().removeAll("estado-activo", "estado-inactivo");

                if (vacia || estado == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                boolean activa = estado.equals("Activa");

                setText(activa ? "A" : "✕");

                getStyleClass().add(
                        activa ? "estado-activo" : "estado-inactivo"
                );
            }
        });

        // COLUMNA NOMBRE
        columnaNombre.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        datos.getValue().getNombreSala()
                )
        );

        columnaNombre.setStyle("-fx-alignment: CENTER-LEFT;");

        // AJUSTE DE ANCHO
        tablaSalas.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );


        // SELECCIÓN DE SALA
        tablaSalas.getSelectionModel().selectedItemProperty()
                .addListener((observable, anterior, seleccionada) -> {

                    mensajeError.setText("");
                    mensajeError.setVisible(false);
                    mensajeError.setManaged(false);

                    if (seleccionada == null) {
                        botonEstado.setText("Desactivar sala");
                        botonEstado.setDisable(true);
                        return;
                    }

                    // Mostrar la distribución de butacas
                    mostrarDistribucion(seleccionada);

                    // Actualizar botón según estado
                    botonEstado.setDisable(false);

                    if (seleccionada.isActiva()) {
                        botonEstado.setText("Desactivar sala");
                    } else {
                        botonEstado.setText("Restaurar sala");
                    }
                });

        // DISTRIBUCIÓN DE BUTACAS
        contenedorButacas.setAlignment(Pos.TOP_CENTER);

        // FILTRO DE ESTADO
        filtroEstado.getItems().addAll("Activas", "Inactivas", "Todas");
        filtroEstado.setValue("Activas");

        // ESTADO INICIAL
        botonEstado.setDisable(true);

        columnaCapacidad.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        String.valueOf(datos.getValue().getButacas().size())
                )
        );
    }

    public void setPrincipalController(PrincipalController principalController) {
        this.principalController = principalController;
    }

    private StackPane crearPantallaCurva(Sala sala) {

        double ancho = calcularAnchoBloqueButacas(sala);

        Arc arco = new Arc();
        arco.setRadiusX(ancho / 2);
        arco.setRadiusY(25);
        arco.setStartAngle(0);
        arco.setLength(180);
        arco.setType(ArcType.OPEN);
        arco.setFill(null);
        arco.setStroke(Color.web("#7E858F"));
        arco.setStrokeWidth(5);

        Label textoPantalla = new Label("PANTALLA");
        textoPantalla.getStyleClass().add("texto-pantalla");

        VBox contenido = new VBox(0.1);
        contenido.setAlignment(Pos.CENTER);
        contenido.getChildren().addAll(arco, textoPantalla);

        StackPane contenedor = new StackPane(contenido);
        contenedor.setAlignment(Pos.CENTER);
        contenedor.setPrefWidth(ancho + (ANCHO_ETIQUETA_FILA * 2));

        return contenedor;
    }

    @FXML
    private void nuevaSala() {
        principalController.mostrarFormularioSala();
    }

    @FXML
    private TableView<Sala> tablaSalas;

    @FXML
    private TableColumn<Sala, String> columnaEstado;

    @FXML
    private TableColumn<Sala, String> columnaNombre;

    @FXML
    private Label nombreSalaSeleccionada;

    @FXML
    private VBox contenedorButacas;

    @FXML
    private Label cantidadButacas;

    @FXML
    private Label mensajeError;

    @FXML
    private ComboBox<String> filtroEstado;

    @FXML
    private Button botonEstado;

    @FXML
    private TableColumn<Sala, String> columnaCapacidad;

    private void mostrarError(String mensaje) {
        mensajeError.setText(mensaje);
        mensajeError.setVisible(true);
        mensajeError.setManaged(true);
    }

    @FXML
    private void editarSala() {

        Sala seleccionada = tablaSalas.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            System.out.println("Seleccioná una sala para editar.");
            return;
        }

        principalController.mostrarFormularioEditarSala(seleccionada);
    }

    @FXML
    private void cambiarEstadoSala() {

        Sala seleccionada = tablaSalas.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            return;
        }

        try {
            Cine cineTrabajo = cine.copiar();

            if (cineTrabajo.getSalas().size() != cine.getSalas().size()) {
                throw new IllegalStateException(
                        "La copia del cine tiene una cantidad diferente de salas."
                );
            }

            if (seleccionada.isActiva()) {
                cineTrabajo.desactivarSala(seleccionada.getIdSala());
            } else {
                cineTrabajo.reactivarSala(seleccionada.getIdSala());
            }

            persistencia.guardar(cineTrabajo);

            principalController.actualizarCine(cineTrabajo);
            principalController.mostrarSalas();

        } catch (IllegalStateException e) {

            DialogoCinema.mostrarError(
                    tablaSalas.getScene().getWindow(),
                    "No se puede desactivar la sala",
                    e.getMessage()
            );

        } catch (Exception e) {

            DialogoCinema.mostrarError(
                    tablaSalas.getScene().getWindow(),
                    "Error al modificar la sala",
                    "No se pudo completar la operación: " + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String mensaje) {

        Alert alerta = new Alert(Alert.AlertType.WARNING);

        alerta.setTitle("CINEMA");
        alerta.setHeaderText("No se puede desactivar la sala");
        alerta.setContentText(mensaje);

        // Aplicar el CSS de la aplicación
        alerta.getDialogPane().getStylesheets().add(
                getClass().getResource("/gestordecine/estilos/estilos.css").toExternalForm()
        );

        alerta.getDialogPane().getStyleClass().add("alerta-cinema");

        // Personalizar botón
        Button botonAceptar = (Button) alerta.getDialogPane()
                .lookupButton(ButtonType.OK);

        botonAceptar.setText("Entendido");
        botonAceptar.getStyleClass().add("boton-alerta-cinema");

        // Mostrar ventana
        alerta.showAndWait();
    }

    /**
     * Regresa a la cartelera principal.
     */
    @FXML
    private void volver() {
        principalController.volver();
    }
}
