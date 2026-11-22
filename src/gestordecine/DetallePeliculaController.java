package gestordecine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class DetallePeliculaController {

    private List<Funcion> funcionesDisponibles;
    private LocalDate fechaSeleccionada;

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

    @FXML
    private FlowPane contenedorFunciones;

    @FXML
    private Label mensajeSinFunciones;

    @FXML
    private FlowPane contenedorFechas;

    @FXML
    private VBox seccionHorarios;

    private PrincipalController principalController;

    public void setPrincipalController(PrincipalController principalController) {
        this.principalController = principalController;
    }

    @FXML
    private void volverCartelera() {
        principalController.mostrarCartelera();
    }

    public void configurar(Pelicula pelicula, Cine cine) {

        tituloPelicula.setText(pelicula.getTituloPelicula());

        informacionPelicula.setText(
                pelicula.getAnioEstreno()
                + " · " + formatearGeneros(pelicula)
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

// Las funciones deben cargarse siempre, exista o no el póster.
        mostrarFechas(pelicula, cine);
    }

    private String formatearGeneros(Pelicula pelicula) {

        if (pelicula.getGeneros().isEmpty()) {
            return "Sin género";
        }

        return pelicula.getGeneros().stream()
                .map(Genero::toString)
                .collect(java.util.stream.Collectors.joining(", "));
    }

    /**
     * Que hace: muestra los horarios de la fecha elegida. Al hacer clic en un
     * horario se abre Nueva reserva con esa funcion ya elegida.
     */
    private void mostrarHorarios() {

        contenedorFunciones.getChildren().clear();

        List<Funcion> funcionesDelDia = funcionesDisponibles.stream()
                .filter(funcion
                        -> funcion.getFechaHora().toLocalDate().equals(fechaSeleccionada))
                .toList();

        mensajeSinFunciones.setVisible(funcionesDelDia.isEmpty());
        mensajeSinFunciones.setManaged(funcionesDelDia.isEmpty());

        DateTimeFormatter formatoHora
                = DateTimeFormatter.ofPattern("HH:mm");

        for (Funcion funcion : funcionesDelDia) {

            VBox tarjeta = new VBox(7);
            tarjeta.getStyleClass().add("tarjeta-horario");
            tarjeta.setCursor(Cursor.HAND);

            Label hora = new Label(
                    funcion.getFechaHora().format(formatoHora)
            );
            hora.getStyleClass().add("hora-funcion");

            Label formato = new Label(
                    funcion.getFormatoPelicula()
                    + " · " + funcion.getIdiomaPelicula()
            );
            formato.getStyleClass().add("detalle-horario");

            Label sala = new Label(
                    funcion.getSala().getNombreSala()
            );
            sala.getStyleClass().add("detalle-horario");

            Label precio = new Label(
                    String.format(Locale.of("es", "AR"), "$%,.2f", funcion.getPrecio())
            );
            precio.getStyleClass().add("precio-horario");

            tarjeta.getChildren().addAll(hora, formato, sala, precio);

            // Al elegir el horario se va a Nueva reserva con esta funcion
            tarjeta.setOnMouseClicked(evento
                    -> principalController.mostrarNuevaReservaConFuncion(funcion));

            contenedorFunciones.getChildren().add(tarjeta);
        }
    }

    private void mostrarFechas(Pelicula pelicula, Cine cine) {

        contenedorFechas.getChildren().clear();

        // Obtener las funciones futuras y activas de la película
        funcionesDisponibles = cine.getFunciones().stream()
                .filter(funcion
                        -> funcion.getPelicula().getIdPelicula()
                == pelicula.getIdPelicula())
                .filter(funcion
                        -> funcion.getFechaHora().isAfter(LocalDateTime.now()))
                .filter(funcion -> funcion.isActiva())
                .filter(funcion -> funcion.getSala().isActiva())
                .sorted(Comparator.comparing(Funcion::getFechaHora))
                .toList();

        // Obtener fechas únicas, ordenadas
        List<LocalDate> fechas = funcionesDisponibles.stream()
                .map(funcion -> funcion.getFechaHora().toLocalDate())
                .distinct()
                .toList();

        // Mostrar u ocultar la sección según existan funciones
        boolean hayFunciones = !funcionesDisponibles.isEmpty();

        seccionHorarios.setVisible(hayFunciones);
        seccionHorarios.setManaged(hayFunciones);

        mensajeSinFunciones.setVisible(!hayFunciones);
        mensajeSinFunciones.setManaged(!hayFunciones);

        DateTimeFormatter formatoDia
                = DateTimeFormatter.ofPattern("EEE", Locale.of("es", "AR"));

        DateTimeFormatter formatoNumero
                = DateTimeFormatter.ofPattern("dd");

        for (LocalDate fecha : fechas) {

            String dia = fecha.format(formatoDia).toUpperCase();
            String numero = fecha.format(formatoNumero);

            Button botonFecha = new Button(dia + "\n" + numero);
            botonFecha.getStyleClass().add("boton-fecha");

            botonFecha.setOnAction(event -> {
                fechaSeleccionada = fecha;
                mostrarHorarios();
            });

            contenedorFechas.getChildren().add(botonFecha);
        }

        // Seleccionar automáticamente la primera fecha disponible
        if (!fechas.isEmpty()) {
            fechaSeleccionada = fechas.get(0);
            mostrarHorarios();
        } else {
            fechaSeleccionada = null;
            contenedorFunciones.getChildren().clear();
            mensajeSinFunciones.setVisible(true);
            mensajeSinFunciones.setManaged(true);
        }
    }
}
