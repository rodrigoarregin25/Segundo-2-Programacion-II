package gestordecine;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class PrincipalController {

    @FXML
    private Label tituloPagina;

    @FXML
    private void mostrarPeliculas() {
        tituloPagina.setText("Administración de películas");
    }
}