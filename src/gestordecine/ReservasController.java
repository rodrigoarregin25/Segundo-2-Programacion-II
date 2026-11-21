package gestordecine;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ReservasController {

    private Cine cine;
    private PersistenciaCine persistencia;
    private PrincipalController principalController;

    // Reservas que se muestran en la lista (misma posicion que en el ListView)
    private ArrayList<Reserva> reservasMostradas = new ArrayList<>();

    @FXML
    private ComboBox<String> comboCriterio;
    @FXML
    private TextField campoBusqueda;
    @FXML
    private ListView<String> listaReservas;
    @FXML
    private TextArea areaComprobante;
    @FXML
    private Label mensajeError;
    @FXML
    private Button botonCancelar;

    /**
     * Que hace: carga los criterios de busqueda y avisa cuando se elige una reserva
     * de la lista. JavaFX lo llama solo al abrir la pantalla.
     */
    @FXML
    private void initialize() {
        comboCriterio.getItems().addAll("Codigo", "DNI");
        comboCriterio.getSelectionModel().selectFirst();
        botonCancelar.setDisable(true);

        listaReservas.getSelectionModel().selectedIndexProperty()
                .addListener((observable, anterior, nuevo)
                        -> mostrarSeleccion(nuevo.intValue()));
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
     * Que hace: muestra las reservas en la lista, una linea por reserva.
     * Parametros: reservas (las reservas a mostrar).
     */
    private void mostrarLista(ArrayList<Reserva> reservas) {
        reservasMostradas = reservas;
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        ArrayList<String> textos = new ArrayList<>();

        for (Reserva reserva : reservas) {
            String estado = reserva.isConfirmada() ? "Confirmada" : "Cancelada";

            textos.add(reserva.getCodigoReserva()
                    + " | DNI " + reserva.getCliente().getDni()
                    + " | " + reserva.getFuncion().getPelicula().getTituloPelicula()
                    + " | " + reserva.getFuncion().getFechaHora().format(formato)
                    + " | " + estado
                    + " | $" + reserva.getImporteTotal());
        }

        listaReservas.setItems(FXCollections.observableArrayList(textos));
        listaReservas.getSelectionModel().clearSelection();
        mostrarSeleccion(-1);
    }

    /**
     * Que hace: al elegir una reserva de la lista, muestra su comprobante.
     * El boton de cancelar solo se habilita si la reserva esta confirmada.
     * Parametros: posicion (posicion elegida en la lista, -1 si no hay ninguna).
     */
    private void mostrarSeleccion(int posicion) {
        if (posicion < 0 || posicion >= reservasMostradas.size()) {
            areaComprobante.clear();
            botonCancelar.setDisable(true);
            return;
        }

        Reserva reserva = reservasMostradas.get(posicion);
        areaComprobante.setText(cine.getServicioDeReservas().generarComprobante(reserva));
        botonCancelar.setDisable(!reserva.isConfirmada());
    }

    /**
     * Que hace: cancela la reserva seleccionada. La reserva no se borra: queda
     * como cancelada y sus butacas se liberan. Se trabaja sobre una copia del cine.
     */
    @FXML
    private void cancelarReserva() {
        ocultarError();

        int posicion = listaReservas.getSelectionModel().getSelectedIndex();
        if (posicion < 0) {
            mostrarError("Seleccione una reserva");
            return;
        }

        try {
            String codigo = reservasMostradas.get(posicion).getCodigoReserva();

            Cine cineTrabajo = cine.copiar();
            cineTrabajo.getServicioDeReservas().cancelarReserva(codigo);

            persistencia.guardar(cineTrabajo);
            principalController.actualizarCine(cineTrabajo);
            this.cine = cineTrabajo;

            // Se vuelve a cargar la lista y se deja marcada la misma reserva
            buscar();
            if (posicion < reservasMostradas.size()) {
                listaReservas.getSelectionModel().select(posicion);
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