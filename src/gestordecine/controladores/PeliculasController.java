package gestordecine.controladores;

import gestordecine.Cine;
import gestordecine.DialogoCinema;
import gestordecine.Genero;
import gestordecine.Pelicula;
import gestordecine.PersistenciaCine;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

/**
 * Controlador de la pantalla de administración de películas. Muestra las
 * películas en una tabla con filtro por estado (activas, inactivas o todas) y
 * permite crear, editar, desactivar y restaurar películas.
 */
public class PeliculasController {

    private Cine cine;
    private PrincipalController principalController;
    private PersistenciaCine persistencia;

    /**
     * Carga las películas en la tabla y registra el filtro por estado. Al
     * abrir, muestra solo las activas.
     *
     * @param cine Cine que contiene las películas.
     * @param persistencia Objeto que guarda los datos en el archivo.
     */
    public void configurar(Cine cine, PersistenciaCine persistencia) {
        this.cine = cine;
        this.persistencia = persistencia;
        ObservableList<Pelicula> peliculas
                = FXCollections.observableArrayList(cine.getPeliculas());

        FilteredList<Pelicula> peliculasFiltradas
                = new FilteredList<>(peliculas);

        tablaPeliculas.setItems(peliculasFiltradas);

        filtroEstado.valueProperty().addListener((observable, anterior, nuevo) -> {
            peliculasFiltradas.setPredicate(pelicula -> {
                if ("Activas".equals(nuevo)) {
                    return pelicula.isActiva();
                }

                if ("Inactivas".equals(nuevo)) {
                    return !pelicula.isActiva();
                }

                return true;
            });
        });

        filtroEstado.setValue("Activas");
        peliculasFiltradas.setPredicate(Pelicula::isActiva);
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

    @FXML
    private TableView<Pelicula> tablaPeliculas;

    @FXML
    private TableColumn<Pelicula, String> colTitulo;

    @FXML
    private TableColumn<Pelicula, Integer> colDuracion;

    @FXML
    private TableColumn<Pelicula, String> colEstado;

    @FXML
    private TableColumn<Pelicula, String> colDirector;

    @FXML
    private TableColumn<Pelicula, String> colClasificacion;

    @FXML
    private TableColumn<Pelicula, String> colPuntuacion;

    @FXML
    private Label mensajeEstado;

    @FXML
    private TableColumn<Pelicula, Integer> colFechaEstreno;

    @FXML
    private TableColumn<Pelicula, String> colGenero;

    @FXML
    private ComboBox<String> filtroEstado;

    @FXML
    private Button botonEstado;

    /**
     * Se ejecuta sola cuando JavaFX termina de cargar la pantalla. Define qué
     * dato muestra cada columna de la tabla, carga las opciones del filtro y
     * cambia el texto del botón según la película seleccionada (Desactivar o
     * Restaurar).
     */
    @FXML
    private void initialize() {

        colTitulo.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        datos.getValue().getTituloPelicula()
                )
        );

        colDuracion.setCellValueFactory(datos
                -> new SimpleIntegerProperty(
                        datos.getValue().getDuracionPelicula()
                ).asObject()
        );

        colEstado.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        datos.getValue().isActiva() ? "Activa" : "Inactiva"
                )
        );
        colDirector.setCellValueFactory(
                datos -> new SimpleStringProperty(
                        datos.getValue().getDirector()
                )
        );

        colClasificacion.setCellValueFactory(
                datos -> new SimpleStringProperty(
                        datos.getValue().getClasificacion()
                )
        );

        colPuntuacion.setCellValueFactory(
                datos -> new SimpleStringProperty(
                        String.format("%.1f/10", datos.getValue().getPuntuacion())
                )
        );

        colEstado.setCellFactory(columna -> new TableCell<Pelicula, String>() {

            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);

                if (empty || estado == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                boolean activa = estado.equals("Activa");

                setText(activa ? "A" : "✕");

                getStyleClass().removeAll("estado-activo", "estado-inactivo");

                getStyleClass().add(
                        activa ? "estado-activo" : "estado-inactivo"
                );

                setAlignment(Pos.CENTER_LEFT);
            }
        });

        colFechaEstreno.setCellValueFactory(datos
                -> new SimpleIntegerProperty(
                        datos.getValue().getAnioEstreno()
                ).asObject()
        );

        colGenero.setCellValueFactory(
                datos -> new SimpleStringProperty(
                        formatearGeneros(datos.getValue())
                )
        );

        filtroEstado.getItems().addAll("Activas", "Inactivas", "Todas");
        filtroEstado.setValue("Activas");

        tablaPeliculas.getSelectionModel().selectedItemProperty()
                .addListener((observable, anterior, seleccionada) -> {

                    if (seleccionada == null) {
                        botonEstado.setDisable(true);
                        botonEstado.setText("Desactivar Película");
                        return;
                    }

                    botonEstado.setDisable(false);

                    if (seleccionada.isActiva()) {
                        botonEstado.setText("Desactivar Película");
                    } else {
                        botonEstado.setText("Restaurar Película");
                    }
                });

        // Identificar la columna de estado para alinear
        // su encabezado y contenido de la misma manera.
        colEstado.getStyleClass().add("columna-estado-admin");

        // Aprovechar el ancho disponible de la tabla.
        tablaPeliculas.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );
    }

    /**
     * Abre el formulario para registrar una película nueva.
     */
    @FXML
    private void nuevaPelicula() {
        principalController.mostrarFormularioPelicula();
    }

    /**
     * Abre el formulario para editar la película seleccionada en la tabla. Si
     * no hay ninguna seleccionada, muestra un aviso.
     */
    @FXML
    private void editarPelicula() {

        Pelicula seleccionada = tablaPeliculas.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            mostrarMensaje("Seleccioná una película para editar.");
            return;
        }

        principalController.mostrarFormularioEditarPelicula(seleccionada);
    }

    /**
     * Desactiva la película seleccionada, o la restaura si ya estaba inactiva.
     *
     * Trabaja sobre una copia del cine y recién cuando se guarda el archivo
     * reemplaza el cine activo. Si el cine no permite el cambio (por ejemplo,
     * por funciones asociadas), muestra el motivo en un diálogo.
     */
    @FXML
    private void desactivarPelicula() {

        Pelicula seleccionada = tablaPeliculas.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            mostrarMensaje("Seleccioná una película.");
            return;
        }

        try {
            Cine cineTrabajo = cine.copiar();

            if (seleccionada.isActiva()) {
                cineTrabajo.desactivarPelicula(seleccionada.getIdPelicula());
            } else {
                cineTrabajo.reactivarPelicula(seleccionada.getIdPelicula());
            }

            persistencia.guardar(cineTrabajo);

            principalController.actualizarCine(cineTrabajo);
            principalController.mostrarPeliculas();

        } catch (IllegalStateException e) {

            DialogoCinema.mostrarError(
                    tablaPeliculas.getScene().getWindow(),
                    "No se puede desactivar la película",
                    e.getMessage()
            );
        } catch (Exception e) {
            mostrarMensaje("Error al cambiar el estado de la película: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Muestra un mensaje en la pantalla.
     *
     * @param mensaje Texto que se quiere mostrar.
     */
    private void mostrarMensaje(String mensaje) {
        mensajeEstado.setText(mensaje);
        mensajeEstado.setVisible(true);
        mensajeEstado.setManaged(true);
    }

    /**
     * Arma un texto con los géneros de una película, separados por coma. Si no
     * tiene géneros, devuelve "Sin género".
     *
     * @param pelicula Película de la que se quieren los géneros.
     * @return Los géneros como texto, por ejemplo "Acción, Drama".
     */
    private String formatearGeneros(Pelicula pelicula) {

        if (pelicula.getGeneros().isEmpty()) {
            return "Sin género";
        }

        return pelicula.getGeneros().stream()
                .map(Genero::toString)
                .collect(java.util.stream.Collectors.joining(", "));
    }

    /**
     * Regresa a la cartelera principal de CINEMA.
     */
    @FXML
    private void volver() {
        principalController.volver();
    }

}