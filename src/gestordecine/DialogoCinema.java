
package gestordecine;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * Clase encargada de mostrar diálogos personalizados en CINEMA.
 *
 * Permite informar errores y operaciones exitosas mediante
 * ventanas modales con el diseño visual del sistema.
 *
 * @author Rodrigo Arregin
 */
public class DialogoCinema {

    /**
     * Muestra un diálogo de error con un ícono rojo.
     *
     * @param ventanaPrincipal Ventana sobre la que se muestra el diálogo.
     * @param titulo Título del mensaje de error.
     * @param mensaje Descripción del error ocurrido.
     */
    public static void mostrarError(Window ventanaPrincipal,
                                    String titulo,
                                    String mensaje) {

        mostrarDialogo(
                ventanaPrincipal,
                titulo,
                mensaje,
                "!",
                "#F87171",
                "#42252D",
                "La operación fue cancelada."
        );
    }

    /**
     * Muestra un diálogo de confirmación con un ícono verde.
     *
     * @param ventanaPrincipal Ventana sobre la que se muestra el diálogo.
     * @param titulo Título de la confirmación.
     * @param mensaje Descripción de la operación realizada.
     */
    public static void mostrarExito(Window ventanaPrincipal,
                                   String titulo,
                                   String mensaje) {

        mostrarDialogo(
                ventanaPrincipal,
                titulo,
                mensaje,
                "✓",
                "#4ADE80",
                "#153F35",
                "La operación se completó correctamente."
        );
    }

    /**
     * Construye y muestra un diálogo modal personalizado.
     *
     * Recibe los textos y colores necesarios para representar
     * distintos tipos de mensajes, reutilizando el mismo diseño.
     *
     * @param ventanaPrincipal Ventana propietaria del diálogo.
     * @param titulo Título que se mostrará en el encabezado.
     * @param mensaje Mensaje principal del diálogo.
     * @param simbolo Símbolo utilizado como ícono.
     * @param colorSimbolo Color del símbolo.
     * @param colorFondoIcono Color de fondo del contenedor del ícono.
     * @param textoSubtitulo Texto mostrado debajo del título.
     */
    private static void mostrarDialogo(Window ventanaPrincipal,
                                       String titulo,
                                       String mensaje,
                                       String simbolo,
                                       String colorSimbolo,
                                       String colorFondoIcono,
                                       String textoSubtitulo) {

        // Crear la ventana modal.
        Stage ventana = new Stage();
        ventana.initStyle(StageStyle.TRANSPARENT);
        ventana.initModality(Modality.WINDOW_MODAL);
        ventana.initOwner(ventanaPrincipal);
        ventana.setResizable(false);

        // Ícono del diálogo.
        Label icono = new Label(simbolo);
        icono.setStyle(
                "-fx-text-fill: " + colorSimbolo + ";"
                + "-fx-font-size: 22px;"
                + "-fx-font-weight: bold;"
        );

        VBox contenedorIcono = new VBox(icono);
        contenedorIcono.setAlignment(Pos.CENTER);
        contenedorIcono.setMinSize(45, 45);
        contenedorIcono.setMaxSize(45, 45);
        contenedorIcono.setStyle(
                "-fx-background-color: " + colorFondoIcono + ";"
                + "-fx-background-radius: 12;"
        );

        // Título.
        Label etiquetaTitulo = new Label(titulo);
        etiquetaTitulo.setWrapText(true);
        etiquetaTitulo.setStyle(
                "-fx-text-fill: white;"
                + "-fx-font-size: 16px;"
                + "-fx-font-weight: bold;"
        );

        // Subtítulo.
        Label subtitulo = new Label(textoSubtitulo);
        subtitulo.setStyle(
                "-fx-text-fill: #94A3B8;"
                + "-fx-font-size: 12px;"
        );

        VBox textos = new VBox(5, etiquetaTitulo, subtitulo);
        HBox.setHgrow(textos, Priority.ALWAYS);

        HBox encabezado = new HBox(15, contenedorIcono, textos);
        encabezado.setAlignment(Pos.CENTER_LEFT);

        // Mensaje principal.
        Label etiquetaMensaje = new Label(mensaje);
        etiquetaMensaje.setWrapText(true);
        etiquetaMensaje.setMaxWidth(Double.MAX_VALUE);
        etiquetaMensaje.setStyle(
                "-fx-text-fill: #CBD5E1;"
                + "-fx-font-size: 13px;"
        );

        // Botón de confirmación.
        Button botonAceptar = new Button("Entendido");
        botonAceptar.setPrefWidth(115);
        botonAceptar.setStyle(
                "-fx-background-color: #2563EB;"
                + "-fx-text-fill: white;"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 7;"
                + "-fx-padding: 10 18;"
                + "-fx-cursor: hand;"
        );

        botonAceptar.setOnAction(evento -> ventana.close());

        HBox botones = new HBox(botonAceptar);
        botones.setAlignment(Pos.CENTER_RIGHT);

        // Contenedor principal.
        VBox contenido = new VBox(20, encabezado, etiquetaMensaje, botones);
        contenido.setPadding(new Insets(24));
        contenido.setStyle(
                "-fx-background-color: #101827;"
                + "-fx-background-radius: 12;"
                + "-fx-border-color: #334155;"
                + "-fx-border-radius: 12;"
                + "-fx-border-width: 1;"
        );

        // Crear la escena con fondo transparente.
        Scene escena = new Scene(contenido, 420, 215);
        escena.setFill(javafx.scene.paint.Color.TRANSPARENT);

        ventana.setScene(escena);

        // Centrar el diálogo respecto a la ventana principal.
        ventana.setOnShown(evento -> {
            ventana.setX(
                    ventanaPrincipal.getX()
                    + (ventanaPrincipal.getWidth() - ventana.getWidth()) / 2
            );
            ventana.setY(
                    ventanaPrincipal.getY()
                    + (ventanaPrincipal.getHeight() - ventana.getHeight()) / 2
            );
        });

        ventana.showAndWait();
    }
}
