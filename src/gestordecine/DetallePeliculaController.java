package gestordecine;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.scene.image.Image;

public class DetallePeliculaController {

    @FXML
    private ImageView imagenPoster;

    @FXML
    private Label tituloPelicula;

    @FXML
    private Label informacionPelicula;

    @FXML
    private Label puntuacionPelicula;

    @FXML
    private Label sinopsisPelicula;

    @FXML
    private Label directorPelicula;

    @FXML
    private Label actoresPelicula;

    @FXML
    private Label clasificacionPelicula;

    private PrincipalController principalController;

    public void setPrincipalController(PrincipalController principalController) {
        this.principalController = principalController;
    }

    @FXML
    private void volverCartelera() {
        principalController.mostrarCartelera();
    }

    public void configurar(Pelicula pelicula) {

        tituloPelicula.setText(pelicula.getTituloPelicula());

        informacionPelicula.setText(
                pelicula.getAnioEstreno()
                + " · " + pelicula.getGenero()
                + " · " + pelicula.getDuracionPelicula() + " min"
        );

        puntuacionPelicula.setText(
                pelicula.getPuntuacion() > 0
                ? "★ " + pelicula.getPuntuacion() + "/10"
                : "Sin puntuación"
        );

        sinopsisPelicula.setText(pelicula.getSinopsis());

        directorPelicula.setText(
                "Director: " + pelicula.getDirector()
        );

        actoresPelicula.setText(
                "Actores: " + pelicula.getActores()
        );

        clasificacionPelicula.setText(
                "Clasificación: " + pelicula.getClasificacion()
        );

        Path rutaPoster = Paths.get("posters", pelicula.getPoster());

        if (Files.exists(rutaPoster)) {
            Image imagen = new Image(
                    rutaPoster.toUri().toString(),
                    true
            );

            imagenPoster.setImage(imagen);
        }
    }
}
