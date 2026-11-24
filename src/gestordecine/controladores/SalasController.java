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

/**
 * Controlador de la pantalla de administración de salas. Muestra las salas en
 * una tabla con filtro por estado (activas, inactivas o todas) y, al elegir
 * una, dibuja su distribución de butacas. Permite crear, editar, desactivar y
 * restaurar salas.
 */
public class SalasController {

    private Cine cine;
    private PrincipalController principalController;
    private PersistenciaCine persistencia;

    /**
     * Carga las salas en la tabla y registra el filtro por estado. Al abrir,
     * muestra solo las activas y selecciona la primera.
     *
     * @param cine Cine que contiene las salas.
     * @param persistencia Objeto que guarda los datos en el archivo.
     */
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

    /**
     * Dibuja el plano de la sala: el nombre, la capacidad, la pantalla y las
     * filas de butacas con sus pasillos según la distribución (por ejemplo
     * 4-8-4).
     *
     * @param sala Sala que se quiere mostrar.
     */
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

    /**
     * Calcula el ancho que ocupa una fila de butacas, sumando las butacas, los
     * pasillos y el espacio entre elementos. Sirve para que la pantalla curva
     * tenga el mismo ancho que la fila.
     *
     * @param sala Sala de la que se calcula el ancho.
     * @return El ancho de la fila, en píxeles.
     */
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

    /**
     * Se ejecuta sola cuando JavaFX termina de cargar la pantalla. Define qué
     * dato muestra cada columna de la tabla, carga las opciones del filtro y
     * hace que, al elegir una sala, se dibuje su plano y el botón cambie entre
     * Desactivar y Restaurar.
     */
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

    /**
     * Guarda el controlador principal, que se usa para cambiar de pantalla y
     * actualizar el cine.
     *
     * @param principalController Controlador de la ventana principal.
     */
    public void setPrincipalController(PrincipalController principalController) {
        this.principalController = principalController;
    }

    /**
     * Crea la pantalla curva que se dibuja arriba del plano de la sala: un
     * arco con el texto PANTALLA, del mismo ancho que una fila de butacas.
     *
     * @param sala Sala de la que se toma el ancho.
     * @return El contenedor con el arco y el texto.
     */
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

    /**
     * Abre el formulario para registrar una sala nueva.
     */
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

    /**
     * Muestra un mensaje de error en la pantalla.
     *
     * @param mensaje Texto del error que se quiere mostrar.
     */
    private void mostrarError(String mensaje) {
        mensajeError.setText(mensaje);
        mensajeError.setVisible(true);
        mensajeError.setManaged(true);
    }

    /**
     * Abre el formulario para editar la sala seleccionada en la tabla. Si no
     * hay ninguna seleccionada, no hace nada.
     */
    @FXML
    private void editarSala() {

        Sala seleccionada = tablaSalas.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            System.out.println("Seleccioná una sala para editar.");
            return;
        }

        principalController.mostrarFormularioEditarSala(seleccionada);
    }

    /**
     * Desactiva la sala seleccionada, o la restaura si ya estaba inactiva.
     *
     * Trabaja sobre una copia del cine y recién cuando se guarda el archivo
     * reemplaza el cine activo. Si el cine no permite el cambio (por ejemplo,
     * por funciones asociadas), muestra el motivo en un diálogo.
     */
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

    /**
     * Muestra una alerta de advertencia con el estilo de la aplicación y un
     * botón "Entendido".
     *
     * @param mensaje Texto que se muestra en la alerta.
     */
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