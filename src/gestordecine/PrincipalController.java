package gestordecine;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

public class PrincipalController {

    private Cine cine;

    public void setCine(Cine cine) {
        this.cine = cine;
    }

    @FXML
    private BorderPane contenedorPrincipal;

    @FXML
    private void mostrarPeliculas() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("Peliculas.fxml")
            );

            Parent pantalla = loader.load();

            PeliculasController controlador = loader.getController();
            controlador.setCine(cine);
            controlador.setPrincipalController(this);

            contenedorPrincipal.setCenter(pantalla);

        } catch (IOException e) {
            System.out.println("Error al cargar la pantalla de peliculas: "
                    + e.getMessage());
        }
    }

    public void mostrarFormularioPelicula() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("FormularioPelicula.fxml")
            );

            Parent pantalla = loader.load();

            contenedorPrincipal.setCenter(pantalla);

        } catch (IOException e) {
            System.out.println("Error al cargar el formulario: "
                    + e.getMessage());
        }
    }
}
