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
import java.time.LocalDate;
import java.time.LocalDateTime;
import javafx.scene.control.DatePicker;
import javafx.scene.control.DateCell;

public class CarteleraController {

    @FXML
    private FlowPane contenedorPeliculas;
    @FXML
    private TextField campoBusqueda;

    @FXML
    private ComboBox<String> filtroGenero;

    @FXML
    private DatePicker filtroFecha;

    private Cine cine;
    private PrincipalController principalController;

    public void setPrincipalController(PrincipalController principalController) {
        this.principalController = principalController;
    }

    /**
     * Configura la cartelera, carga los géneros disponibles y registra los
     * filtros de búsqueda por título, género y fecha.
     *
     * @param cine Cine que contiene las películas y funciones.
     */
    public void configurar(Cine cine) {
        this.cine = cine;

        filtroGenero.getItems().clear();
        filtroGenero.getItems().add("Todos los géneros");

        for (Genero genero : Genero.values()) {
            filtroGenero.getItems().add(genero.toString());
        }

        filtroGenero.setValue("Todos los géneros");

        campoBusqueda.textProperty().addListener(
                (observable, anterior, nuevo) -> mostrarPeliculas()
        );

        filtroGenero.valueProperty().addListener(
                (observable, anterior, nuevo) -> mostrarPeliculas()
        );

        filtroFecha.valueProperty().addListener(
                (observable, anterior, nuevo) -> mostrarPeliculas()
        );

        // Impedir seleccionar fechas anteriores al día actual.
        filtroFecha.setDayCellFactory(datePicker -> new DateCell() {

            @Override
            public void updateItem(LocalDate fecha, boolean vacio) {
                super.updateItem(fecha, vacio);

                if (vacio || fecha.isBefore(LocalDate.now())) {
                    setDisable(true);
                }
            }
        });

        mostrarPeliculas();
    }

    /**
     * Muestra las películas activas que cumplen con los filtros de título,
     * género y fecha seleccionados.
     *
     * Si se selecciona una fecha, solamente muestra películas que tengan al
     * menos una función futura y activa ese día, en una sala activa.
     */
    private void mostrarPeliculas() {

        contenedorPeliculas.getChildren().clear();

        String busqueda = campoBusqueda.getText().trim().toLowerCase();
        String generoSeleccionado = filtroGenero.getValue();
        LocalDate fechaSeleccionada = filtroFecha.getValue();

        for (Pelicula pelicula : cine.getPeliculas()) {

            // Mostrar solamente películas activas.
            if (!pelicula.isActiva()) {
                continue;
            }

            // FILTRO POR TÍTULO
            if (!pelicula.getTituloPelicula().toLowerCase().contains(busqueda)) {
                continue;
            }

            // FILTRO POR GÉNERO
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

            // FILTRO POR FECHA
            if (fechaSeleccionada != null) {

                boolean tieneFuncionDisponible = false;
                LocalDateTime ahora = LocalDateTime.now();

                for (Funcion funcion : cine.buscarFuncionesPorFecha(fechaSeleccionada)) {

                    boolean mismaPelicula
                            = funcion.getPelicula().getIdPelicula()
                            == pelicula.getIdPelicula();

                    boolean funcionActiva = funcion.isActiva();

                    boolean salaActiva = funcion.getSala().isActiva();

                    boolean funcionFutura = funcion.getFechaHora().isAfter(ahora);

                    if (mismaPelicula && funcionActiva
                            && salaActiva && funcionFutura) {

                        tieneFuncionDisponible = true;
                        break;
                    }
                }

                if (!tieneFuncionDisponible) {
                    continue;
                }
            }

            // CREAR TARJETA DE PELÍCULA
            VBox tarjeta = new VBox(10);
            tarjeta.getStyleClass().add("tarjeta-pelicula");

            tarjeta.setOnMouseClicked(event -> {
                principalController.mostrarDetallePelicula(pelicula);
            });

            // PÓSTER
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

            // TÍTULO
            Label titulo = new Label(pelicula.getTituloPelicula());
            titulo.getStyleClass().add("titulo-tarjeta-pelicula");
            titulo.setWrapText(true);
            titulo.setMinHeight(45);
            titulo.setPrefHeight(45);
            titulo.setMaxHeight(45);

            // INFORMACIÓN
            Label informacion = new Label(
                    formatearGeneros(pelicula)
                    + " · " + pelicula.getDuracionPelicula() + " min"
            );

            informacion.getStyleClass().add("info-tarjeta-pelicula");
            informacion.setWrapText(true);
            informacion.setMinHeight(35);
            informacion.setPrefHeight(35);
            informacion.setMaxHeight(35);

            // PUNTUACIÓN
            Label puntuacion = new Label(
                    "★ " + pelicula.getPuntuacion() + "/10"
            );
            puntuacion.getStyleClass().add("puntuacion-pelicula");

            // ESPACIO FLEXIBLE INDIVIDUAL PARA CADA TARJETA
            Region espacio = new Region();
            VBox.setVgrow(espacio, Priority.ALWAYS);

            // AGREGAR ELEMENTOS A LA TARJETA
            tarjeta.getChildren().addAll(
                    imagenPoster,
                    titulo,
                    informacion,
                    espacio,
                    puntuacion
            );

            // AGREGAR TARJETA A LA CARTELERA
            contenedorPeliculas.getChildren().add(tarjeta);
        }

        // Si ninguna pelicula cumplio los filtros, se avisa en pantalla
        if (contenedorPeliculas.getChildren().isEmpty()) {
            Label sinResultados = new Label("No hay funciones para estos filtros");
            sinResultados.setStyle(
                    "-fx-text-fill: #94A3B8;"
                    + "-fx-font-size: 14px;"
            );
            contenedorPeliculas.getChildren().add(sinResultados);
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

    /**
     * Elimina el filtro de fecha y vuelve a mostrar las películas que cumplen
     * con los demás filtros seleccionados.
     */
    @FXML
    private void limpiarFecha() {
        filtroFecha.setValue(null);
        mostrarPeliculas();
    }
}