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
import java.util.ArrayList;
import java.util.stream.Collectors;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;

public class CarteleraController {

    @FXML
    private FlowPane contenedorPeliculas;
    @FXML
    private TextField campoBusqueda;

    @FXML
    private ComboBox<String> filtroGenero;

    private Cine cine;
    private PrincipalController principalController;

    public void setPrincipalController(PrincipalController principalController) {
        this.principalController = principalController;
    }

    public void configurar(Cine cine) {
        this.cine = cine;

        filtroGenero.getItems().clear();
        filtroGenero.getItems().add("Todos los géneros");

        for (Pelicula pelicula : cine.getPeliculas()) {

            if (!pelicula.isActiva()) {
                continue;
            }
            filtroGenero.getItems().clear();
            filtroGenero.getItems().add("Todos los géneros");

            for (Genero genero : Genero.values()) {
                filtroGenero.getItems().add(genero.toString());
            }

            filtroGenero.setValue("Todos los géneros");

        }

        filtroGenero.setValue("Todos los géneros");

        campoBusqueda.textProperty().addListener(
                (observable, anterior, nuevo) -> mostrarPeliculas()
        );

        filtroGenero.valueProperty().addListener(
                (observable, anterior, nuevo) -> mostrarPeliculas()
        );

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

            String busqueda = campoBusqueda.getText().trim().toLowerCase();

            String generoSeleccionado = filtroGenero.getValue();

            if (!pelicula.getTituloPelicula().toLowerCase().contains(busqueda)) {
                continue;
            }

            if (generoSeleccionado != null
                    && !generoSeleccionado.equals("Todos los géneros")) {

                boolean coincideGenero = false;

                for (Genero genero : pelicula.getGeneros()) {

                    if (genero.toString().equals(generoSeleccionado)) {
                        coincideGenero = true;
                        break;
                    }
                }
                if (!coincideGenero) {
                    continue;
                }
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
                    formatearGeneros(pelicula)
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

    private String formatearGeneros(Pelicula pelicula) {

        ArrayList<Genero> generos = pelicula.getGeneros();

        if (generos.isEmpty()) {
            return "Sin género";
        }

        return generos.stream()
                .map(Genero::toString)
                .collect(Collectors.joining(", "));
    }
}
