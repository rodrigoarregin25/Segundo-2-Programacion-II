package gestordecine;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Muestra el comprobante en formato horizontal, con QR y exportación a PDF.
 *
 * Mantiene la firma pública mostrar(Reserva) usada por el resto de CINEMA.
 * Solo presenta información: no modifica las reservas ni su persistencia.
 */
public final class VentanaComprobante {

    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Ancho del comprobante, compartido por la vista y la versión imprimible. */
    private static final double ANCHO_TICKET = 760;

    private VentanaComprobante() {
    }

    /** Abre el comprobante sin alterar el estado de la reserva. */
    public static void mostrar(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva no puede ser nula");
        }

        try {
            // La misma imagen QR se utiliza en el diálogo y en el PDF.
            Image qr = GeneradorQRComprobante.crearImagen(reserva);

            Stage ventana = new Stage();
            ventana.setTitle("CINEMA | Comprobante " + reserva.getCodigoReserva());
            ventana.initModality(Modality.APPLICATION_MODAL);
            ventana.setResizable(true);
            ventana.setMinWidth(800);
            ventana.setMinHeight(450);

            VBox ticket = crearTicket(reserva, qr, false);

            // En reservas habituales el comprobante entra completo y no hay barras.
            // Si una reserva tiene muchas entradas, se permite desplazarse para
            // no recortar información ni abrir una ventana mayor a la pantalla.
            ScrollPane desplazamiento = new ScrollPane(ticket);
            desplazamiento.setFitToWidth(true);
            desplazamiento.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            desplazamiento.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            desplazamiento.setStyle("-fx-background: #0B1220;"
                    + "-fx-background-color: transparent; -fx-border-color: transparent;");

            Button descargar = new Button("Descargar PDF");
            descargar.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white;"
                    + "-fx-font-weight: bold; -fx-padding: 10 20 10 20;"
                    + "-fx-background-radius: 7; -fx-cursor: hand;");
            descargar.setOnAction(evento -> guardarPdf(reserva, qr, ventana));

            Button cerrar = new Button("Cerrar");
            cerrar.setStyle("-fx-background-color: #1E293B; -fx-text-fill: #E2E8F0;"
                    + "-fx-padding: 10 20 10 20; -fx-background-radius: 7;"
                    + "-fx-cursor: hand;");
            cerrar.setCancelButton(true);
            cerrar.setOnAction(evento -> ventana.close());

            HBox acciones = new HBox(10, descargar, cerrar);
            acciones.setAlignment(Pos.CENTER_RIGHT);
            acciones.setPadding(new Insets(9, 0, 0, 0));

            BorderPane contenedor = new BorderPane();
            contenedor.setPadding(new Insets(12));
            contenedor.setStyle("-fx-background-color: #0B1220;");
            contenedor.setCenter(desplazamiento);
            contenedor.setBottom(acciones);

            ventana.setScene(new Scene(contenedor, 805, 565));
            ventana.showAndWait();
        } catch (IOException | RuntimeException e) {
            mostrarError("No se pudo mostrar el comprobante", e);
        }
    }

    /**
     * Crea una tarjeta compacta: información a la izquierda y QR a la derecha.
     * Se usa también como contenido del PDF, cambiando únicamente los colores.
     */
    private static VBox crearTicket(Reserva reserva, Image qr, boolean impresion) {
        String fondo = impresion ? "#FFFFFF" : "#131B2E";
        String principal = impresion ? "#0F172A" : "#F8FAFC";
        String secundario = impresion ? "#475569" : "#94A3B8";
        String linea = impresion ? "#CBD5E1" : "#273449";
        String panel = impresion ? "#EFF6FF" : "#18253C";

        VBox ticket = new VBox(11);
        ticket.setPadding(new Insets(18));
        ticket.setMinWidth(ANCHO_TICKET);
        ticket.setPrefWidth(ANCHO_TICKET);
        ticket.setMaxWidth(ANCHO_TICKET);
        ticket.setStyle("-fx-background-color: " + fondo + ";"
                + "-fx-background-radius: 10;"
                + "-fx-border-color: " + linea + "; -fx-border-radius: 10;");

        // Encabezado horizontal, sin ocupar varias líneas en vertical.
        Label marca = etiqueta("CINEMA", principal, 24, true);
        Label subtitulo = etiqueta("COMPROBANTE DE RESERVA", secundario, 10, true);
        VBox identidad = new VBox(2, marca, subtitulo);
        identidad.setAlignment(Pos.CENTER_LEFT);

        Label codigo = etiqueta(reserva.getCodigoReserva(), "#3B82F6", 25, true);
        Label estado = etiqueta(reserva.isConfirmada() ? "CONFIRMADA" : "CANCELADA",
                impresion ? (reserva.isConfirmada() ? "#166534" : "#991B1B")
                        : (reserva.isConfirmada() ? "#4ADE80" : "#FCA5A5"), 11, true);
        estado.setPadding(new Insets(3, 12, 3, 12));
        estado.setStyle(estado.getStyle() + "-fx-background-color: "
                + (impresion ? (reserva.isConfirmada() ? "#DCFCE7" : "#FEE2E2")
                        : (reserva.isConfirmada() ? "#14532D" : "#7F1D1D"))
                + "; -fx-background-radius: 14;");
        HBox reservaId = new HBox(12, codigo, estado);
        reservaId.setAlignment(Pos.CENTER_RIGHT);

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        HBox encabezado = new HBox(12, identidad, espacio, reservaId);
        encabezado.setAlignment(Pos.CENTER_LEFT);
        ticket.getChildren().addAll(encabezado, crearLinea(linea));

        // Columna izquierda: título completo, datos y detalle de entradas.
        VBox detalles = new VBox(9);
        detalles.setPrefWidth(484);
        detalles.setMinWidth(484);
        detalles.setMaxWidth(484);

        Label tituloPelicula = etiqueta(
                reserva.getFuncion().getPelicula().getTituloPelicula(), principal, 17, true);
        tituloPelicula.setWrapText(true);
        tituloPelicula.setMaxWidth(456);
        tituloPelicula.setMinHeight(Region.USE_PREF_SIZE);

        VBox bloquePelicula = new VBox(4,
                etiqueta("PELÍCULA", secundario, 10, true), tituloPelicula);
        bloquePelicula.setPadding(new Insets(10, 12, 10, 12));
        bloquePelicula.setStyle("-fx-background-color: " + panel + ";"
                + "-fx-background-radius: 7;");
        detalles.getChildren().add(bloquePelicula);

        Funcion funcion = reserva.getFuncion();
        Cliente cliente = reserva.getCliente();
        VBox datos = new VBox(5,
                crearFila("Fecha y hora", funcion.getFechaHora().format(FECHA), principal, secundario, false),
                crearFila("Sala", funcion.getSala().getNombreSala(), principal, secundario, false),
                crearFila("Cliente", cliente.getNombre() + " " + cliente.getApellido(), principal, secundario, false),
                crearFila("DNI", String.valueOf(cliente.getDni()), principal, secundario, false));
        detalles.getChildren().addAll(datos, crearLinea(linea));

        VBox entradas = new VBox(6);
        entradas.getChildren().add(etiqueta("ENTRADAS", secundario, 10, true));
        for (Entrada entrada : reserva.getEntradas()) {
            entradas.getChildren().add(crearFila(
                    entrada.getButaca().getIdentificador() + " · " + entrada.getTipo(),
                    darFormatoDinero(entrada.calcularPrecioFinal()),
                    principal, secundario, false));
        }
        detalles.getChildren().addAll(entradas, crearLinea(linea),
                crearFila("TOTAL", darFormatoDinero(reserva.getImporteTotal()),
                        principal, secundario, true));

        // Columna derecha: QR siempre íntegro y con fondo blanco para escanear.
        ImageView imagenQr = new ImageView(qr);
        imagenQr.setFitWidth(195);
        imagenQr.setFitHeight(195);
        imagenQr.setPreserveRatio(true);
        imagenQr.setSmooth(false);
        VBox fondoQr = new VBox(imagenQr);
        fondoQr.setAlignment(Pos.CENTER);
        fondoQr.setPadding(new Insets(8));
        fondoQr.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 8;");

        Label instruccionesQr = etiqueta("Escaneá para ver los datos de la reserva",
                secundario, 11, false);
        instruccionesQr.setWrapText(true);
        instruccionesQr.setMaxWidth(212);
        instruccionesQr.setAlignment(Pos.CENTER);
        instruccionesQr.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        VBox sectorQr = new VBox(10, fondoQr, instruccionesQr);
        sectorQr.setAlignment(Pos.TOP_CENTER);
        sectorQr.setPrefWidth(220);
        sectorQr.setMinWidth(220);
        sectorQr.setMaxWidth(220);

        HBox contenido = new HBox(16, detalles, sectorQr);
        contenido.setAlignment(Pos.TOP_LEFT);
        ticket.getChildren().add(contenido);

        Label aviso = etiqueta(reserva.isConfirmada()
                ? "Presentá el comprobante o el código de reserva al ingresar."
                : "Reserva cancelada: este comprobante no habilita el ingreso.",
                secundario, 11, false);
        aviso.setWrapText(true);
        aviso.setMaxWidth(Double.MAX_VALUE);
        aviso.setAlignment(Pos.CENTER);
        ticket.getChildren().add(aviso);
        return ticket;
    }

    /** Genera una etiqueta con los estilos del tema seleccionado. */
    private static Label etiqueta(String texto, String color, int tamano, boolean negrita) {
        Label label = new Label(texto == null ? "" : texto);
        label.setStyle("-fx-text-fill: " + color + "; -fx-font-size: " + tamano + "px;"
                + (negrita ? " -fx-font-weight: bold;" : ""));
        label.setMinWidth(0);
        return label;
    }

    /**
     * Crea una fila con descripción flexible y precio/valor alineado a la derecha.
     * Evita puntos suspensivos en nombres largos de tipos de entrada.
     */
    private static HBox crearFila(String nombre, String valor, String principal,
            String secundario, boolean destacada) {
        Label izquierda = etiqueta(nombre, destacada ? principal : secundario,
                destacada ? 16 : 12, destacada);
        izquierda.setWrapText(true);
        izquierda.setMinHeight(Region.USE_PREF_SIZE);
        izquierda.setMaxWidth(Double.MAX_VALUE);
        izquierda.setPrefWidth(0);
        HBox.setHgrow(izquierda, Priority.ALWAYS);

        Label derecha = etiqueta(valor, principal, destacada ? 21 : 12, true);
        derecha.setMinWidth(142);
        derecha.setPrefWidth(142);
        derecha.setAlignment(Pos.CENTER_RIGHT);
        derecha.setTextAlignment(javafx.scene.text.TextAlignment.RIGHT);

        HBox fila = new HBox(8, izquierda, derecha);
        fila.setAlignment(Pos.TOP_LEFT);
        return fila;
    }

    private static Region crearLinea(String color) {
        Region linea = new Region();
        linea.setMinHeight(1);
        linea.setMaxHeight(1);
        linea.setStyle("-fx-background-color: " + color + ";");
        return linea;
    }

    /** Solicita un archivo y guarda el PDF; solo muestra una alerta si falla. */
    private static void guardarPdf(Reserva reserva, Image qr, Stage ventana) {
        FileChooser selector = new FileChooser();
        selector.setTitle("Guardar comprobante de reserva");
        selector.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Documento PDF (*.pdf)", "*.pdf"));
        selector.setInitialFileName("CINEMA_Reserva_" + reserva.getCodigoReserva() + ".pdf");

        File destino = selector.showSaveDialog(ventana);
        if (destino == null) {
            return;
        }

        try {
            ExportadorComprobantePDF.exportar(crearTicket(reserva, qr, true), destino);
            // No mostrar Alert de éxito: el archivo ya está guardado.
        } catch (IOException | RuntimeException e) {
            mostrarError("No se pudo guardar el PDF", e);
        }
    }

    private static void mostrarError(String titulo, Exception e) {
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setTitle("CINEMA");
        error.setHeaderText(titulo);
        error.setContentText(e.getMessage() == null ? "Error inesperado" : e.getMessage());
        error.showAndWait();
    }

    private static String darFormatoDinero(double importe) {
        return String.format(Locale.forLanguageTag("es-AR"), "$%,.2f", importe);
    }
}
