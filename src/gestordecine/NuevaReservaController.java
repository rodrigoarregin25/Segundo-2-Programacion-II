package gestordecine;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Modality;
import javafx.stage.Window;
import javafx.scene.paint.Color;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Effect;

public class NuevaReservaController {

    private static final double ANCHO_BUTACA = 28;
    private static final double ESPACIO_ENTRE_ELEMENTOS = 8;
    private static final double ANCHO_PASILLO = 26;
    private static final double ANCHO_ETIQUETA_FILA = 24;
    private static final int MAXIMO_POR_TIPO = 10;

    // Cliente que inicio sesion. Es static para que siga igual al cambiar de pantalla
    private static Cliente clienteActual;

    private Cine cine;
    private PersistenciaCine persistencia;
    private PrincipalController principalController;
    private boolean volverCarteleraSiCancela = false;

    // true cuando el cuadro de ingreso esta en modo "registrarme"
    private boolean registrando = false;

    // Peliculas que tienen funciones futuras (misma posicion que en comboPelicula)
    private ArrayList<Pelicula> peliculasDisponibles = new ArrayList<>();
    // Funciones de la pelicula elegida (misma posicion que en comboFuncion)
    private ArrayList<Funcion> funcionesFiltradas = new ArrayList<>();
    private Funcion funcionElegida;

    // Butacas que el usuario fue marcando en el plano
    private ArrayList<Butaca> butacasMarcadas = new ArrayList<>();

    @FXML
    private VBox tarjetaCompra;
    @FXML
    private VBox panelLogin;
    @FXML
    private VBox panelRegistro;
    @FXML
    private VBox panelSesion;
    @FXML
    private VBox panelResumen;
    @FXML
    private ComboBox<String> comboPelicula;
    @FXML
    private ComboBox<String> comboFuncion;
    @FXML
    private ComboBox<String> comboGeneral;
    @FXML
    private ComboBox<String> comboEstudiante;
    @FXML
    private ComboBox<String> comboJubilado;
    @FXML
    private VBox contenedorButacas;
    @FXML
    private ListView<String> listaEntradas;
    @FXML
    private Label etiquetaTotal;
    @FXML
    private Label etiquetaCliente;
    @FXML
    private TextField campoDni;
    @FXML
    private TextField campoNombre;
    @FXML
    private TextField campoApellido;
    @FXML
    private TextField campoEmail;
    @FXML
    private PasswordField campoContrasenia;
    @FXML
    private Label mensajeLogin;
    @FXML
    private Label mensajeError;
    @FXML
    private Button botonIngresar;
    @FXML
    private Button botonAlternar;
    @FXML
    private Button botonConfirmar;
    @FXML
    private Button botonAbrirAcceso;
    @FXML
    private Label tituloAcceso;

    private Stage ventanaAcceso;

    /**
     * Que hace: carga las cantidades (0 a 10) y avisa cuando se elige otra
     * pelicula, otra funcion o se cambia una cantidad. JavaFX lo llama solo al
     * abrir la pantalla.
     */
    @FXML
    private void initialize() {
        for (int i = 0; i <= MAXIMO_POR_TIPO; i++) {
            comboGeneral.getItems().add(String.valueOf(i));
            comboEstudiante.getItems().add(String.valueOf(i));
            comboJubilado.getItems().add(String.valueOf(i));
        }
        comboGeneral.getSelectionModel().select(1);
        comboEstudiante.getSelectionModel().select(0);
        comboJubilado.getSelectionModel().select(0);

        // Al cambiar una cantidad solo se sacan las butacas que sobran
        comboGeneral.getSelectionModel().selectedIndexProperty()
                .addListener((observable, anterior, nuevo) -> ajustarSeleccion());
        comboEstudiante.getSelectionModel().selectedIndexProperty()
                .addListener((observable, anterior, nuevo) -> ajustarSeleccion());
        comboJubilado.getSelectionModel().selectedIndexProperty()
                .addListener((observable, anterior, nuevo) -> ajustarSeleccion());

        comboPelicula.getSelectionModel().selectedIndexProperty()
                .addListener((observable, anterior, nuevo) -> {
                    int posicion = nuevo.intValue();
                    if (posicion >= 0 && posicion < peliculasDisponibles.size()) {
                        cargarFunciones(peliculasDisponibles.get(posicion));
                    }
                });

        comboFuncion.getSelectionModel().selectedIndexProperty()
                .addListener((observable, anterior, nuevo) -> {
                    int posicion = nuevo.intValue();
                    if (posicion >= 0 && posicion < funcionesFiltradas.size()) {
                        elegirFuncion(funcionesFiltradas.get(posicion));
                    }
                });
        // Retirar el formulario de acceso del panel lateral.
        // Se mostrará únicamente dentro de la ventana emergente.
        VBox contenedorOriginal = (VBox) panelLogin.getParent();
        contenedorOriginal.getChildren().remove(panelLogin);
    }

    /**
     * Que hace: recibe el cine y la persistencia, carga las peliculas que
     * tienen funciones futuras activas y muestra el estado de la sesion.
     * Parametros: cine (el cine actual), persistencia (para guardar cambios).
     */
    public void configurar(Cine cine, PersistenciaCine persistencia) {
        this.cine = cine;
        this.persistencia = persistencia;

        peliculasDisponibles = new ArrayList<>();
        for (Funcion funcion : cine.buscarFuncionesFuturas()) {
            // las funciones desactivadas no se pueden reservar
            if (!funcion.isActiva()) {
                continue;
            }
            boolean yaEsta = false;
            for (Pelicula pelicula : peliculasDisponibles) {
                if (pelicula.getIdPelicula() == funcion.getPelicula().getIdPelicula()) {
                    yaEsta = true;
                }
            }
            if (!yaEsta) {
                peliculasDisponibles.add(funcion.getPelicula());
                comboPelicula.getItems().add(funcion.getPelicula().getTituloPelicula());
            }
        }

        if (!peliculasDisponibles.isEmpty()) {
            comboPelicula.getSelectionModel().selectFirst();
        }

        actualizarSesion();
    }

    public void setPrincipalController(PrincipalController principalController) {
        this.principalController = principalController;
    }

    public void abrirAccesoDesdeCartelera() {

        // Si ya inició sesión, puede reservar directamente.
        if (clienteActual != null) {
            return;
        }

        // Si cancela este acceso, debe regresar a Cartelera.
        volverCarteleraSiCancela = true;

        abrirAcceso();
    }

    /**
     * Que hace: deja elegidas la pelicula y la funcion que se pasan, por
     * ejemplo cuando se llega desde el detalle de una pelicula. Se llama
     * despues de configurar. Parametros: funcion (la funcion que se quiere
     * reservar).
     */
    public void preseleccionarFuncion(Funcion funcion) {
        for (int i = 0; i < peliculasDisponibles.size(); i++) {
            if (peliculasDisponibles.get(i).getIdPelicula()
                    == funcion.getPelicula().getIdPelicula()) {
                comboPelicula.getSelectionModel().select(i);
            }
        }

        for (int i = 0; i < funcionesFiltradas.size(); i++) {
            if (funcionesFiltradas.get(i).getIdFuncion() == funcion.getIdFuncion()) {
                comboFuncion.getSelectionModel().select(i);
            }
        }
    }

    /**
     * Que hace: muestra el cuadro de ingreso o los datos del cliente segun haya
     * sesion iniciada, y bloquea la compra hasta que se ingrese.
     */
    private void actualizarSesion() {
        // si el cliente ya no existe en los datos, se cierra la sesion
        if (clienteActual != null
                && cine.getServicioDeReservas().buscarClientePorDni(clienteActual.getDni()) == null) {
            clienteActual = null;
        }

        boolean conSesion = clienteActual != null;
        botonAbrirAcceso.setVisible(!conSesion);
        botonAbrirAcceso.setManaged(!conSesion);

        panelLogin.setVisible(!conSesion);
        panelLogin.setManaged(!conSesion);
        panelSesion.setVisible(conSesion);
        panelSesion.setManaged(conSesion);

        tarjetaCompra.setDisable(!conSesion);
        panelResumen.setDisable(!conSesion);

        if (conSesion) {
            etiquetaCliente.setText("Cliente: " + clienteActual.getNombre() + " "
                    + clienteActual.getApellido() + " (DNI " + clienteActual.getDni() + ")");
        }
    }

    /**
     * Que hace: cambia el cuadro de ingreso entre "ingresar" y "registrarme".
     */
    @FXML
    private void alternarRegistro() {

        registrando = !registrando;
        ocultarErrorLogin();

        panelRegistro.setVisible(registrando);
        panelRegistro.setManaged(registrando);

        if (registrando) {

            tituloAcceso.setText("Crear cuenta");
            botonIngresar.setText("Crear cuenta");
            botonAlternar.setText("Ya tengo cuenta, ingresar");

        } else {

            tituloAcceso.setText("Ingresar");
            botonIngresar.setText("Ingresar");
            botonAlternar.setText("No tengo cuenta, registrarme");
        }

        // Adaptar la altura del emergente según los campos visibles
        if (ventanaAcceso != null && ventanaAcceso.isShowing()) {

            javafx.application.Platform.runLater(() -> {

                ventanaAcceso.sizeToScene();

                // Mantener la ventana centrada sobre CINEMA
                Window principal = ventanaAcceso.getOwner();

                ventanaAcceso.setX(
                        principal.getX()
                        + (principal.getWidth() - ventanaAcceso.getWidth()) / 2
                );

                ventanaAcceso.setY(
                        principal.getY()
                        + (principal.getHeight() - ventanaAcceso.getHeight()) / 2
                );
            });
        }
    }

    /**
     * Que hace: inicia sesion con DNI y contrasenia. Si se esta registrando,
     * valida los datos y crea el cliente nuevo (se guarda sobre una copia del
     * cine).
     */
    @FXML
    private void ingresar() {
        ocultarErrorLogin();

        try {
            int dni;
            try {
                dni = Integer.parseInt(campoDni.getText().trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("El DNI debe ser un numero");
            }

            Cliente cliente;

            if (registrando) {
                validarDatosCliente();

                Cine cineTrabajo = cine.copiar();
                cliente = new Cliente(dni, campoNombre.getText().trim(),
                        campoApellido.getText().trim(), campoEmail.getText().trim(),
                        campoContrasenia.getText());
                cineTrabajo.getServicioDeReservas().registrarCliente(cliente);

                persistencia.guardar(cineTrabajo);
                principalController.actualizarCine(cineTrabajo);
                this.cine = cineTrabajo;

                if (ventanaAcceso != null) {
                    ventanaAcceso.close();
                }
            } else {
                cliente = cine.getServicioDeReservas().buscarClientePorDni(dni);
                if (cliente == null) {
                    throw new IllegalArgumentException(
                            "No existe un cliente con ese DNI. Usa registrarme");
                }
                if (!cliente.getContrasenia().equals(campoContrasenia.getText())) {
                    throw new IllegalArgumentException("La contrasenia es incorrecta");
                }
            }

            clienteActual = cliente;

            campoDni.clear();
            campoNombre.clear();
            campoApellido.clear();
            campoEmail.clear();
            campoContrasenia.clear();
            if (registrando) {
                alternarRegistro();
            }

            if (ventanaAcceso != null) {
                ventanaAcceso.close();
            }
            actualizarSesion();

        } catch (IOException | ClassNotFoundException e) {
            // Error de datos (archivo / copia), distinto de un error de reglas
            mostrarErrorLogin("Error al guardar los datos: " + e.getMessage());
        } catch (Exception e) {
            // Error de reglas: DNI repetido, datos invalidos, contrasenia incorrecta
            mostrarErrorLogin(e.getMessage());
        }
    }

    /**
     * Que hace: cierra la sesion y deja la compra bloqueada otra vez.
     */
    @FXML
    private void cerrarSesion() {
        clienteActual = null;
        butacasMarcadas.clear();
        ocultarError();
        dibujarButacas();
        actualizarResumen();
        actualizarSesion();
    }

    /**
     * Que hace: carga en el desplegable las funciones futuras y activas de una
     * pelicula. Parametros: pelicula (la pelicula elegida).
     */
    private void cargarFunciones(Pelicula pelicula) {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        funcionElegida = null;
        butacasMarcadas.clear();
        funcionesFiltradas = new ArrayList<>();
        comboFuncion.getItems().clear();

        for (Funcion funcion : cine.buscarFuncionesFuturas()) {
            // las funciones desactivadas no se pueden reservar
            if (!funcion.isActiva()) {
                continue;
            }
            if (funcion.getPelicula().getIdPelicula() == pelicula.getIdPelicula()) {
                funcionesFiltradas.add(funcion);
                comboFuncion.getItems().add(
                        funcion.getSala().getNombreSala()
                        + " | " + funcion.getFechaHora().format(formato)
                        + " | " + funcion.getFormatoPelicula()
                        + " | $" + funcion.getPrecio());
            }
        }

        dibujarButacas();
        actualizarResumen();

        if (!funcionesFiltradas.isEmpty()) {
            comboFuncion.getSelectionModel().selectFirst();
        }
    }

    /**
     * Que hace: al elegir una funcion, borra lo marcado y redibuja el plano.
     * Parametros: funcion (la funcion elegida).
     */
    private void elegirFuncion(Funcion funcion) {
        funcionElegida = funcion;
        butacasMarcadas.clear();
        ocultarError();
        dibujarButacas();
        actualizarResumen();
    }

    /**
     * Que hace: suma las entradas de los tres tipos. Retorna: la cantidad total
     * de entradas elegida.
     */
    private int obtenerTotalEntradas() {
        return comboGeneral.getSelectionModel().getSelectedIndex()
                + comboEstudiante.getSelectionModel().getSelectedIndex()
                + comboJubilado.getSelectionModel().getSelectedIndex();
    }

    /**
     * Que hace: cuando baja la cantidad de entradas, saca las ultimas butacas
     * marcadas que sobran. Lo que ya estaba bien marcado se conserva.
     */
    private void ajustarSeleccion() {
        int total = obtenerTotalEntradas();

        while (butacasMarcadas.size() > total) {
            butacasMarcadas.remove(butacasMarcadas.size() - 1);
        }

        ocultarError();
        dibujarButacas();
        actualizarResumen();
    }

    /**
     * Que hace: dibuja el plano de la sala de la funcion elegida, con los
     * pasillos segun la distribucion (ej: 4-8-4).
     */
    private void dibujarButacas() {
        contenedorButacas.getChildren().clear();
        contenedorButacas.setAlignment(Pos.TOP_CENTER);

        if (funcionElegida == null) {
            return;
        }

        Sala sala = funcionElegida.getSala();
        String[] bloques = sala.getDistribucionButacas().split("-");

        Label pantalla = new Label("PANTALLA");
        pantalla.getStyleClass().add("texto-pantalla");
        contenedorButacas.getChildren().add(pantalla);

        for (int i = 0; i < sala.getCantidadFilas(); i++) {

            char letraFila = (char) ('A' + i);

            HBox filaVisual = new HBox(ESPACIO_ENTRE_ELEMENTOS);
            filaVisual.setAlignment(Pos.CENTER);

            Label etiquetaFila = new Label(String.valueOf(letraFila));
            etiquetaFila.getStyleClass().add("etiqueta-fila");
            etiquetaFila.setMinWidth(ANCHO_ETIQUETA_FILA);
            etiquetaFila.setPrefWidth(ANCHO_ETIQUETA_FILA);
            etiquetaFila.setMaxWidth(ANCHO_ETIQUETA_FILA);
            etiquetaFila.setAlignment(Pos.CENTER);
            filaVisual.getChildren().add(etiquetaFila);

            int numero = 1;

            for (int b = 0; b < bloques.length; b++) {

                int cantidadBloque = Integer.parseInt(bloques[b].trim());

                for (int j = 0; j < cantidadBloque; j++) {
                    Butaca butaca = new Butaca(letraFila, numero);
                    filaVisual.getChildren().add(crearButacaVisual(butaca, numero));
                    numero++;
                }

                if (b < bloques.length - 1) {
                    Region pasillo = new Region();
                    pasillo.setMinWidth(ANCHO_PASILLO);
                    pasillo.setPrefWidth(ANCHO_PASILLO);
                    pasillo.setMaxWidth(ANCHO_PASILLO);
                    filaVisual.getChildren().add(pasillo);
                }
            }

            contenedorButacas.getChildren().add(filaVisual);
        }
    }

    /**
     * Que hace: crea el cuadradito de una butaca. Gris si esta ocupada, verde
     * si esta seleccionada, y al hacer clic la marca o la desmarca. Parametros:
     * butaca (la butaca), numero (el numero que se muestra). Retorna: el Label
     * que representa la butaca.
     */
    private Label crearButacaVisual(Butaca butaca, int numero) {
        Label etiqueta = new Label(String.valueOf(numero));
        etiqueta.getStyleClass().add("butaca-visual");
        etiqueta.setAlignment(Pos.CENTER);
        etiqueta.setMinWidth(ANCHO_BUTACA);
        etiqueta.setPrefWidth(ANCHO_BUTACA);
        etiqueta.setMaxWidth(ANCHO_BUTACA);

        if (cine.getServicioDeReservas().estaOcupada(funcionElegida, butaca)) {
            etiqueta.setStyle("-fx-background-color: #6B7280;");
        } else if (butacasMarcadas.contains(butaca)) {
            etiqueta.setStyle("-fx-background-color: #22C55E;");
        }

        etiqueta.setOnMouseClicked(evento -> alternarButaca(butaca));

        return etiqueta;
    }

    /**
     * Que hace: marca la butaca o la desmarca si ya estaba marcada. Las
     * ocupadas no se pueden marcar, y tampoco mas butacas que la cantidad total
     * de entradas elegida. Parametros: butaca (la butaca donde se hizo clic).
     */
    private void alternarButaca(Butaca butaca) {
        ocultarError();

        if (cine.getServicioDeReservas().estaOcupada(funcionElegida, butaca)) {
            mostrarError("La butaca " + butaca.getIdentificador() + " ya esta ocupada");
            return;
        }

        if (butacasMarcadas.contains(butaca)) {
            butacasMarcadas.remove(butaca);
        } else {
            if (butacasMarcadas.size() >= obtenerTotalEntradas()) {
                mostrarError("Ya elegiste las " + obtenerTotalEntradas()
                        + " butacas. Desmarca una o aumenta la cantidad de entradas");
                return;
            }
            butacasMarcadas.add(butaca);
        }

        dibujarButacas();
        actualizarResumen();
    }

    /**
     * Que hace: arma las entradas a partir de las butacas marcadas. Las
     * primeras son generales, despues estudiantes y al final jubilados, segun
     * las cantidades elegidas. Retorna: la lista de entradas (ArrayList de
     * Entrada).
     */
    private ArrayList<Entrada> construirEntradas() {
        ArrayList<Entrada> entradas = new ArrayList<>();

        if (funcionElegida == null) {
            return entradas;
        }

        double precio = funcionElegida.getPrecio();
        int generales = comboGeneral.getSelectionModel().getSelectedIndex();
        int estudiantes = comboEstudiante.getSelectionModel().getSelectedIndex();

        for (int i = 0; i < butacasMarcadas.size(); i++) {
            Butaca butaca = butacasMarcadas.get(i);

            if (i < generales) {
                entradas.add(new EntradaGeneral(butaca, precio));
            } else if (i < generales + estudiantes) {
                entradas.add(new EntradaEstudiante(butaca, precio));
            } else {
                entradas.add(new EntradaJubilados(butaca, precio));
            }
        }
        return entradas;
    }

    /**
     * Que hace: actualiza la lista de butacas marcadas y el total.
     */
    private void actualizarResumen() {
        ArrayList<String> textos = new ArrayList<>();
        double total = 0;

        for (Entrada entrada : construirEntradas()) {
            textos.add(entrada.getButaca().getIdentificador()
                    + " - " + entrada.getTipo()
                    + " - $" + entrada.calcularPrecioFinal());
            total += entrada.calcularPrecioFinal();
        }
        listaEntradas.setItems(FXCollections.observableArrayList(textos));
        etiquetaTotal.setText("Total: $" + total);
    }

    /**
     * Que hace: confirma la reserva con las butacas marcadas para el cliente
     * que inicio sesion. Muestra el comprobante en una ventana y limpia la
     * pantalla. Se trabaja sobre una copia del cine: si algo falla, no se
     * guarda nada.
     */
    @FXML
    private void confirmarReserva() {
        ocultarError();

        try {
            if (clienteActual == null) {
                throw new IllegalArgumentException("Inicia sesion para reservar");
            }
            if (funcionElegida == null) {
                throw new IllegalArgumentException("Seleccione una funcion");
            }
            int total = obtenerTotalEntradas();
            if (total == 0) {
                throw new IllegalArgumentException("Elegi al menos una entrada");
            }
            if (butacasMarcadas.size() != total) {
                throw new IllegalArgumentException("Elegiste " + total
                        + " entradas pero marcaste " + butacasMarcadas.size() + " butacas");
            }

            Cine cineTrabajo = cine.copiar();
            ServicioDeReservas servicio = cineTrabajo.getServicioDeReservas();
            Funcion funcion = cineTrabajo.buscarFuncionPorId(funcionElegida.getIdFuncion());
            Cliente cliente = servicio.buscarClientePorDni(clienteActual.getDni());

            Reserva reserva = cineTrabajo.confirmarReserva(
                    cliente, funcion, construirEntradas());

            persistencia.guardar(cineTrabajo);
            principalController.actualizarCine(cineTrabajo);
            this.cine = cineTrabajo;

            limpiarPantalla();
            VentanaComprobante.mostrar(reserva);

        } catch (IOException | ClassNotFoundException e) {
            // Error de datos (archivo / copia), distinto de un error de reglas
            mostrarError("Error al guardar los datos: " + e.getMessage());
        } catch (Exception e) {
            // Error de reglas de negocio: butaca ocupada, datos invalidos, etc.
            mostrarError(e.getMessage());
        }
    }

    /**
     * Que hace: valida los datos de un cliente nuevo (nombre, apellido, email y
     * contrasenia). Lanza IllegalArgumentException si algo esta mal.
     */
    private void validarDatosCliente() {
        if (campoNombre.getText().isBlank() || campoApellido.getText().isBlank()) {
            throw new IllegalArgumentException("Complete el nombre y el apellido");
        }

        String email = campoEmail.getText().trim();
        if (!email.contains("@") || !email.contains(".")
                || email.indexOf('@') > email.lastIndexOf('.')) {
            throw new IllegalArgumentException("El email no es valido");
        }

        if (campoContrasenia.getText().length() < 4) {
            throw new IllegalArgumentException(
                    "La contrasenia debe tener al menos 4 caracteres");
        }
    }

    /**
     * Que hace: deja la compra como nueva: borra las butacas marcadas y vuelve
     * la cantidad a 1 entrada general. La sesion sigue iniciada.
     */
    private void limpiarPantalla() {
        butacasMarcadas.clear();
        comboGeneral.getSelectionModel().select(1);
        comboEstudiante.getSelectionModel().select(0);
        comboJubilado.getSelectionModel().select(0);

        ocultarError();
        dibujarButacas();
        actualizarResumen();
    }

    @FXML
    private void abrirAcceso() {

        if (ventanaAcceso == null) {

            ventanaAcceso = new Stage();
            ventanaAcceso.initStyle(StageStyle.TRANSPARENT);
            ventanaAcceso.initModality(Modality.WINDOW_MODAL);

            Window principal = botonAbrirAcceso.getScene().getWindow();
            ventanaAcceso.initOwner(principal);

            // El formulario que ya desarrolló Gimena
            panelLogin.setVisible(true);
            panelLogin.setManaged(true);

            // Botón para cerrar
            Button botonCerrar = new Button("✕");
            botonCerrar.setStyle(
                    "-fx-background-color: transparent;"
                    + "-fx-text-fill: #94A3B8;"
                    + "-fx-font-size: 17px;"
                    + "-fx-cursor: hand;"
            );

            botonCerrar.setOnAction(evento -> ventanaAcceso.close());

            // Retirar el título de su posición original
            panelLogin.getChildren().remove(tituloAcceso);

// Encabezado: título a la izquierda y X a la derecha
            Region separador = new Region();
            HBox.setHgrow(separador, Priority.ALWAYS);

            HBox encabezado = new HBox(10, tituloAcceso, separador, botonCerrar);
            encabezado.setAlignment(Pos.CENTER_LEFT);

            // Contenedor personalizado
            VBox contenido = new VBox(16, encabezado, panelLogin);
            contenido.setPadding(new javafx.geometry.Insets(24));
            contenido.setPrefWidth(390);

            contenido.setStyle(
                    "-fx-background-color: #151F30;"
                    + "-fx-background-radius: 12;"
                    + "-fx-border-color: #334155;"
                    + "-fx-border-radius: 12;"
                    + "-fx-border-width: 1;"
            );

            contenido.getStyleClass().add("acceso-cinema");

            contenido.setPrefWidth(420);

            Scene escena = new Scene(contenido);
            escena.setFill(Color.TRANSPARENT);

            escena.getStylesheets().add(
                    getClass().getResource("estilos.css").toExternalForm()
            );

            escena.setOnKeyPressed(evento -> {
                if (evento.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                    ventanaAcceso.close();
                }
            });

            ventanaAcceso.setScene(escena);

            ventanaAcceso.setOnShown(evento -> {
                ventanaAcceso.setX(
                        principal.getX()
                        + (principal.getWidth() - ventanaAcceso.getWidth()) / 2
                );

                ventanaAcceso.setY(
                        principal.getY()
                        + (principal.getHeight() - ventanaAcceso.getHeight()) / 2
                );
            });
        }

        // Obtener la ventana principal de CINEMA
        Window principal = botonAbrirAcceso.getScene().getWindow();

// Guardar el efecto anterior para restaurarlo después
        Effect efectoAnterior = principal.getScene().getRoot().getEffect();

// Oscurecer el fondo mientras está abierto el ingreso
        ColorAdjust oscurecer = new ColorAdjust();
        oscurecer.setBrightness(-0.50);

        principal.getScene().getRoot().setEffect(oscurecer);

// Al cerrar el emergente
        ventanaAcceso.setOnHidden(evento -> {

            // Recuperar el aspecto normal de CINEMA
            principal.getScene().getRoot().setEffect(efectoAnterior);

            // Si vino desde Cartelera y canceló el acceso
            if (volverCarteleraSiCancela && clienteActual == null) {
                principalController.mostrarCartelera();
            }

            volverCarteleraSiCancela = false;
        });

        ventanaAcceso.showAndWait();
    }

    private void mostrarError(String mensaje) {
        mensajeError.setText(mensaje);
        mensajeError.setVisible(true);
        mensajeError.setManaged(true);
    }

    private void ocultarError() {
        mensajeError.setVisible(false);
        mensajeError.setManaged(false);
    }

    private void mostrarErrorLogin(String mensaje) {
        mensajeLogin.setText(mensaje);
        mensajeLogin.setVisible(true);
        mensajeLogin.setManaged(true);
    }

    private void ocultarErrorLogin() {
        mensajeLogin.setVisible(false);
        mensajeLogin.setManaged(false);
    }
}
