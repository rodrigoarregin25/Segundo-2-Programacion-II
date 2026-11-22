package gestordecine;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class FuncionesController {

    private Cine cine;
    private PersistenciaCine persistencia;
    private PrincipalController principalController;

    @FXML
    private ListView<Funcion> listaFunciones;
    @FXML
    private ComboBox<String> filtroEstado;
    @FXML
    private Button botonEstado;
    @FXML
    private Label mensajeError;

    /**
     * Que hace: prepara el filtro de estado, la forma de las tarjetas y el
     * boton de estado. JavaFX lo llama solo al abrir la pantalla.
     */
    @FXML
    private void initialize() {
        filtroEstado.getItems().addAll("Activas", "Inactivas", "Todas");
        filtroEstado.setValue("Activas");

        // Cada fila de la lista se dibuja como una tarjeta
        listaFunciones.setCellFactory(lista -> new ListCell<Funcion>() {
            @Override
            protected void updateItem(Funcion funcion, boolean vacia) {
                super.updateItem(funcion, vacia);
                if (vacia || funcion == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                    return;
                }
                HBox tarjeta = crearTarjeta(funcion);
                tarjeta.setStyle(estiloTarjeta(isSelected(), funcion.isActiva()));
                tarjeta.prefWidthProperty().bind(
                        getListView().widthProperty().subtract(40));
                setGraphic(tarjeta);
                setText(null);
                setStyle("-fx-background-color: transparent; -fx-padding: 4 0 4 0;");
            }
        });

        // Al elegir una funcion se repinta la lista y se actualiza el boton de estado
        listaFunciones.getSelectionModel().selectedItemProperty()
                .addListener((observable, anterior, seleccionada) -> {
                    listaFunciones.refresh();

                    if (seleccionada == null) {
                        botonEstado.setText("Desactivar función");
                        botonEstado.setDisable(true);
                        return;
                    }

                    botonEstado.setDisable(false);
                    if (seleccionada.isActiva()) {
                        botonEstado.setText("Desactivar función");
                    } else {
                        botonEstado.setText("Restaurar función");
                    }
                });

        botonEstado.setDisable(true);
    }

    /**
     * Que hace: recibe el cine y la persistencia, y muestra las funciones segun
     * el filtro. Parametros: cine (el cine actual), persistencia (para guardar
     * los cambios).
     */
    public void configurar(Cine cine, PersistenciaCine persistencia) {
        this.cine = cine;
        this.persistencia = persistencia;

        ObservableList<Funcion> funciones
                = FXCollections.observableArrayList(cine.getFunciones());

        FilteredList<Funcion> funcionesFiltradas = new FilteredList<>(funciones);
        listaFunciones.setItems(funcionesFiltradas);

        filtroEstado.valueProperty().addListener((observable, anterior, nuevo) -> {

            funcionesFiltradas.setPredicate(funcion -> {
                if ("Activas".equals(nuevo)) {
                    return funcion.isActiva();
                }
                if ("Inactivas".equals(nuevo)) {
                    return !funcion.isActiva();
                }
                return true;
            });

            listaFunciones.getSelectionModel().clearSelection();
        });

        // Al abrir se muestran las funciones activas
        funcionesFiltradas.setPredicate(Funcion::isActiva);
    }

    public void setPrincipalController(PrincipalController principalController) {
        this.principalController = principalController;
    }

    /**
     * Que hace: arma la tarjeta de una funcion (datos a la izquierda, precio a
     * la derecha). Parametros: funcion (la funcion a mostrar). Retorna: la
     * tarjeta lista para mostrar en la lista.
     */
    private HBox crearTarjeta(Funcion funcion) {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        Label titulo = new Label(funcion.getPelicula().getTituloPelicula());
        titulo.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label detalle = new Label(funcion.getSala().getNombreSala()
                + "  ·  " + funcion.getFechaHora().format(formato));
        detalle.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 13px;");

        HBox etiquetas = new HBox(8,
                crearEtiqueta(funcion.getFormatoPelicula()),
                crearEtiqueta(funcion.getIdiomaPelicula()));

        if (!funcion.isActiva()) {
            Label inactiva = crearEtiqueta("DESACTIVADA");
            inactiva.setStyle(inactiva.getStyle()
                    + " -fx-text-fill: #FCA5A5; -fx-background-color: #3B1D24;");
            etiquetas.getChildren().add(inactiva);
        }

        VBox datos = new VBox(6, titulo, detalle, etiquetas);

        Label precio = new Label(String.format(Locale.of("es", "AR"),
                "$%,.2f", funcion.getPrecio()));
        precio.setStyle("-fx-text-fill: #60A5FA; -fx-font-size: 20px; -fx-font-weight: bold;");
        Label porEntrada = new Label("por entrada");
        porEntrada.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px;");
        VBox bloquePrecio = new VBox(2, precio, porEntrada);
        bloquePrecio.setAlignment(Pos.CENTER_RIGHT);

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        HBox tarjeta = new HBox(15, datos, espacio, bloquePrecio);
        tarjeta.setAlignment(Pos.CENTER_LEFT);

        if (!funcion.isActiva()) {
            tarjeta.setOpacity(0.65);
        }
        return tarjeta;
    }

    /**
     * Que hace: arma una etiqueta chica (por ejemplo el formato o el idioma).
     * Parametros: texto (lo que dice la etiqueta). Retorna: el Label con
     * estilo.
     */
    private Label crearEtiqueta(String texto) {
        Label etiqueta = new Label(texto);
        etiqueta.setStyle("-fx-background-color: #1E293B; -fx-text-fill: #CBD5E1;"
                + " -fx-padding: 2 10 2 10; -fx-background-radius: 10; -fx-font-size: 11px;");
        return etiqueta;
    }

    /**
     * Que hace: devuelve el estilo de la tarjeta: borde azul si esta elegida,
     * rojo si esta desactivada, gris si no. Parametros: seleccionada (true si
     * es la elegida), activa (true si esta activa). Retorna: el estilo en
     * texto.
     */
    private String estiloTarjeta(boolean seleccionada, boolean activa) {
        String borde = "#334155";
        if (seleccionada) {
            borde = "#3B82F6";
        } else if (!activa) {
            borde = "#EF4444";
        }
        return "-fx-background-color: #111C30; -fx-background-radius: 10;"
                + " -fx-border-color: " + borde + "; -fx-border-radius: 10;"
                + " -fx-border-width: 1.5; -fx-padding: 14 18 14 18;";
    }

    @FXML
    private void nuevaFuncion() {
        principalController.mostrarFormularioFuncion();
    }

    /**
     * Que hace: abre el formulario para editar el precio de la funcion elegida.
     */
    @FXML
    private void editarFuncion() {
        ocultarError();

        Funcion seleccionada = listaFunciones.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            mostrarError("Seleccione una funcion para editar");
            return;
        }
        if (!seleccionada.isActiva()) {
            mostrarError("Restaure la funcion para poder editarla");
            return;
        }

        principalController.mostrarFormularioEditarFuncion(seleccionada);
    }

    /**
     * Que hace: desactiva la funcion elegida, o la restaura si estaba
     * desactivada. No se borra nada: las reservas de la funcion quedan en el
     * historial.
     */
    @FXML
    private void cambiarEstadoFuncion() {
        ocultarError();

        Funcion seleccionada = listaFunciones.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            return;
        }

        try {
            // Se trabaja sobre una copia, igual que en Salas
            Cine cineTrabajo = cine.copiar();

            if (seleccionada.isActiva()) {
                cineTrabajo.desactivarFuncion(seleccionada.getIdFuncion());
            } else {
                cineTrabajo.reactivarFuncion(seleccionada.getIdFuncion());
            }

            persistencia.guardar(cineTrabajo);
            principalController.actualizarCine(cineTrabajo);
            principalController.mostrarFunciones();

        } catch (IllegalStateException e) {

            String titulo = seleccionada.isActiva()
                    ? "No se puede desactivar la función"
                    : "No se puede reactivar la función";

            DialogoCinema.mostrarError(
                    listaFunciones.getScene().getWindow(),
                    titulo,
                    e.getMessage()
            );

        } catch (Exception e) {

            DialogoCinema.mostrarError(
                    listaFunciones.getScene().getWindow(),
                    "Error al modificar la función",
                    e.getMessage()
            );

            e.printStackTrace();
        }
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
     * Regresa a la cartelera principal de CINEMA.
     */
    @FXML
    private void volver() {
        principalController.volver();
    }
}
