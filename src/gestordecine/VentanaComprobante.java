package gestordecine;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Muestra el comprobante de una reserva en una ventana emergente con formato de
 * ticket. Se usa desde Nueva reserva y desde Reservas.
 */
public class VentanaComprobante {

    private VentanaComprobante() {
    }

    /**
     * Muestra el comprobante de una reserva en una ventana emergente.
     *
     * Presenta los datos del cliente, función, entradas, importe total y estado
     * actual de la reserva, ya sea confirmada o cancelada.
     *
     * @param reserva Reserva cuyos datos se mostrarán.
     *
     * @throws IllegalArgumentException Si la reserva es nula.
     */
    public static void mostrar(Reserva reserva) {

        if (reserva == null) {
            throw new IllegalArgumentException(
                    "La reserva no puede ser nula"
            );
        }
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Funcion funcion = reserva.getFuncion();
        Cliente cliente = reserva.getCliente();

        VBox ticket = new VBox(10);
        ticket.setPadding(new Insets(22));
        ticket.setPrefWidth(340);
        ticket.setStyle("-fx-background-color: #131B2E; -fx-background-radius: 12;"
                + "-fx-border-color: #1E293B; -fx-border-radius: 12;");

        // Encabezado
        Label marca = new Label("CINEMA");
        marca.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 20px;");
        Label titulo = new Label("Comprobante de reserva");
        titulo.setStyle("-fx-text-fill: #94A3B8;");
        Label codigo = new Label(reserva.getCodigoReserva());
        codigo.setStyle("-fx-text-fill: #60A5FA; -fx-font-weight: bold; -fx-font-size: 28px;");

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

        VBox encabezado = new VBox(6, marca, titulo, codigo, estado);
        encabezado.setAlignment(Pos.CENTER);

        ticket.getChildren().addAll(encabezado, crearLinea());

        // Datos de la funcion y del cliente
        ticket.getChildren().add(crearFila("Película",
                funcion.getPelicula().getTituloPelicula(), false));
        ticket.getChildren().add(crearFila("Fecha", funcion.getFechaHora().format(formato), false));
        ticket.getChildren().add(crearFila("Sala", funcion.getSala().getNombreSala(), false));
        ticket.getChildren().add(crearFila("Cliente",
                cliente.getNombre() + " " + cliente.getApellido(), false));
        ticket.getChildren().add(crearFila("DNI", String.valueOf(cliente.getDni()), false));

        ticket.getChildren().add(crearLinea());

        // Entradas
        Label entradas = new Label("Entradas");
        entradas.setStyle("-fx-text-fill: #94A3B8;");
        ticket.getChildren().add(entradas);

        for (Entrada entrada : reserva.getEntradas()) {
            ticket.getChildren().add(crearFila(
                    entrada.getButaca().getIdentificador() + "  " + entrada.getTipo(),
                    darFormatoDinero(entrada.calcularPrecioFinal()), false));
        }

        ticket.getChildren().add(crearLinea());
        ticket.getChildren().add(crearFila("TOTAL",
                darFormatoDinero(reserva.getImporteTotal()), true));

        Alert ventana = new Alert(Alert.AlertType.INFORMATION);
        ventana.setTitle("Comprobante");
        ventana.setHeaderText(null);
        ventana.setGraphic(null);
        ventana.getDialogPane().setStyle("-fx-background-color: #0F172A; -fx-base: #1E293B;");
        ventana.getDialogPane().setContent(ticket);
        ventana.showAndWait();
    }

    /**
     * Crea una fila horizontal con un texto a la izquierda y otro a la derecha,
     * separados por un espacio flexible.
     *
     * @param izquierda Texto que se muestra a la izquierda.
     * @param derecha Texto que se muestra a la derecha.
     * @param destacada Indica si los textos deben resaltarse, como ocurre con
     * el importe total.
     *
     * @return Un HBox que contiene los textos y el espacio flexible.
     */
    private static HBox crearFila(String izquierda, String derecha, boolean destacada) {
        Label textoIzquierda = new Label(izquierda);
        Label textoDerecha = new Label(derecha);

        if (destacada) {
            textoIzquierda.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
            textoDerecha.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 20px;");
        } else {
            textoIzquierda.setStyle("-fx-text-fill: #94A3B8;");
            textoDerecha.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        }

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        return new HBox(10, textoIzquierda, espacio, textoDerecha);
    }

    /**
     * Crea una línea horizontal para separar visualmente las secciones del
     * comprobante.
     *
     * @return Un Region de un píxel de altura que representa la línea
     * divisoria.
     */
    private static Region crearLinea() {
        Region linea = new Region();
        linea.setMinHeight(1);
        linea.setMaxHeight(1);
        linea.setStyle("-fx-background-color: #1E293B;");
        return linea;
    }

    /**
     * Convierte un importe numérico a texto utilizando el formato monetario
     * argentino.
     *
     * Por ejemplo, 15000 se representa como $15.000,00.
     *
     * @param importe Importe que se desea formatear.
     *
     * @return Una cadena de texto con el importe formateado.
     */
    private static String darFormatoDinero(double importe) {
        return String.format(Locale.of("es", "AR"), "$%,.2f", importe);
    }
}
