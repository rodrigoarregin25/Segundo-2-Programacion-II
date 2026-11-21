package gestordecine;

import javafx.fxml.FXML;
import javafx.scene.layout.FlowPane;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;

public class CarteleraController {

    @FXML
    private FlowPane contenedorPeliculas;

    private Cine cine;
    private PrincipalController principalController;

    public void setPrincipalController(PrincipalController principalController) {
        this.principalController = principalController;
    }

    public void configurar(Cine cine) {
        this.cine = cine;
        mostrarPeliculas();
    }

    private void mostrarPeliculas() {

        contenedorPeliculas.getChildren().clear();
        Region espacio = new Region();
        VBox.setVgrow(espacio, Priority.ALWAYS);

        for (Pelicula pelicula : cine.getPeliculas()) {

            if (!pelicula.isActiva()) {
                continue;
            }

            VBox tarjeta = new VBox(10);
            tarjeta.getStyleClass().add("tarjeta-pelicula");

            tarjeta.setOnMouseClicked(event -> {
                principalController.mostrarDetallePelicula(pelicula);
            });

            ImageView imagenPoster = new ImageView();
            imagenPoster.setFitWidth(200);
            imagenPoster.setFitHeight(280);
            imagenPoster.setPreserveRatio(false);

            Path rutaPoster = Paths.get("posters", pelicula.getPoster());

            if (Files.exists(rutaPoster)) {
                Image imagen = new Image(
                        rutaPoster.toUri().toString(),
                        true
                );

                imagenPoster.setImage(imagen);
            }

            Label titulo = new Label(pelicula.getTituloPelicula());
            titulo.getStyleClass().add("titulo-tarjeta-pelicula");
            titulo.setWrapText(true);
            titulo.setMinHeight(45);
            titulo.setPrefHeight(45);
            titulo.setMaxHeight(45);

            Label informacion = new Label(
                    pelicula.getGenero()
                    + " · " + pelicula.getDuracionPelicula() + " min"
            );
            informacion.getStyleClass().add("info-tarjeta-pelicula");
            informacion.setWrapText(true);
            informacion.setMinHeight(35);
            informacion.setPrefHeight(35);
            informacion.setMaxHeight(35);

            Label puntuacion = new Label(
                    "★ " + pelicula.getPuntuacion() + "/10"
            );
            puntuacion.getStyleClass().add("puntuacion-pelicula");

            tarjeta.getChildren().addAll(
                    imagenPoster,
                    titulo,
                    informacion,
                    espacio,
                    puntuacion
            );

            contenedorPeliculas.getChildren().add(tarjeta);
        }
    }
}
