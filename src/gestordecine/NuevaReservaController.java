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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class NuevaReservaController {

    private static final double ANCHO_BUTACA = 28;
    private static final double ESPACIO_ENTRE_ELEMENTOS = 8;
    private static final double ANCHO_PASILLO = 26;
    private static final double ANCHO_ETIQUETA_FILA = 24;
    private static final int MAXIMO_ENTRADAS = 10;

    private Cine cine;
    private PersistenciaCine persistencia;
    private PrincipalController principalController;

    // La posicion elegida en el desplegable coincide con la posicion en esta lista
    private ArrayList<Funcion> funcionesDisponibles;
    private Funcion funcionElegida;

    // Entradas (butaca + tipo) que el usuario fue marcando en el plano
    private ArrayList<Entrada> entradas = new ArrayList<>();

    @FXML
    private ComboBox<String> comboFuncion;
    @FXML
    private ComboBox<String> comboCantidad;
    @FXML
    private ComboBox<String> comboTipoEntrada;
    @FXML
    private VBox contenedorButacas;
    @FXML
    private ListView<String> listaEntradas;
    @FXML
    private Label etiquetaTotal;
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
    private Label mensajeError;
    @FXML
    private Button botonConfirmar;
    @FXML
    private TextArea areaComprobante;

    /**
     * Que hace: carga las cantidades y los tipos de entrada, y avisa cuando se
     * elige otra funcion o otra cantidad. JavaFX lo llama solo al abrir la pantalla.
     */
    @FXML
    private void initialize() {
        comboTipoEntrada.getItems().addAll(
                "Entrada General", "Entrada Estudiante", "Entrada Jubilados");
        comboTipoEntrada.getSelectionModel().selectFirst();

        for (int i = 1; i <= MAXIMO_ENTRADAS; i++) {
            comboCantidad.getItems().add(String.valueOf(i));
        }
        comboCantidad.getSelectionModel().selectFirst();

        // Al cambiar la cantidad se borra lo marcado para empezar de nuevo
        comboCantidad.getSelectionModel().selectedIndexProperty()
                .addListener((observable, anterior, nuevo) -> {
                    entradas.clear();
                    ocultarError();
                    dibujarButacas();
                    actualizarResumen();
                });

        comboFuncion.getSelectionModel().selectedIndexProperty()
                .addListener((observable, anterior, nuevo) -> {
                    int posicion = nuevo.intValue();
                    if (posicion >= 0) {
                        elegirFuncion(funcionesDisponibles.get(posicion));
                    }
                });
    }

    /**
     * Que hace: recibe el cine y la persistencia, y carga las funciones futuras.
     * Parametros: cine (el cine actual), persistencia (para guardar cambios).
     */
    public void configurar(Cine cine, PersistenciaCine persistencia) {
        this.cine = cine;
        this.persistencia = persistencia;

        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        funcionesDisponibles = cine.buscarFuncionesFuturas();

        for (Funcion funcion : funcionesDisponibles) {
            comboFuncion.getItems().add(
                    funcion.getPelicula().getTituloPelicula()
                    + " | " + funcion.getSala().getNombreSala()
                    + " | " + funcion.getFechaHora().format(formato)
                    + " | " + funcion.getFormatoPelicula()
                    + " | $" + funcion.getPrecio());
        }

        if (!funcionesDisponibles.isEmpty()) {
            comboFuncion.getSelectionModel().selectFirst();
        }
    }

    public void setPrincipalController(PrincipalController principalController) {
        this.principalController = principalController;
    }

    /**
     * Que hace: devuelve la cantidad de entradas elegida en el desplegable.
     * Retorna: la cantidad (de 1 a 10).
     */
    private int obtenerCantidad() {
        return comboCantidad.getSelectionModel().getSelectedIndex() + 1;
    }

    /**
     * Que hace: al elegir una funcion, borra lo marcado y redibuja el plano.
     * Parametros: funcion (la funcion elegida).
     */
    private void elegirFuncion(Funcion funcion) {
        funcionElegida = funcion;
        entradas.clear();
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
     * Que hace: crea el cuadradito de una butaca. Gris si esta ocupada,
     * verde si esta seleccionada, y al hacer clic la marca o la desmarca.
     * Parametros: butaca (la butaca), numero (el numero que se muestra).
     * Retorna: el Label que representa la butaca.
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
        } else if (buscarPosicionEntrada(butaca) >= 0) {
            etiqueta.setStyle("-fx-background-color: #22C55E;");
        }

        etiqueta.setOnMouseClicked(evento -> alternarButaca(butaca));

        return etiqueta;
    }

    /**
     * Que hace: marca la butaca con el tipo de entrada elegido, o la desmarca
     * si ya estaba marcada. Las ocupadas no se pueden marcar, y tampoco se
     * pueden marcar mas butacas que la cantidad de entradas elegida.
     * Parametros: butaca (la butaca donde se hizo clic).
     */
    private void alternarButaca(Butaca butaca) {
        ocultarError();

        if (cine.getServicioDeReservas().estaOcupada(funcionElegida, butaca)) {
            mostrarError("La butaca " + butaca.getIdentificador() + " ya esta ocupada");
            return;
        }

        int posicion = buscarPosicionEntrada(butaca);

        if (posicion >= 0) {
            entradas.remove(posicion);
        } else {
            if (entradas.size() >= obtenerCantidad()) {
                mostrarError("Ya elegiste las " + obtenerCantidad()
                        + " butacas. Desmarca una o aumenta la cantidad de entradas");
                return;
            }
            entradas.add(crearEntrada(butaca));
        }

        dibujarButacas();
        actualizarResumen();
    }

    /**
     * Que hace: crea la entrada del tipo elegido en el desplegable.
     * Parametros: butaca (la butaca de la entrada).
     * Retorna: una EntradaGeneral, EntradaEstudiante o EntradaJubilados.
     */
    private Entrada crearEntrada(Butaca butaca) {
        double precio = funcionElegida.getPrecio();
        int tipo = comboTipoEntrada.getSelectionModel().getSelectedIndex();

        if (tipo == 1) {
            return new EntradaEstudiante(butaca, precio);
        }
        if (tipo == 2) {
            return new EntradaJubilados(butaca, precio);
        }
        return new EntradaGeneral(butaca, precio);
    }

    /**
     * Que hace: busca si una butaca ya esta marcada.
     * Retorna: la posicion en la lista de entradas, o -1 si no esta.
     */
    private int buscarPosicionEntrada(Butaca butaca) {
        for (int i = 0; i < entradas.size(); i++) {
            if (entradas.get(i).getButaca().equals(butaca)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Que hace: actualiza la lista de butacas marcadas y el total.
     */
    private void actualizarResumen() {
        ArrayList<String> textos = new ArrayList<>();
        double total = 0;

        for (Entrada entrada : entradas) {
            textos.add(entrada.getButaca().getIdentificador()
                    + " - " + entrada.getTipo()
                    + " - $" + entrada.calcularPrecioFinal());
            total += entrada.calcularPrecioFinal();
        }
        listaEntradas.setItems(FXCollections.observableArrayList(textos));
        etiquetaTotal.setText("Total: $" + total);
    }

    /**
     * Que hace: confirma la reserva con las butacas marcadas. Si el DNI no existe,
     * registra al cliente con los datos del formulario. Muestra el comprobante.
     * Se trabaja sobre una copia del cine: si algo falla, no se guarda nada.
     */
    @FXML
    private void confirmarReserva() {
        ocultarError();

        try {
            if (funcionElegida == null) {
                throw new IllegalArgumentException("Seleccione una funcion");
            }
            if (entradas.isEmpty()) {
                throw new IllegalArgumentException("Debe seleccionar al menos una butaca");
            }
            if (entradas.size() != obtenerCantidad()) {
                throw new IllegalArgumentException("Elegiste " + obtenerCantidad()
                        + " entradas pero marcaste " + entradas.size() + " butacas");
            }

            int dni;
            try {
                dni = Integer.parseInt(campoDni.getText().trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("El DNI debe ser un numero");
            }

            Cine cineTrabajo = cine.copiar();
            ServicioDeReservas servicio = cineTrabajo.getServicioDeReservas();
            Funcion funcion = cineTrabajo.buscarFuncionPorId(funcionElegida.getIdFuncion());

            // Si el cliente ya existe se usa ese; si no, se registra uno nuevo
            Cliente cliente = servicio.buscarClientePorDni(dni);
            if (cliente == null) {
                cliente = new Cliente(dni, campoNombre.getText(), campoApellido.getText(),
                        campoEmail.getText(), campoContrasenia.getText());
                servicio.registrarCliente(cliente);
            }

            Reserva reserva = cineTrabajo.confirmarReserva(cliente, funcion, entradas);

            persistencia.guardar(cineTrabajo);
            principalController.actualizarCine(cineTrabajo);
            this.cine = cineTrabajo;

            // Se muestra el comprobante y se limpia lo marcado
            areaComprobante.setText(servicio.generarComprobante(reserva));
            areaComprobante.setVisible(true);
            areaComprobante.setManaged(true);

            entradas.clear();
            dibujarButacas();
            actualizarResumen();

        } catch (IOException | ClassNotFoundException e) {
            // Error de datos (archivo / copia), distinto de un error de reglas
            mostrarError("Error al guardar los datos: " + e.getMessage());
        } catch (Exception e) {
            // Error de reglas de negocio: butaca ocupada, DNI repetido, datos invalidos, etc.
            mostrarError(e.getMessage());
        }
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
}