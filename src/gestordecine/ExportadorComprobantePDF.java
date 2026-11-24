package gestordecine;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 * Exporta el comprobante completo a una sola página A4 horizontal.
 * El contenido se escala proporcionalmente para que nunca se corte el QR.
 * No modifica los datos serializados de CINEMA.
 */
public final class ExportadorComprobantePDF {

    private static final float MARGEN = 30f;

    private ExportadorComprobantePDF() {
    }

    /**
     * Genera una captura del comprobante y la ajusta íntegra a una página.
     * Debe invocarse desde el hilo de JavaFX.
     *
     * @param contenido tarjeta independiente para impresión
     * @param destino archivo elegido por el usuario
     * @throws IOException si falla el guardado
     */
    public static void exportar(Region contenido, File destino) throws IOException {
        if (contenido == null || destino == null) {
            throw new IllegalArgumentException("El contenido y el destino son obligatorios");
        }

        // Se captura el nodo completo, no el ScrollPane visible en pantalla.
        new Scene(contenido);
        contenido.applyCss();
        contenido.autosize();
        contenido.layout();

        SnapshotParameters parametros = new SnapshotParameters();
        parametros.setFill(Color.WHITE);
        WritableImage captura = contenido.snapshot(parametros, null);
        BufferedImage imagen = aBufferedImage(captura);

        if (imagen.getWidth() <= 0 || imagen.getHeight() <= 0) {
            throw new IOException("No se pudo capturar el comprobante");
        }

        // A4 apaisado: ajusta ancho Y alto para evitar saltos entre páginas.
        PDRectangle tamanoPagina = new PDRectangle(
                PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
        float anchoUtil = tamanoPagina.getWidth() - 2 * MARGEN;
        float altoUtil = tamanoPagina.getHeight() - 2 * MARGEN;
        float escala = Math.min(1f, Math.min(
                anchoUtil / imagen.getWidth(), altoUtil / imagen.getHeight()));
        float anchoFinal = imagen.getWidth() * escala;
        float altoFinal = imagen.getHeight() * escala;
        float x = (tamanoPagina.getWidth() - anchoFinal) / 2;
        float y = (tamanoPagina.getHeight() - altoFinal) / 2;

        try (PDDocument documento = new PDDocument()) {
            PDPage pagina = new PDPage(tamanoPagina);
            documento.addPage(pagina);
            PDImageXObject imagenPdf = LosslessFactory.createFromImage(documento, imagen);

            try (PDPageContentStream flujo = new PDPageContentStream(documento, pagina)) {
                flujo.drawImage(imagenPdf, x, y, anchoFinal, altoFinal);
            }

            documento.getDocumentInformation().setTitle("Comprobante de reserva CINEMA");
            documento.getDocumentInformation().setCreator("CINEMA");
            documento.save(destino);
        }
    }

    private static BufferedImage aBufferedImage(WritableImage imagen) {
        int ancho = (int) imagen.getWidth();
        int alto = (int) imagen.getHeight();
        BufferedImage resultado = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        PixelReader lector = imagen.getPixelReader();

        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                resultado.setRGB(x, y, lector.getArgb(x, y) & 0x00FFFFFF);
            }
        }
        return resultado;
    }
}
