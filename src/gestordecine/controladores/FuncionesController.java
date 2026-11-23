package gestordecine.controladores;

import gestordecine.Cine;
import gestordecine.DialogoCinema;
import gestordecine.Funcion;
import gestordecine.PersistenciaCine;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.time.LocalDate;
import javafx.scene.control.DatePicker;

public class FuncionesController {

    private Cine cine;
    private PersistenciaCine persistencia;
    private PrincipalController principalController;

    @FXML
    private TableView<Funcion> listaFunciones;

    @FXML
    private TableColumn<Funcion, String> colEstado;

    @FXML
    private TableColumn<Funcion, String> colPelicula;

    @FXML
    private TableColumn<Funcion, String> colSala;

    @FXML
    private TableColumn<Funcion, String> colFecha;

    @FXML
    private TableColumn<Funcion, String> colHora;

    @FXML
    private TableColumn<Funcion, String> colFormato;

    @FXML
    private TableColumn<Funcion, String> colIdioma;

    @FXML
    private TableColumn<Funcion, String> colPrecio;

    @FXML
    private ComboBox<String> filtroEstado;

    @FXML
    private ComboBox<String> filtroPelicula;

    @FXML
    private DatePicker filtroFecha;

    private FilteredList<Funcion> funcionesFiltradas;
    @FXML
    private Button botonEstado;
    @FXML
    private Label mensajeError;

    /**
     * Configura las columnas de la tabla de funciones, el filtro de estado y
     * las acciones disponibles para la función seleccionada.
     */
    @FXML
    private void initialize() {

        filtroEstado.getItems().addAll("Activas", "Inactivas", "Todas");
        filtroEstado.setValue("Activas");

        botonEstado.setDisable(true);

        DateTimeFormatter formatoFecha
                = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        DateTimeFormatter formatoHora
                = DateTimeFormatter.ofPattern("HH:mm");

        // ESTADO
        colEstado.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        datos.getValue().isActiva()
                        ? "Activa" : "Inactiva"
                )
        );

        colEstado.setCellFactory(columna
                -> new TableCell<Funcion, String>() {

            @Override
            protected void updateItem(String estado, boolean vacia) {

                super.updateItem(estado, vacia);

                getStyleClass().removeAll(
                        "estado-activo", "estado-inactivo"
                );

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
        }
        );

        // PELÍCULA
        colPelicula.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        datos.getValue().getPelicula().getTituloPelicula()
                )
        );

        // SALA
        colSala.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        datos.getValue().getSala().getNombreSala()
                )
        );

        // FECHA
        colFecha.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        datos.getValue().getFechaHora().format(formatoFecha)
                )
        );

        // HORA
        colHora.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        datos.getValue().getFechaHora().format(formatoHora)
                )
        );

        // FORMATO
        colFormato.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        datos.getValue().getFormatoPelicula()
                )
        );

        // IDIOMA
        colIdioma.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        datos.getValue().getIdiomaPelicula()
                )
        );

        // PRECIO
        colPrecio.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        String.format(
                                Locale.of("es", "AR"),
                                "$%,.2f",
                                datos.getValue().getPrecio()
                        )
                )
        );

        // DISTRIBUIR LAS COLUMNAS
        listaFunciones.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        // ACTUALIZAR BOTÓN SEGÚN LA SELECCIÓN
        listaFunciones.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, anterior, seleccionada) -> {

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

        listaFunciones.setPlaceholder(
                new Label("No hay funciones para mostrar")
        );
    }

    /**
     * Configura la pantalla y prepara los filtros combinados de estado,
     * película y fecha.
     *
     * @param cine Cine con las funciones registradas.
     * @param persistencia Administrador de persistencia.
     */
    public void configurar(Cine cine, PersistenciaCine persistencia) {

        this.cine = cine;
        this.persistencia = persistencia;

        ObservableList<Funcion> funciones
                = FXCollections.observableArrayList(cine.getFunciones());

        funcionesFiltradas = new FilteredList<>(funciones);
        listaFunciones.setItems(funcionesFiltradas);

        // Cargar títulos de películas sin repetir.
        filtroPelicula.getItems().clear();
        filtroPelicula.getItems().add("Todas las películas");

        funciones.stream()
                .map(funcion -> funcion.getPelicula().getTituloPelicula())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .forEach(titulo -> filtroPelicula.getItems().add(titulo));

        filtroPelicula.setValue("Todas las películas");
        filtroFecha.setValue(null);

        // Combinar todos los criterios en una sola búsqueda.
        filtroEstado.valueProperty().addListener(
                (observable, anterior, nuevo) -> aplicarFiltros()
        );

        filtroPelicula.valueProperty().addListener(
                (observable, anterior, nuevo) -> aplicarFiltros()
        );

        filtroFecha.valueProperty().addListener(
                (observable, anterior, nuevo) -> aplicarFiltros()
        );

        aplicarFiltros();
    }

    public void setPrincipalController(PrincipalController principalController) {
        this.principalController = principalController;
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

    /**
     * Filtra las funciones según su estado, película y fecha.
     *
     * Los criterios se aplican simultáneamente.
     */
    private void aplicarFiltros() {

        if (funcionesFiltradas == null) {
            return;
        }

        String estado = filtroEstado.getValue();
        String peliculaSeleccionada = filtroPelicula.getValue();
        LocalDate fechaSeleccionada = filtroFecha.getValue();

        funcionesFiltradas.setPredicate(funcion -> {

            // FILTRO DE ESTADO
            if ("Activas".equals(estado) && !funcion.isActiva()) {
                return false;
            }

            if ("Inactivas".equals(estado) && funcion.isActiva()) {
                return false;
            }

            // FILTRO DE PELÍCULA
            if (peliculaSeleccionada != null
                    && !peliculaSeleccionada.equals("Todas las películas")
                    && !funcion.getPelicula().getTituloPelicula()
                            .equals(peliculaSeleccionada)) {

                return false;
            }

            // FILTRO DE FECHA
            if (fechaSeleccionada != null
                    && !funcion.getFechaHora().toLocalDate()
                            .equals(fechaSeleccionada)) {

                return false;
            }

            return true;
        });

        listaFunciones.getSelectionModel().clearSelection();
    }

    /**
     * Limpia los filtros de película y fecha.
     *
     * Conserva el filtro de estado seleccionado.
     */
    @FXML
    private void limpiarFiltros() {

        filtroPelicula.setValue("Todas las películas");
        filtroFecha.setValue(null);
    }
}
