package gestordecine.controladores;

import gestordecine.Cine;
import gestordecine.DialogoCinema;
import gestordecine.Funcion;
import gestordecine.PersistenciaCine;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Controlador de la pantalla de administración de funciones del cine.
 *
 * Muestra las funciones registradas en una tabla con información de su estado,
 * película, sala, fecha, horario, formato, idioma y precio.
 *
 * Permite filtrar las funciones por estado, película y fecha, registrar nuevas
 * funciones, editar funciones existentes y desactivarlas o reactivarlas.
 *
 * Para modificar el estado de una función, trabaja sobre una copia del cine y
 * persiste los cambios antes de actualizar el sistema.
 *
 * Se comunica con PrincipalController para gestionar la navegación y utiliza
 * Cine para aplicar las reglas de negocio.
 */
public class FuncionesController {

    // Referencias al sistema.
    private Cine cine;
    private PersistenciaCine persistencia;
    private PrincipalController principalController;

    // Lista utilizada para aplicar los filtros de la tabla.
    private FilteredList<Funcion> funcionesFiltradas;

    // Componentes de la interfaz.
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

    @FXML
    private Button botonEstado;

    @FXML
    private Label mensajeError;

    /**
     * Inicializa los componentes de la pantalla de funciones.
     *
     * Configura los filtros, las columnas de la tabla y los formatos utilizados
     * para mostrar fechas, horarios y precios.
     *
     * También establece la representación visual del estado de cada función y
     * registra un listener para actualizar el botón de activación o
     * desactivación según la selección.
     *
     * JavaFX ejecuta este método automáticamente después de cargar los
     * componentes definidos en el archivo FXML.
     */
    @FXML
    private void initialize() {

        filtroEstado.getItems().addAll(
                "Activas", "Inactivas", "Todas"
        );

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
        });

        // PELÍCULA
        colPelicula.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        datos.getValue()
                                .getPelicula()
                                .getTituloPelicula()
                )
        );

        // SALA
        colSala.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        datos.getValue()
                                .getSala()
                                .getNombreSala()
                )
        );

        // FECHA
        colFecha.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        datos.getValue()
                                .getFechaHora()
                                .format(formatoFecha)
                )
        );

        // HORA
        colHora.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        datos.getValue()
                                .getFechaHora()
                                .format(formatoHora)
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

        // Distribuir las columnas dentro de la tabla.
        listaFunciones.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        // Actualizar el botón según la función seleccionada.
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
     * Configura la pantalla con las funciones registradas en el cine.
     *
     * Obtiene las funciones y las incorpora a una lista observable que permite
     * aplicar filtros sin modificar la colección original.
     *
     * Carga los títulos de las películas sin duplicados y establece los
     * listeners que permiten combinar los filtros de estado, película y fecha.
     *
     * Finalmente, aplica los filtros iniciales a la tabla.
     *
     * @param cine Cine que contiene las funciones registradas.
     * @param persistencia Objeto encargado de guardar el estado del cine.
     */
    public void configurar(Cine cine, PersistenciaCine persistencia) {

        this.cine = cine;
        this.persistencia = persistencia;

        ObservableList<Funcion> funciones
                = FXCollections.observableArrayList(cine.getFunciones());

        funcionesFiltradas = new FilteredList<>(funciones);

        listaFunciones.setItems(funcionesFiltradas);

        // Cargar los títulos de películas sin repetir.
        filtroPelicula.getItems().clear();

        filtroPelicula.getItems().add(
                "Todas las películas"
        );

        funciones.stream()
                .map(funcion
                        -> funcion.getPelicula().getTituloPelicula())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .forEach(titulo
                        -> filtroPelicula.getItems().add(titulo));

        filtroPelicula.setValue("Todas las películas");

        filtroFecha.setValue(null);

        // Actualizar la tabla cuando cambia algún filtro.
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

    /**
     * Asigna el controlador principal de la aplicación.
     *
     * Permite navegar hacia los formularios de funciones, actualizar el cine y
     * regresar a otras pantallas.
     *
     * @param principalController Controlador principal de CINEMA.
     */
    public void setPrincipalController(
            PrincipalController principalController) {

        this.principalController = principalController;
    }

    /**
     * Abre el formulario para registrar una nueva función.
     *
     * Utiliza PrincipalController para realizar la navegación.
     */
    @FXML
    private void nuevaFuncion() {
        principalController.mostrarFormularioFuncion();
    }

    /**
     * Abre el formulario para editar la función seleccionada.
     *
     * Verifica que exista una función seleccionada, que esté activa y que su
     * fecha y hora de inicio sean futuras.
     *
     * Si alguna condición no se cumple, muestra un mensaje de error.
     *
     * Cuando la función cumple las condiciones, abre el formulario de edición
     * mediante PrincipalController.
     *
     * Las restricciones relacionadas con reservas asociadas se gestionan
     * posteriormente en FormularioFuncionController.
     */
    @FXML
    private void editarFuncion() {

        ocultarError();

        Funcion seleccionada
                = listaFunciones.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {

            mostrarError("Seleccione una funcion para editar");

            return;
        }

        if (!seleccionada.isActiva()) {

            mostrarError(
                    "Restaure la funcion para poder editarla"
            );

            return;
        }

        if (!seleccionada.getFechaHora().isAfter(LocalDateTime.now())) {

            mostrarError(
                    "No se puede editar una función que ya comenzó o terminó."
            );

            return;
        }

        principalController.mostrarFormularioEditarFuncion(
                seleccionada
        );
    }

    /**
     * Cambia el estado de la función seleccionada.
     *
     * Si la función está activa, intenta desactivarla. Si está inactiva,
     * intenta reactivarla.
     *
     * Realiza la operación sobre una copia del cine y utiliza los métodos de
     * Cine para aplicar las reglas de negocio.
     *
     * Después de guardar los cambios, actualiza el cine activo y regresa al
     * listado de funciones.
     *
     * Si la operación no está permitida, muestra un diálogo con la descripción
     * del error correspondiente.
     *
     * La desactivación no elimina las reservas del historial.
     */
    @FXML
    private void cambiarEstadoFuncion() {

        ocultarError();

        Funcion seleccionada
                = listaFunciones.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            return;
        }

        try {

            // Trabajar sobre una copia del cine.
            Cine cineTrabajo = cine.copiar();

            if (seleccionada.isActiva()) {

                cineTrabajo.desactivarFuncion(
                        seleccionada.getIdFuncion()
                );

            } else {

                cineTrabajo.reactivarFuncion(
                        seleccionada.getIdFuncion()
                );
            }

            // Persistir antes de actualizar el cine activo.
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

    /**
     * Limpia los filtros de película y fecha.
     *
     * Restablece la selección de todas las películas y elimina la fecha
     * elegida.
     *
     * Conserva el filtro de estado seleccionado.
     */
    @FXML
    private void limpiarFiltros() {

        filtroPelicula.setValue("Todas las películas");

        filtroFecha.setValue(null);
    }

    /**
     * Regresa a la pantalla anterior mediante el controlador principal de la
     * aplicación.
     */
    @FXML
    private void volver() {
        principalController.volver();
    }

    /**
     * Aplica los filtros combinados sobre las funciones de la tabla.
     *
     * Comprueba el estado, el título de la película y la fecha seleccionados
     * por el usuario.
     *
     * Los criterios se aplican simultáneamente: una función solamente se
     * muestra si cumple todos los filtros activos.
     *
     * Si no se especifica una película o fecha, no se aplica ninguna
     * restricción para ese criterio.
     *
     * Al finalizar, limpia la selección actual de la tabla.
     */
    private void aplicarFiltros() {

        if (funcionesFiltradas == null) {
            return;
        }

        String estado = filtroEstado.getValue();

        String peliculaSeleccionada
                = filtroPelicula.getValue();

        LocalDate fechaSeleccionada
                = filtroFecha.getValue();

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
     * Muestra un mensaje de error dentro de la pantalla.
     *
     * Asigna el texto recibido a la etiqueta correspondiente y habilita su
     * visualización.
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
