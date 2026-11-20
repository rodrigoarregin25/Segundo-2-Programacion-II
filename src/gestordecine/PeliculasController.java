package gestordecine;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class PeliculasController {

    private Cine cine;
    private PrincipalController principalController;

    public void setCine(Cine cine) {
        this.cine = cine;
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
    }

    @FXML
    private void nuevaPelicula() {
        principalController.mostrarFormularioPelicula();
    }
}
