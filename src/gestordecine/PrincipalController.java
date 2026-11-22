package gestordecine;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import gestordecine.misreservas.MisReservasController;
import gestordecine.sesion.AccesoController;
import gestordecine.sesion.SesionCliente;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Effect;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.control.Button;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;

public class PrincipalController {

    private Cine cine;
    private PersistenciaCine persistencia;

    public void setCine(Cine cine) {
        this.cine = cine;
        this.persistencia = persistencia;
    }

    @FXML
    private BorderPane contenedorPrincipal;

    @FXML
    private Button botonMisReservas;

    @FXML
    private Button botonIniciarSesion;

    @FXML
    private Button botonCerrarSesion;

    @FXML
    private Label etiquetaSesion;

    @FXML
    public void mostrarPeliculas() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("Peliculas.fxml")
            );

            Parent pantalla = loader.load();

            PeliculasController controlador = loader.getController();
            controlador.configurar(cine, persistencia);
            controlador.setPrincipalController(this);

            contenedorPrincipal.setCenter(pantalla);

        } catch (IOException e) {
            System.out.println("Error al cargar la pantalla de peliculas: "
                    + e.getMessage());
        }
    }

    public void configurar(Cine cine, PersistenciaCine persistencia) {
        this.cine = cine;
        this.persistencia = persistencia;
        actualizarMenuCuenta();
    }

    public void actualizarCine(Cine cineActualizado) {
        this.cine = cineActualizado;
    }

    public void recuperarCineGuardado() throws IOException, ClassNotFoundException {

        this.cine = persistencia.cargarOCrear();
    }

    public void mostrarFormularioPelicula() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("FormularioPelicula.fxml")
            );

            Parent pantalla = loader.load();

            FormularioPeliculaController controlador = loader.getController();

            controlador.configurar(cine, persistencia, this);

            contenedorPrincipal.setCenter(pantalla);

        } catch (IOException e) {
            System.out.println("Error al cargar el formulario: "
                    + e.getMessage());
        }
    }

    public void mostrarFormularioEditarPelicula(Pelicula pelicula) {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("FormularioPelicula.fxml")
            );

            Parent pantalla = loader.load();

            FormularioPeliculaController controlador = loader.getController();

            controlador.configurar(cine, persistencia, this);
            controlador.configurarEdicion(pelicula);

            contenedorPrincipal.setCenter(pantalla);

        } catch (IOException e) {
            System.out.println("Error al cargar el formulario de edición: "
                    + e.getMessage());
        }
    }

    @FXML
    public void mostrarSalas() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("Salas.fxml")
            );

            Parent pantalla = loader.load();
            SalasController controlador = loader.getController();
            controlador.configurar(cine, persistencia);
            controlador.setPrincipalController(this);

            contenedorPrincipal.setCenter(pantalla);

        } catch (IOException e) {
            System.out.println("Error al cargar la pantalla de salas:");
            e.printStackTrace();
        }
    }

    /**
     * Que hace: abre la pantalla para consultar y cancelar reservas.
     */
    @FXML
    public void mostrarReservas() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("Reservas.fxml")
            );

            Parent pantalla = loader.load();

            ReservasController controlador = loader.getController();
            controlador.configurar(cine, persistencia);
            controlador.setPrincipalController(this);

            contenedorPrincipal.setCenter(pantalla);

        } catch (IOException e) {
            System.out.println("Error al cargar la pantalla de reservas:");
            e.printStackTrace();
        }
    }

    /**
     * Que hace: abre la pantalla de funciones en el centro de la ventana.
     */
    @FXML
    public void mostrarFunciones() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("Funciones.fxml")
            );

            Parent pantalla = loader.load();

            FuncionesController controlador = loader.getController();
            controlador.configurar(cine, persistencia);
            controlador.setPrincipalController(this);

            contenedorPrincipal.setCenter(pantalla);

        } catch (IOException e) {
            System.out.println("Error al cargar la pantalla de funciones:");
            e.printStackTrace();
        }
    }

    /**
     * Que hace: abre el formulario para programar una funcion nueva.
     */
    public void mostrarFormularioFuncion() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("FormularioFuncion.fxml")
            );

            Parent pantalla = loader.load();

            FormularioFuncionController controlador = loader.getController();

            controlador.configurar(cine, persistencia, this);

            contenedorPrincipal.setCenter(pantalla);

        } catch (IOException e) {
            System.out.println("Error al cargar el formulario de funcion:");
            e.printStackTrace();
        }
    }

    /**
     * Que hace: abre el formulario para editar el precio de una funcion
     * existente. Parametros: funcion (la funcion a editar).
     */
    public void mostrarFormularioEditarFuncion(Funcion funcion) {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("FormularioFuncion.fxml")
            );

            Parent pantalla = loader.load();

            FormularioFuncionController controlador = loader.getController();

            controlador.configurar(cine, persistencia, this);
            controlador.configurarEdicion(funcion);

            contenedorPrincipal.setCenter(pantalla);

        } catch (IOException e) {
            System.out.println("Error al cargar el formulario de edición de funcion:");
            e.printStackTrace();
        }
    }

    public void mostrarFormularioSala() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("FormularioSala.fxml")
            );

            Parent pantalla = loader.load();

            FormularioSalaController controlador = loader.getController();

            controlador.configurar(cine, persistencia, this);

            contenedorPrincipal.setCenter(pantalla);

        } catch (IOException e) {
            System.out.println("Error al cargar el formulario de sala:");
            e.printStackTrace();
        }
    }

    public void mostrarFormularioEditarSala(Sala sala) {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("FormularioSala.fxml")
            );

            Parent pantalla = loader.load();

            FormularioSalaController controlador = loader.getController();

            controlador.configurar(cine, persistencia, this);
            controlador.configurarEdicion(sala);

            contenedorPrincipal.setCenter(pantalla);

        } catch (IOException e) {
            System.out.println("Error al cargar el formulario de edición de sala:");
            e.printStackTrace();
        }
    }

    @FXML
    public void mostrarCartelera() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("Cartelera.fxml")
            );

            Parent pantalla = loader.load();

            CarteleraController controlador = loader.getController();
            controlador.setPrincipalController(this);
            controlador.configurar(cine);
            contenedorPrincipal.setCenter(pantalla);

        } catch (IOException e) {
            System.out.println("Error al cargar la cartelera:");
            e.printStackTrace();
        }
    }

    public void mostrarDetallePelicula(Pelicula pelicula) {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("DetallePelicula.fxml")
            );

            Parent pantalla = loader.load();

            DetallePeliculaController controlador = loader.getController();

            controlador.setPrincipalController(this);
            controlador.configurar(pelicula, cine);

            contenedorPrincipal.setCenter(pantalla);

        } catch (IOException e) {
            System.out.println("Error al cargar el detalle de película:");
            e.printStackTrace();
        }
    }

    @FXML
    public void mostrarNuevaReserva() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("NuevaReserva.fxml")
            );

            Parent pantalla = loader.load();

            NuevaReservaController controlador = loader.getController();
            controlador.configurar(cine, persistencia);
            controlador.setPrincipalController(this);

            contenedorPrincipal.setCenter(pantalla);

        } catch (IOException e) {
            System.out.println("Error al cargar Nueva reserva:");
            e.printStackTrace();
        }
    }

    public void mostrarNuevaReservaConFuncion(Funcion funcion) {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("NuevaReserva.fxml")
            );

            Parent pantalla = loader.load();

            NuevaReservaController controlador = loader.getController();
            controlador.configurar(cine, persistencia);
            controlador.setPrincipalController(this);
            controlador.preseleccionarFuncion(funcion);

            contenedorPrincipal.setCenter(pantalla);
            javafx.application.Platform.runLater(
                    controlador::abrirAccesoDesdeCartelera
            );

        } catch (IOException e) {
            System.out.println("Error al cargar Nueva reserva:");
            e.printStackTrace();
        }
    }

    /**
     * Abre la pantalla de reservas del cliente autenticado.
     *
     * Carga las reservas del cine actualizado y permite consultar únicamente
     * las del cliente que inició sesión.
     */
    @FXML
    public void mostrarMisReservas() {

        if (!SesionCliente.haySesion()) {
            mostrarCartelera();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/gestordecine/misreservas/MisReservas.fxml"
                    )
            );

            Parent pantalla = loader.load();

            // Identificar la pantalla para regresar a Cartelera al cerrar sesión.
            pantalla.getProperties().put("pantalla", "misreservas");

            MisReservasController controlador = loader.getController();
            controlador.configurar(cine, persistencia, this);

            contenedorPrincipal.setCenter(pantalla);

        } catch (IOException e) {
            System.out.println("Error al cargar Mis reservas:");
            e.printStackTrace();
        }
    }

    /**
     * Actualiza los controles del menú según la sesión del cliente.
     *
     * Mis reservas y Cerrar sesión solamente aparecen cuando hay un cliente
     * autenticado.
     */
    public void actualizarMenuCuenta() {

        boolean haySesion = SesionCliente.haySesion();

        botonMisReservas.setVisible(haySesion);
        botonMisReservas.setManaged(haySesion);

        botonIniciarSesion.setVisible(!haySesion);
        botonIniciarSesion.setManaged(!haySesion);

        botonCerrarSesion.setVisible(haySesion);
        botonCerrarSesion.setManaged(haySesion);

        etiquetaSesion.setVisible(haySesion);
        etiquetaSesion.setManaged(haySesion);

        if (haySesion) {
            Cliente cliente = SesionCliente.getClienteActual();

            etiquetaSesion.setText(
                    cliente.getNombre() + " " + cliente.getApellido()
            );
        } else {
            etiquetaSesion.setText("");
        }
    }

    /**
     * Muestra el formulario de inicio de sesión y registro.
     *
     * Abre una ventana modal sin barra de título, centrada sobre CINEMA, y
     * oscurece temporalmente el fondo.
     */
    @FXML
    private void mostrarAcceso() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/gestordecine/sesion/Acceso.fxml"
                    )
            );

            Parent formulario = loader.load();

            AccesoController controlador = loader.getController();
            controlador.configurar(cine, persistencia, this);

            Window principal = contenedorPrincipal.getScene().getWindow();

            Stage ventana = new Stage();
            ventana.initStyle(StageStyle.TRANSPARENT);
            ventana.initOwner(principal);
            ventana.initModality(Modality.WINDOW_MODAL);
            ventana.setResizable(false);

            // Botón de cierre integrado en el encabezado.
            Button botonCerrar = new Button("✕");
            botonCerrar.setStyle(
                    "-fx-background-color: transparent;"
                    + "-fx-text-fill: #94A3B8;"
                    + "-fx-font-size: 16px;"
                    + "-fx-cursor: hand;"
            );

            botonCerrar.setOnAction(evento -> ventana.close());

            Region separador = new Region();
            HBox.setHgrow(separador, Priority.ALWAYS);

            HBox encabezado = new HBox(separador, botonCerrar);
            encabezado.setAlignment(Pos.CENTER_RIGHT);

            // El formulario original se incorpora al modal.
            VBox contenido = new VBox(encabezado, formulario);
            contenido.setStyle(
                    "-fx-background-color: #151F30;"
                    + "-fx-background-radius: 12;"
                    + "-fx-border-color: #334155;"
                    + "-fx-border-radius: 12;"
                    + "-fx-border-width: 1;"
            );

            Scene escena = new Scene(contenido);
            escena.setFill(Color.TRANSPARENT);

            // Aplicar los estilos generales de CINEMA al formulario.
            escena.getStylesheets().add(
                    getClass().getResource("estilos.css").toExternalForm()
            );

            // Cerrar también con ESC.
            escena.setOnKeyPressed(evento -> {
                if (evento.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                    ventana.close();
                }
            });

            ventana.setScene(escena);

            // Oscurecer la aplicación mientras el modal está abierto.
            Effect efectoAnterior = contenedorPrincipal.getEffect();

            ColorAdjust oscurecer = new ColorAdjust();
            oscurecer.setBrightness(-0.50);

            contenedorPrincipal.setEffect(oscurecer);

            // Restaurar el fondo al cerrar.
            ventana.setOnHidden(evento
                    -> contenedorPrincipal.setEffect(efectoAnterior)
            );

            // Centrar el modal al mostrarse.
            ventana.setOnShown(evento -> {
                ventana.setX(
                        principal.getX()
                        + (principal.getWidth() - ventana.getWidth()) / 2
                );

                ventana.setY(
                        principal.getY()
                        + (principal.getHeight() - ventana.getHeight()) / 2
                );
            });

            ventana.showAndWait();

        } catch (IOException e) {
            System.out.println("Error al abrir Mi cuenta:");
            e.printStackTrace();
        }
    }

    /**
     * Finaliza la sesión del cliente.
     *
     * Oculta las opciones personales y, si el usuario estaba consultando Mis
     * reservas, regresa a Cartelera.
     *
     * No modifica los datos persistidos del cine.
     */
    @FXML
    private void cerrarSesion() {

        SesionCliente.cerrarSesion();
        actualizarMenuCuenta();

        // Salir de Mis reservas si era la pantalla actual.
        Node pantallaActual = contenedorPrincipal.getCenter();

        if (pantallaActual != null
                && "misreservas".equals(
                        pantallaActual.getProperties().get("pantalla")
                )) {

            mostrarCartelera();
        }
    }

    /**
     * Regresa a la cartelera principal de CINEMA.
     *
     * Permite reutilizar la navegación desde las distintas pantallas del
     * sistema.
     */
    public void volver() {
        mostrarCartelera();
    }

}
