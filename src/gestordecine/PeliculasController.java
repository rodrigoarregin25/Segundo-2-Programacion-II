package gestordecine;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.geometry.Pos;

public class PeliculasController {

    private Cine cine;
    private PrincipalController principalController;
    private PersistenciaCine persistencia;

    public void configurar(Cine cine, PersistenciaCine persistencia) {
        this.cine = cine;
        this.persistencia = persistencia;
        tablaPeliculas.getItems().setAll(cine.getPeliculas());
    }

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
    private Label mensajeEstado;

    @FXML
    private TableColumn<Pelicula, Integer> colFechaEstreno;

    @FXML
    private TableColumn<Pelicula, String> colGenero;

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

                setText(activa ? "✓" : "✕");

                getStyleClass().removeAll("estado-activo", "estado-inactivo");

                getStyleClass().add(
                        activa ? "estado-activo" : "estado-inactivo"
                );

                setAlignment(Pos.CENTER);
            }
        });

        colFechaEstreno.setCellValueFactory(datos
                -> new SimpleIntegerProperty(
                        datos.getValue().getAnioEstreno()
                ).asObject()
        );

        colGenero.setCellValueFactory(datos
                -> new SimpleStringProperty(
                        datos.getValue().getGenero()
                )
        );
    }

    @FXML
    private void nuevaPelicula() {
        principalController.mostrarFormularioPelicula();
    }

    @FXML
    private void editarPelicula() {

        Pelicula seleccionada = tablaPeliculas.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            mostrarMensaje("Seleccioná una película para editar.");
            return;
        }

        principalController.mostrarFormularioEditarPelicula(seleccionada);
    }

    @FXML
    private void desactivarPelicula() {

        Pelicula seleccionada = tablaPeliculas.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            mostrarMensaje("Seleccioná una película.");
            return;
        }

        if (!seleccionada.isActiva()) {
            mostrarMensaje("La película ya está desactivada.");
            return;
        }

        try {
            cine.desactivarPelicula(seleccionada.getIdPelicula());

            persistencia.guardar(cine);

            tablaPeliculas.refresh();

        } catch (Exception e) {
            mostrarMensaje("Error al desactivar la película: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void mostrarMensaje(String mensaje) {
        mensajeEstado.setText(mensaje);
        mensajeEstado.setVisible(true);
    }
}
