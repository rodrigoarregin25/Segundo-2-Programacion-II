package gestordecine;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import javax.imageio.ImageIO;
import javafx.scene.image.Image;

/**
 * Construye un QR autónomo con los datos del comprobante de una reserva.
 * No utiliza Internet ni modifica los objetos del modelo.
 *
 * El contenido del QR es texto legible por cualquier lector: al escanearlo,
 * se mostrarán todos los datos, incluido el DNI. No constituye una validación
 * de autenticidad ni una confirmación de que la entrada no fue utilizada.
 */
public final class GeneradorQRComprobante {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private GeneradorQRComprobante() {
    }

    /**
     * Produce una imagen QR a partir de todos los datos visibles del comprobante.
     *
     * @param reserva Reserva de la que se obtienen los datos.
     * @return Imagen que puede mostrarse directamente en JavaFX.
     * @throws IOException Si no se puede codificar la imagen QR.
     */
    public static Image crearImagen(Reserva reserva) throws IOException {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva no puede ser nula");
        }

        Map<EncodeHintType, Object> opciones = new EnumMap<>(EncodeHintType.class);
        opciones.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        opciones.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        opciones.put(EncodeHintType.MARGIN, 3);

        try {
            BitMatrix matriz = new QRCodeWriter().encode(
                    obtenerTexto(reserva), BarcodeFormat.QR_CODE, 440, 440, opciones);
            BufferedImage imagen = new BufferedImage(
                    matriz.getWidth(), matriz.getHeight(), BufferedImage.TYPE_INT_RGB);

            for (int y = 0; y < matriz.getHeight(); y++) {
                for (int x = 0; x < matriz.getWidth(); x++) {
                    imagen.setRGB(x, y, matriz.get(x, y) ? 0xFF111827 : 0xFFFFFFFF);
                }
            }

            try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
                if (!ImageIO.write(imagen, "png", salida)) {
                    throw new IOException("No se pudo codificar la imagen PNG del QR");
                }
                return new Image(new ByteArrayInputStream(salida.toByteArray()));
            }
        } catch (WriterException e) {
            throw new IOException("No se pudo generar el código QR del comprobante", e);
        }
    }

    /**
     * Obtiene el texto que se codifica dentro del QR.
     * Incluye cada butaca, tipo de entrada, precio y el total confirmado.
     */
    public static String obtenerTexto(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva no puede ser nula");
        }

        Funcion funcion = reserva.getFuncion();
        Cliente cliente = reserva.getCliente();
        StringBuilder texto = new StringBuilder();
        texto.append("CINEMA - COMPROBANTE DE RESERVA\n")
                .append("Codigo: ").append(reserva.getCodigoReserva()).append('\n')
                .append("Estado: ").append(reserva.isConfirmada() ? "CONFIRMADA" : "CANCELADA").append('\n')
                .append("Pelicula: ").append(limpiar(funcion.getPelicula().getTituloPelicula())).append('\n')
                .append("Fecha: ").append(funcion.getFechaHora().format(FORMATO_FECHA)).append('\n')
                .append("Sala: ").append(limpiar(funcion.getSala().getNombreSala())).append('\n')
                .append("Cliente: ").append(limpiar(cliente.getNombre())).append(' ')
                .append(limpiar(cliente.getApellido())).append('\n')
                .append("DNI: ").append(cliente.getDni()).append('\n')
                .append("Entradas:\n");

        for (Entrada entrada : reserva.getEntradas()) {
            texto.append("- ")
                    .append(entrada.getButaca().getIdentificador()).append(" - ")
                    .append(limpiar(entrada.getTipo())).append(" - ")
                    .append(darFormatoDinero(entrada.calcularPrecioFinal())).append('\n');
        }

        texto.append("TOTAL: ").append(darFormatoDinero(reserva.getImporteTotal()));
        return texto.toString();
    }

    private static String limpiar(String valor) {
        return valor == null ? "" : valor.replace('\r', ' ').replace('\n', ' ');
    }

    private static String darFormatoDinero(double importe) {
        return String.format(Locale.forLanguageTag("es-AR"), "$%,.2f", importe);
    }
}
