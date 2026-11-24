package gestordecine.controladores;

import gestordecine.Cine;
import gestordecine.Cliente;
import gestordecine.Funcion;
import gestordecine.Pelicula;
import gestordecine.PersistenciaCine;
import gestordecine.Sala;
import gestordecine.controladores.NuevaReservaController;
import gestordecine.controladores.DetallePeliculaController;
import gestordecine.controladores.CarteleraController;
import gestordecine.controladores.ReservasController;
import gestordecine.controladores.FuncionesController;
import gestordecine.controladores.SalasController;
import gestordecine.controladores.PeliculasController;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import gestordecine.misreservas.MisReservasController;
import gestordecine.sesion.AccesoController;
import gestordecine.sesion.SesionCliente;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Effect;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.geometry.Pos;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import gestordecine.usuarios.UsuariosController;
import gestordecine.usuarios.FormularioUsuarioController;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

/**
 * Controlador de la ventana principal. Guarda el cine activo y se encarga de la
 * navegación: carga cada pantalla en el centro de la ventana, le pasa los datos
 * que necesita y controla qué opciones del menú ve cada usuario según su rol.
 */
public class PrincipalController {

    private Cine cine;
    private PersistenciaCine persistencia;

    /**
     * Cambia el cine activo.
     *
     * @param cine Cine que se quiere usar.
     */
    public void setCine(Cine cine) {
        this.cine = cine;
        this.persistencia = persistencia;
    }

    @FXML
    private BorderPane contenedorPrincipal;

    @FXML
    private Button botonMisReservas;

    // Botones del menu que solo ve el administrador
    @FXML
    private Button botonReservas;

    @FXML
    private Button botonPeliculas;

    @FXML
    private Button botonSalas;

    @FXML
    private Button botonFunciones;

    @FXML
    private Button botonIniciarSesion;

    @FXML
    private Button botonCerrarSesion;

    @FXML
    private Label etiquetaSesion;

    @FXML
    private Button botonUsuarios;

    @FXML
    private Button botonCartelera;

    /**
     * Se ejecuta automáticamente al cargar la ventana principal. Asigna a cada
     * botón su ícono correspondiente.
     */
    @FXML
    private void initialize() {
        aplicarIcono(botonCartelera, "Cartelera");
        aplicarIcono(botonMisReservas, "Mis reservas");
        aplicarIcono(botonReservas, "Reservas");
        aplicarIcono(botonPeliculas, "Películas");
        aplicarIcono(botonSalas, "Salas");
        aplicarIcono(botonFunciones, "Funciones");
        aplicarIcono(botonUsuarios, "Usuarios");
        aplicarIcono(botonIniciarSesion, "Iniciar sesión");
        aplicarIcono(botonCerrarSesion, "Cerrar sesión");
    }

    /**
     * Comprueba que el usuario con sesión sea administrador. Si no lo es, lo
     * manda a la cartelera. Se usa al principio de cada pantalla de
     * administración, además de ocultar los botones del menú.
     *
     * @return true si NO tiene permisos (hay que cortar), false si es
     * administrador.
     */
    private boolean sinPermisoAdministrador() {
        if (SesionCliente.esAdministrador()) {
            return false;
        }

        mostrarCartelera();
        return true;
    }

    /**
     * Abre la pantalla de administración de películas. Solo para
     * administradores.
     */
    @FXML
    public void mostrarPeliculas() {

        if (sinPermisoAdministrador()) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/gestordecine/vistas/Peliculas.fxml")
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

    /**
     * Guarda el cine y la persistencia con los que va a trabajar la ventana, y
     * actualiza el menú según la sesión.
     *
     * @param cine Cine con los datos cargados.
     * @param persistencia Objeto que guarda los datos en el archivo.
     */
    public void configurar(Cine cine, PersistenciaCine persistencia) {
        this.cine = cine;
        this.persistencia = persistencia;
        actualizarMenuCuenta();
    }

    /**
     * Actualiza la instancia principal del cine y sincroniza el usuario que
     * tiene la sesión iniciada.
     *
     * Si la cuenta fue desactivada, finaliza su sesión.
     *
     * @param cineActualizado Cine con los últimos cambios guardados.
     */
    public void actualizarCine(Cine cineActualizado) {

        this.cine = cineActualizado;

        Cliente clienteActual = SesionCliente.getClienteActual();

        if (clienteActual == null) {
            return;
        }

        Cliente clienteActualizado = cineActualizado
                .getServicioDeReservas()
                .buscarClientePorDni(clienteActual.getDni());

        if (clienteActualizado == null || !clienteActualizado.isActivo()) {
            SesionCliente.cerrarSesion();
            actualizarMenuCuenta();
            mostrarCartelera();
            return;
        }

        SesionCliente.iniciarSesion(clienteActualizado);
        actualizarMenuCuenta();
    }

    /**
     * Devuelve el cine actual, con los últimos cambios guardados.
     *
     * @return El cine en uso.
     */
    public Cine obtenerCineActual() {
        return cine;
    }

    /**
     * Vuelve a cargar el cine desde el archivo.
     *
     * @throws IOException Si no se puede leer el archivo.
     * @throws ClassNotFoundException Si el archivo tiene una clase que no se
     * encuentra.
     */
    public void recuperarCineGuardado() throws IOException, ClassNotFoundException {

        this.cine = persistencia.cargarOCrear();
    }

    /**
     * Abre el formulario para registrar una película nueva. Solo para
     * administradores.
     */
    public void mostrarFormularioPelicula() {

        if (sinPermisoAdministrador()) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/gestordecine/vistas/FormularioPelicula.fxml")
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

    /**
     * Abre el formulario para editar una película existente. Solo para
     * administradores.
     *
     * @param pelicula Película que se quiere editar.
     */
    public void mostrarFormularioEditarPelicula(Pelicula pelicula) {

        if (sinPermisoAdministrador()) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/gestordecine/vistas/FormularioPelicula.fxml")
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

    /**
     * Abre la pantalla de administración de salas. Solo para administradores.
     */
    @FXML
    public void mostrarSalas() {

        if (sinPermisoAdministrador()) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/gestordecine/vistas/Salas.fxml")
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
     * Abre la pantalla para consultar y cancelar reservas. Solo para
     * administradores.
     */
    @FXML
    public void mostrarReservas() {

        if (sinPermisoAdministrador()) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/gestordecine/vistas/Reservas.fxml")
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
     * Abre la pantalla de funciones en el centro de la ventana. Solo para
     * administradores.
     */
    @FXML
    public void mostrarFunciones() {

        if (sinPermisoAdministrador()) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/gestordecine/vistas/Funciones.fxml")
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
     * Abre el formulario para programar una función nueva. Solo para
     * administradores.
     */
    public void mostrarFormularioFuncion() {

        if (sinPermisoAdministrador()) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/gestordecine/vistas/FormularioFuncion.fxml")
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
     * Abre el formulario para editar el precio de una función existente. Solo
     * para administradores.
     *
     * @param funcion Función que se quiere editar.
     */
    public void mostrarFormularioEditarFuncion(Funcion funcion) {

        if (sinPermisoAdministrador()) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/gestordecine/vistas/FormularioFuncion.fxml")
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

    /**
     * Abre el formulario para registrar una sala nueva. Solo para
     * administradores.
     */
    public void mostrarFormularioSala() {

        if (sinPermisoAdministrador()) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/gestordecine/vistas/FormularioSala.fxml")
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

    /**
     * Abre el formulario para editar una sala existente. Solo para
     * administradores.
     *
     * @param sala Sala que se quiere editar.
     */
    public void mostrarFormularioEditarSala(Sala sala) {

        if (sinPermisoAdministrador()) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/gestordecine/vistas/FormularioSala.fxml")
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

    /**
     * Abre la cartelera en el centro de la ventana. Puede verla cualquier
     * usuario, con o sin sesión.
     */
    @FXML
    public void mostrarCartelera() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/gestordecine/vistas/Cartelera.fxml")
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

    /**
     * Abre el detalle de una película, con sus datos y sus funciones.
     *
     * @param pelicula Película que se quiere ver.
     */
    public void mostrarDetallePelicula(Pelicula pelicula) {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/gestordecine/vistas/DetallePelicula.fxml")
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

    /**
     * Abre la pantalla para hacer una reserva nueva.
     */
    @FXML
    public void mostrarNuevaReserva() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/gestordecine/vistas/NuevaReserva.fxml")
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

    /**
     * Abre la pantalla de nueva reserva con una función ya elegida, por ejemplo
     * cuando se llega desde el detalle de una película. Si no hay sesión
     * iniciada, pide el acceso.
     *
     * @param funcion Función que se quiere reservar.
     */
    public void mostrarNuevaReservaConFuncion(Funcion funcion) {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/gestordecine/vistas/NuevaReserva.fxml")
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
     * Actualiza los controles del menú según el rol del usuario autenticado.
     *
     * Los clientes pueden acceder a Mis reservas. Los administradores pueden
     * acceder a la gestión de reservas, películas, salas, funciones y usuarios.
     *
     * Las opciones de sesión se muestran según exista o no un usuario
     * autenticado.
     */
    public void actualizarMenuCuenta() {

        boolean haySesion = SesionCliente.haySesion();
        boolean esAdministrador = SesionCliente.esAdministrador();
        boolean esCliente = SesionCliente.esCliente();

        // Opciones exclusivas del cliente
        botonMisReservas.setVisible(esCliente);
        botonMisReservas.setManaged(esCliente);

        // Opciones exclusivas del administrador
        botonReservas.setVisible(esAdministrador);
        botonReservas.setManaged(esAdministrador);

        botonPeliculas.setVisible(esAdministrador);
        botonPeliculas.setManaged(esAdministrador);

        botonSalas.setVisible(esAdministrador);
        botonSalas.setManaged(esAdministrador);

        botonFunciones.setVisible(esAdministrador);
        botonFunciones.setManaged(esAdministrador);

        botonUsuarios.setVisible(esAdministrador);
        botonUsuarios.setManaged(esAdministrador);

        // Opciones de inicio y cierre de sesión
        botonIniciarSesion.setVisible(!haySesion);
        botonIniciarSesion.setManaged(!haySesion);

        botonCerrarSesion.setVisible(haySesion);
        botonCerrarSesion.setManaged(haySesion);

        etiquetaSesion.setVisible(haySesion);
        etiquetaSesion.setManaged(haySesion);

        // Nombre del usuario autenticado
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
    public void mostrarAcceso() {

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
                    getClass().getResource("/gestordecine/estilos/estilos.css").toExternalForm()
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
     * Abre la pantalla de administración de usuarios.
     *
     * Solo permite el acceso a usuarios con rol ADMINISTRADOR.
     */
    @FXML
    public void mostrarUsuarios() {

        if (sinPermisoAdministrador()) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/gestordecine/usuarios/Usuarios.fxml")
            );

            Parent pantalla = loader.load();

            UsuariosController controlador = loader.getController();
            controlador.configurar(cine, persistencia, this);

            contenedorPrincipal.setCenter(pantalla);

        } catch (IOException e) {
            System.out.println("Error al cargar la pantalla de usuarios:");
            e.printStackTrace();
        }
    }

    /**
     * Abre el formulario para editar un usuario registrado.
     *
     * @param usuario Usuario seleccionado en la administración.
     */
    public void mostrarFormularioEditarUsuario(Cliente usuario) {

        if (sinPermisoAdministrador()) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/gestordecine/usuarios/FormularioUsuario.fxml"
                    )
            );

            Parent pantalla = loader.load();

            FormularioUsuarioController controlador = loader.getController();

            controlador.configurar(cine, persistencia, this, usuario);

            contenedorPrincipal.setCenter(pantalla);

        } catch (IOException e) {
            System.out.println("Error al abrir el formulario de usuario:");
            e.printStackTrace();
        }
    }

    /**
     * Abre el formulario para registrar un nuevo usuario.
     *
     * Permite acceder únicamente a administradores autenticados.
     */
    public void mostrarFormularioNuevoUsuario() {

        if (sinPermisoAdministrador()) {
            return;
        }

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/gestordecine/usuarios/FormularioUsuario.fxml"
                    )
            );

            Parent pantalla = loader.load();

            FormularioUsuarioController controlador = loader.getController();

            controlador.configurarNuevo(cine, persistencia, this);

            contenedorPrincipal.setCenter(pantalla);

        } catch (IOException e) {
            System.out.println("Error al abrir Nuevo usuario:");
            e.printStackTrace();
        }
    }

    /**
     * Finaliza la sesión del cliente.
     *
     * Oculta las opciones personales y de administracion, y vuelve siempre a
     * Cartelera para no quedar en una pantalla que ya no corresponde.
     *
     * No modifica los datos persistidos del cine.
     */
    @FXML
    private void cerrarSesion() {

        SesionCliente.cerrarSesion();
        actualizarMenuCuenta();
        mostrarCartelera();
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

    /**
     * Crea un ícono vectorial para cada sección del menú.
     *
     * Los íconos utilizan trazados SVG y un color azul uniforme para mantener
     * la identidad visual de CINEMA.
     *
     * @param seccion Nombre de la sección del menú.
     * @return Nodo con el ícono correspondiente.
     */
    private Node crearIconoMenu(String seccion) {

        String trazado = switch (seccion) {

            case "Cartelera" ->
                "M3 9h18v12H3z M3 9L2 4l19-2 1 5z "
                + "M7 3l3 5 M14 2l3 5";

            case "Reservas", "Mis reservas" ->
                "M3 7h18v4a2 2 0 0 0 0 4v3H3v-3a2 2 0 0 0 0-4z "
                + "M13 7v2 M13 12v2 M13 17v1";

            case "Películas" ->
                "M4 3h16v18H4z M8 3v18 M16 3v18 "
                + "M4 8h4 M4 15h4 M16 8h4 M16 15h4";

            case "Salas" ->
                "M7 5v9h10V5 M5 10H3v7h18v-7h-2 "
                + "M7 17v3 M17 17v3";

            case "Funciones" ->
                "M4 6h16v15H4z M4 10h16 "
                + "M8 3v5 M16 3v5";

            case "Usuarios" ->
                "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2 "
                + "M9 11a4 4 0 1 0 0-8a4 4 0 0 0 0 8 "
                + "M20 21v-2a4 4 0 0 0-3-3.87";

            case "Iniciar sesión" ->
                "M10 17l5-5-5-5 M15 12H3 "
                + "M12 3h6a3 3 0 0 1 3 3v12a3 3 0 0 1-3 3h-6";

            case "Cerrar sesión" ->
                "M9 17l5-5-5-5 M14 12H3 "
                + "M12 3h6a3 3 0 0 1 3 3v12a3 3 0 0 1-3 3h-6";

            default ->
                "M6 4l10 8-10 8";
        };

        SVGPath icono = new SVGPath();
        icono.setContent(trazado);

        // Color azul de CINEMA.
        icono.setStroke(Color.web("#60A5FA"));
        icono.setStrokeWidth(1.8);
        icono.setFill(Color.TRANSPARENT);

        // Trazos suaves y redondeados.
        icono.setStrokeLineCap(StrokeLineCap.ROUND);
        icono.setStrokeLineJoin(StrokeLineJoin.ROUND);

        StackPane contenedor = new StackPane(icono);
        contenedor.setMinSize(26, 26);
        contenedor.setPrefSize(26, 26);
        contenedor.setMaxSize(26, 26);

        return contenedor;
    }

    /**
     * Asigna un ícono al botón indicado.
     *
     * @param boton Botón al que se le asignará el ícono.
     * @param seccion Nombre de la sección del menú.
     */
    private void aplicarIcono(Button boton, String seccion) {

        if (boton != null) {
            boton.setGraphic(crearIconoMenu(seccion));
            boton.setContentDisplay(
                    javafx.scene.control.ContentDisplay.LEFT
            );
            boton.setGraphicTextGap(10);
        }
    }

}
