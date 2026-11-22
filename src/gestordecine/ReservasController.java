package gestordecine;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class ReservasController {

    private Cine cine;
    private PersistenciaCine persistencia;
    private PrincipalController principalController;

    @FXML
    private ComboBox<String> comboCriterio;
    @FXML
    private TextField campoBusqueda;
    @FXML
    private ListView<Reserva> listaReservas;
    @FXML
    private Label mensajeError;
    @FXML
    private Button botonComprobante;
    @FXML
    private Button botonCancelar;

    /**
     * Que hace: carga los criterios de busqueda, indica como se dibuja cada
     * reserva de la lista y avisa cuando se elige una.
     * JavaFX lo llama solo al abrir la pantalla.
     */
    @FXML
    private void initialize() {
        comboCriterio.getItems().addAll("Codigo", "DNI");
        comboCriterio.getSelectionModel().selectFirst();
        botonComprobante.setDisable(true);
        botonCancelar.setDisable(true);

        // Cada reserva se dibuja como una tarjeta
        listaReservas.setCellFactory(lista -> new ListCell<Reserva>() {
            {
                setStyle("-fx-background-color: transparent; -fx-padding: 4 0 4 0;");

                // al marcar o desmarcar la fila, se cambia el borde de la tarjeta
                selectedProperty().addListener((observable, antes, ahora) -> {
                    if (getItem() != null && getGraphic() != null) {
                        getGraphic().setStyle(estiloTarjeta(getItem().isConfirmada(), ahora));
                    }
                });
            }

            @Override
            protected void updateItem(Reserva reserva, boolean vacia) {
                super.updateItem(reserva, vacia);
                if (vacia || reserva == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    HBox tarjeta = crearTarjeta(reserva);
                    tarjeta.setStyle(estiloTarjeta(reserva.isConfirmada(), isSelected()));
                    setGraphic(tarjeta);
                    setText(null);
                }
            }
        });

        listaReservas.getSelectionModel().selectedItemProperty()
                .addListener((observable, anterior, nueva) -> mostrarSeleccion(nueva));
    }

    /**
     * Que hace: arma el estilo de una tarjeta: franja verde o roja a la
     * izquierda segun el estado, y borde azul si esta seleccionada.
     * Parametros: confirmada (true si la reserva esta confirmada),
     *             seleccionada (true si es la fila elegida).
     * Retorna: el texto de estilo (String).
     */
    private String estiloTarjeta(boolean confirmada, boolean seleccionada) {
        String fondo = confirmada ? "#131B2E" : "#2A1520";
        String acento = confirmada ? "#22C55E" : "#EF4444";
        String borde = seleccionada ? "#3B82F6" : "#1E293B";

        return "-fx-background-color: " + fondo + ";"
                + "-fx-background-radius: 10;"
                + "-fx-border-radius: 10;"
                + "-fx-border-width: 1 1 1 4;"
                + "-fx-border-color: " + borde + " " + borde + " " + borde + " " + acento + ";";
    }

    /**
     * Que hace: arma la tarjeta visual de una reserva, con el codigo, la
     * pelicula con su fecha y sala, el cliente, el estado y el importe.
     * Parametros: reserva (la reserva a dibujar).
     * Retorna: el HBox con la tarjeta.
     */
    private HBox crearTarjeta(Reserva reserva) {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // Codigo
        Label codigo = new Label(reserva.getCodigoReserva());
        codigo.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 17px;");
        VBox bloqueCodigo = new VBox(codigo);
        bloqueCodigo.setAlignment(Pos.CENTER_LEFT);
        bloqueCodigo.setPrefWidth(90);

        // Pelicula, fecha y sala
        Label pelicula = new Label(reserva.getFuncion().getPelicula().getTituloPelicula());
        pelicula.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        Label fecha = new Label(reserva.getFuncion().getFechaHora().format(formato));
        fecha.setStyle("-fx-text-fill: #CBD5E1;");
        Label sala = new Label(reserva.getFuncion().getSala().getNombreSala());
        sala.setStyle("-fx-text-fill: #94A3B8;");
        VBox bloquePelicula = new VBox(3, pelicula, fecha, sala);
        bloquePelicula.setAlignment(Pos.CENTER_LEFT);
        bloquePelicula.setPrefWidth(230);

        // Cliente
        Label nombre = new Label(reserva.getCliente().getNombre() + " "
                + reserva.getCliente().getApellido());
        nombre.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        Label dni = new Label("DNI " + reserva.getCliente().getDni());
        dni.setStyle("-fx-text-fill: #94A3B8;");
        VBox bloqueCliente = new VBox(3, nombre, dni);
        bloqueCliente.setAlignment(Pos.CENTER_LEFT);

        // Estado e importe
        Label estado = new Label();
        if (reserva.isConfirmada()) {
            estado.setText("CONFIRMADA");
            estado.setStyle("-fx-text-fill: #4ADE80; -fx-font-weight: bold;"
                    + "-fx-background-color: #14532D; -fx-background-radius: 12;"
                    + "-fx-padding: 3 12 3 12;");
        } else {
            estado.setText("CANCELADA");
            estado.setStyle("-fx-text-fill: #FCA5A5; -fx-font-weight: bold;"
                    + "-fx-background-color: #7F1D1D; -fx-background-radius: 12;"
                    + "-fx-padding: 3 12 3 12;");
        }
        Label importe = new Label(String.format(Locale.of("es", "AR"), "$%,.2f",
                reserva.getImporteTotal()));
        importe.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        VBox bloqueEstado = new VBox(8, estado, importe);
        bloqueEstado.setAlignment(Pos.CENTER_RIGHT);

        // Lineas verticales que separan los bloques
        Region separador1 = new Region();
        separador1.setMinWidth(1);
        separador1.setMaxWidth(1);
        separador1.setStyle("-fx-background-color: #1E293B;");
        Region separador2 = new Region();
        separador2.setMinWidth(1);
        separador2.setMaxWidth(1);
        separador2.setStyle("-fx-background-color: #1E293B;");

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        HBox tarjeta = new HBox(18, bloqueCodigo, separador1, bloquePelicula,
                separador2, bloqueCliente, espacio, bloqueEstado);
        tarjeta.setAlignment(Pos.CENTER_LEFT);
        tarjeta.setPadding(new Insets(12, 18, 12, 18));
        return tarjeta;
    }

    /**
     * Que hace: recibe el cine y la persistencia y muestra todas las reservas.
     * Parametros: cine (el cine actual), persistencia (para guardar cambios).
     */
    public void configurar(Cine cine, PersistenciaCine persistencia) {
        this.cine = cine;
        this.persistencia = persistencia;
        buscar();
    }

    public void setPrincipalController(PrincipalController principalController) {
        this.principalController = principalController;
    }

    /**
     * Que hace: busca reservas por codigo o por DNI. Si el campo esta vacio,
     * muestra todas las reservas.
     */
    @FXML
    private void buscar() {
        ocultarError();

        ServicioDeReservas servicio = cine.getServicioDeReservas();
        String texto = campoBusqueda.getText().trim();
        ArrayList<Reserva> resultado;

        try {
            if (texto.isEmpty()) {
                resultado = servicio.getReservas();
            } else if (comboCriterio.getSelectionModel().getSelectedIndex() == 0) {
                resultado = new ArrayList<>();
                Reserva encontrada = servicio.buscarPorCodigo(texto.toUpperCase());
                if (encontrada != null) {
                    resultado.add(encontrada);
                }
            } else {
                resultado = servicio.buscarPorDni(Integer.parseInt(texto));
            }
        } catch (NumberFormatException e) {
            mostrarError("El DNI debe ser un numero");
            return;
        }

        mostrarLista(resultado);

        if (resultado.isEmpty() && !texto.isEmpty()) {
            mostrarError("No se encontraron reservas");
        }
    }

    @FXML
    private void verTodas() {
        campoBusqueda.clear();
        buscar();
    }

    /**
     * Que hace: muestra las reservas en la lista, una tarjeta por reserva.
     * Parametros: reservas (las reservas a mostrar).
     */
    private void mostrarLista(ArrayList<Reserva> reservas) {
        listaReservas.setItems(FXCollections.observableArrayList(reservas));
        listaReservas.getSelectionModel().clearSelection();
        mostrarSeleccion(null);
    }

    /**
     * Que hace: al elegir una reserva de la lista, habilita los botones.
     * El boton de cancelar solo se habilita si la reserva esta confirmada.
     * Parametros: reserva (la reserva elegida, null si no hay ninguna).
     */
    private void mostrarSeleccion(Reserva reserva) {
        botonComprobante.setDisable(reserva == null);
        botonCancelar.setDisable(reserva == null || !reserva.isConfirmada());
    }

    /**
     * Que hace: muestra el comprobante de la reserva seleccionada en una
     * ventana emergente con formato de ticket.
     */
    @FXML
    private void verComprobante() {
        Reserva seleccionada = listaReservas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            return;
        }

        VentanaComprobante.mostrar(seleccionada);
    }

    /**
     * Que hace: cancela la reserva seleccionada. La reserva no se borra: queda
     * como cancelada y sus butacas se liberan. Se trabaja sobre una copia del cine.
     */
    @FXML
    private void cancelarReserva() {
        ocultarError();

        Reserva seleccionada = listaReservas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarError("Seleccione una reserva");
            return;
        }

        try {
            String codigo = seleccionada.getCodigoReserva();

            Cine cineTrabajo = cine.copiar();
            cineTrabajo.getServicioDeReservas().cancelarReserva(codigo);

            persistencia.guardar(cineTrabajo);
            principalController.actualizarCine(cineTrabajo);
            this.cine = cineTrabajo;

            // Se vuelve a cargar la lista y se deja marcada la misma reserva
            buscar();
            for (Reserva reserva : listaReservas.getItems()) {
                if (reserva.getCodigoReserva().equals(codigo)) {
                    listaReservas.getSelectionModel().select(reserva);
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            // Error de datos (archivo / copia), distinto de un error de reglas
            mostrarError("Error al guardar los datos: " + e.getMessage());
        } catch (Exception e) {
            // Error de reglas de negocio
            mostrarError(e.getMessage());
        }
    }

    @FXML
    private void nuevaReserva() {
        principalController.mostrarNuevaReserva();
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