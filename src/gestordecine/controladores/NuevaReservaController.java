package gestordecine.controladores;

import gestordecine.Butaca;
import gestordecine.Cine;
import gestordecine.Cliente;
import gestordecine.Entrada;
import gestordecine.EntradaEstudiante;
import gestordecine.EntradaGeneral;
import gestordecine.EntradaJubilados;
import gestordecine.Funcion;
import gestordecine.Pelicula;
import gestordecine.PersistenciaCine;
import gestordecine.Reserva;
import gestordecine.Sala;
import gestordecine.ServicioDeReservas;
import gestordecine.VentanaComprobante;
import gestordecine.sesion.SesionCliente;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Controla la selección y compra de entradas para funciones del cine. Utiliza
 * el acceso centralizado y la sesión vigente del cliente.
 */
public class NuevaReservaController {

    private static final double ANCHO_BUTACA = 28;
    private static final double ESPACIO_ENTRE_ELEMENTOS = 8;
    private static final double ANCHO_PASILLO = 26;
    private static final double ANCHO_ETIQUETA_FILA = 24;
    private static final int MAXIMO_POR_TIPO = 10;

    private Cine cine;
    private PersistenciaCine persistencia;
    private PrincipalController principalController;
    private boolean volverCarteleraSiCancela = false;

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
    private Label mensajeError;
    @FXML
    private Button botonConfirmar;
    @FXML
    private Button botonAbrirAcceso;

    /**
     * Obtiene el cliente que tiene una sesión iniciada.
     *
     * @return Cliente autenticado o null si no existe una sesión activa.
     */
    public static Cliente getClienteActual() {
        return SesionCliente.getClienteActual();
    }

    /**
     * Carga las cantidades (0 a 10) y avisa cuando se elige otra película,
     * otra función o se cambia una cantidad. JavaFX lo llama solo al abrir la
     * pantalla.
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
    }

    /**
     * Configura la pantalla de nueva reserva.
     *
     * Carga únicamente las películas que tengan al menos una función futura,
     * activa y con butacas disponibles.
     *
     * @param cine Cine que contiene las películas y funciones.
     * @param persistencia Servicio encargado de guardar los datos.
     */
    public void configurar(Cine cine, PersistenciaCine persistencia) {

        this.cine = cine;
        this.persistencia = persistencia;

        peliculasDisponibles = new ArrayList<>();
        funcionesFiltradas = new ArrayList<>();
        funcionElegida = null;
        butacasMarcadas.clear();

        comboPelicula.getItems().clear();
        comboFuncion.getItems().clear();

        for (Funcion funcion : cine.buscarFuncionesFuturas()) {

            // La función debe estar activa.
            if (!funcion.isActiva()) {
                continue;
            }

            // La sala y la película deben estar activas.
            if (!funcion.getSala().isActiva()
                    || !funcion.getPelicula().isActiva()) {
                continue;
            }

            // La función debe tener al menos una butaca disponible.
            if (cine.getServicioDeReservas()
                    .obtenerButacasDisponibles(funcion) == 0) {
                continue;
            }

            // Evitar agregar la misma película varias veces.
            boolean yaEsta = false;

            for (Pelicula pelicula : peliculasDisponibles) {

                if (pelicula.getIdPelicula()
                        == funcion.getPelicula().getIdPelicula()) {

                    yaEsta = true;
                    break;
                }
            }

            if (!yaEsta) {

                peliculasDisponibles.add(funcion.getPelicula());

                comboPelicula.getItems().add(
                        funcion.getPelicula().getTituloPelicula()
                );
            }
        }

        // Seleccionar la primera película que tenga funciones disponibles.
        if (!peliculasDisponibles.isEmpty()) {
            comboPelicula.getSelectionModel().selectFirst();
        }

        actualizarSesion();
    }

    /**
     * Guarda el controlador principal, que se usa para abrir el acceso, cerrar
     * sesión y actualizar el cine.
     *
     * @param principalController Controlador de la ventana principal.
     */
    public void setPrincipalController(PrincipalController principalController) {
        this.principalController = principalController;
    }

    /**
     * Abre el acceso cuando se llega desde la cartelera sin haber iniciado
     * sesión. Si el usuario cancela el acceso, vuelve a la cartelera. Si ya
     * tiene sesión, no hace nada.
     */
    public void abrirAccesoDesdeCartelera() {

        // Si ya inició sesión, puede reservar directamente.
        if (SesionCliente.haySesion()) {
            return;
        }

        // Si cancela este acceso, debe regresar a Cartelera.
        volverCarteleraSiCancela = true;

        abrirAcceso();
    }

    /**
     * Deja elegidas la película y la función que se pasan, por ejemplo cuando
     * se llega desde el detalle de una película. Se llama después de
     * configurar.
     *
     * @param funcion Función que se quiere reservar.
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
     * Actualiza la interfaz según el estado de la sesión.
     *
     * Si el cliente ya no existe en el cine, cierra su sesión. Bloquea las
     * compras cuando no hay un cliente autenticado.
     */
    private void actualizarSesion() {

        Cliente clienteActual = SesionCliente.getClienteActual();

        // Comprobar que el cliente siga registrado en el cine.
        if (clienteActual != null
                && cine.getServicioDeReservas()
                        .buscarClientePorDni(clienteActual.getDni()) == null) {

            SesionCliente.cerrarSesion();
            clienteActual = null;
        }

        boolean conSesion = clienteActual != null;

        botonAbrirAcceso.setVisible(!conSesion);
        botonAbrirAcceso.setManaged(!conSesion);

        panelSesion.setVisible(conSesion);
        panelSesion.setManaged(conSesion);

        tarjetaCompra.setDisable(!conSesion);
        panelResumen.setDisable(!conSesion);

        if (conSesion) {
            etiquetaCliente.setText(
                    "Cliente: " + clienteActual.getNombre() + " "
                    + clienteActual.getApellido()
                    + " (DNI " + clienteActual.getDni() + ")"
            );
        }
    }

    /**
     * Cierra la sesión del cliente y limpia los datos seleccionados para una
     * posible compra.
     *
     * No elimina los clientes ni las reservas persistidas.
     */
    @FXML
    private void cerrarSesion() {

        SesionCliente.cerrarSesion();
        principalController.actualizarMenuCuenta();

        butacasMarcadas.clear();
        ocultarError();
        dibujarButacas();
        actualizarResumen();
        actualizarSesion();
    }

    /**
     * Carga en el desplegable las funciones futuras y activas de una película,
     * dejando afuera las que no tienen butacas disponibles.
     *
     * @param pelicula Película elegida.
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
            // No ofrecer funciones que no tengan butacas disponibles.
            if (cine.getServicioDeReservas()
                    .obtenerButacasDisponibles(funcion) == 0) {
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
     * Al elegir una función, borra lo marcado y redibuja el plano de la sala.
     *
     * @param funcion Función elegida.
     */
    private void elegirFuncion(Funcion funcion) {
        funcionElegida = funcion;
        butacasMarcadas.clear();
        ocultarError();
        dibujarButacas();
        actualizarResumen();
    }

    /**
     * Suma las entradas de los tres tipos (general, estudiante y jubilado).
     *
     * @return La cantidad total de entradas elegida.
     */
    private int obtenerTotalEntradas() {
        return comboGeneral.getSelectionModel().getSelectedIndex()
                + comboEstudiante.getSelectionModel().getSelectedIndex()
                + comboJubilado.getSelectionModel().getSelectedIndex();
    }

    /**
     * Cuando baja la cantidad de entradas, saca las últimas butacas marcadas
     * que sobran. Lo que ya estaba bien marcado se conserva.
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
     * Dibuja el plano de la sala de la función elegida, con los pasillos según
     * la distribución (por ejemplo 4-8-4).
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
     * Crea el cuadradito de una butaca. Es gris si está ocupada y verde si
     * está seleccionada. Al hacer click la marca o la desmarca.
     *
     * @param butaca Butaca que se dibuja.
     * @param numero Número que se muestra en el cuadradito.
     * @return El Label que representa la butaca.
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
     * Marca la butaca o la desmarca si ya estaba marcada. Las ocupadas no se
     * pueden marcar, y tampoco más butacas que la cantidad total de entradas
     * elegida.
     *
     * @param butaca Butaca donde se hizo click.
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
     * Arma las entradas a partir de las butacas marcadas. Las primeras son
     * generales, después estudiantes y al final jubilados, según las
     * cantidades elegidas.
     *
     * @return La lista de entradas (vacía si no hay función elegida).
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
     * Actualiza la lista de butacas marcadas y el total a pagar.
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
     * Confirma la reserva con las butacas marcadas para el cliente que inició
     * sesión. Muestra el comprobante en una ventana y limpia la pantalla.
     *
     * Trabaja sobre una copia del cine: si algo falla, no se guarda nada. Los
     * errores de reglas (butaca ocupada, datos inválidos) y los de datos
     * (archivo o copia) se muestran en pantalla.
     */
    @FXML
    private void confirmarReserva() {
        ocultarError();

        try {
            if (!SesionCliente.haySesion()) {
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

            // Verificar la disponibilidad actual antes de confirmar la compra.
            int disponibles = servicio.obtenerButacasDisponibles(funcion);

            if (disponibles == 0) {
                throw new IllegalStateException(
                        "La función está agotada. No quedan butacas disponibles."
                );
            }

            if (obtenerTotalEntradas() > disponibles) {
                throw new IllegalStateException(
                        "Solo quedan " + disponibles
                        + " butacas disponibles para esta función."
                );
            }

            Cliente cliente = servicio.buscarClientePorDni(
                    SesionCliente.getClienteActual().getDni()
            );

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
     * Deja la compra como nueva: borra las butacas marcadas y vuelve la
     * cantidad a 1 entrada general. La sesión sigue iniciada.
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

    /**
     * Abre el formulario de acceso centralizado de CINEMA.
     *
     * Reutiliza el formulario administrado por PrincipalController y actualiza
     * el estado de la compra cuando se cierra.
     */
    @FXML
    private void abrirAcceso() {

        // Abrir el mismo formulario utilizado desde el menú principal.
        principalController.mostrarAcceso();

        // El registro puede haber creado una nueva copia de Cine.
        // Recuperar la referencia vigente antes de comprobar la sesión.
        this.cine = principalController.obtenerCineActual();

        // Actualizar la compra según el resultado del acceso.
        actualizarSesion();

        // Si llegó desde Cartelera y canceló el ingreso, regresar.
        if (volverCarteleraSiCancela && !SesionCliente.haySesion()) {
            principalController.mostrarCartelera();
        }

        volverCarteleraSiCancela = false;
    }

    /**
     * Muestra un mensaje de error en la pantalla.
     *
     * @param mensaje Texto del error que se quiere mostrar.
     */
    private void mostrarError(String mensaje) {
        mensajeError.setText(mensaje);
        mensajeError.setVisible(true);
        mensajeError.setManaged(true);
    }

    /**
     * Oculta el mensaje de error de la pantalla.
     */
    private void ocultarError() {
        mensajeError.setVisible(false);
        mensajeError.setManaged(false);
    }

}