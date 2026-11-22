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

public class DialogoCinema {

    public static void mostrarError(Window ventanaPrincipal,
                                    String titulo,
                                    String mensaje) {

        Stage ventana = new Stage();
        ventana.initStyle(StageStyle.TRANSPARENT);
        ventana.initModality(Modality.WINDOW_MODAL);
        ventana.initOwner(ventanaPrincipal);
        ventana.setResizable(false);

        // Ícono de advertencia
        Label icono = new Label("!");
        icono.setStyle(
                "-fx-text-fill: #F87171;"
                + "-fx-font-size: 22px;"
                + "-fx-font-weight: bold;"
        );

        VBox contenedorIcono = new VBox(icono);
        contenedorIcono.setAlignment(Pos.CENTER);
        contenedorIcono.setMinSize(45, 45);
        contenedorIcono.setMaxSize(45, 45);
        contenedorIcono.setStyle(
                "-fx-background-color: #42252D;"
                + "-fx-background-radius: 12;"
        );

        // Título
        Label etiquetaTitulo = new Label(titulo);
        etiquetaTitulo.setWrapText(true);
        etiquetaTitulo.setStyle(
                "-fx-text-fill: white;"
                + "-fx-font-size: 16px;"
                + "-fx-font-weight: bold;"
        );

        // Subtítulo
        Label subtitulo = new Label("La operación fue cancelada.");
        subtitulo.setStyle(
                "-fx-text-fill: #94A3B8;"
                + "-fx-font-size: 12px;"
        );

        VBox textos = new VBox(5, etiquetaTitulo, subtitulo);
        HBox.setHgrow(textos, Priority.ALWAYS);

        HBox encabezado = new HBox(15, contenedorIcono, textos);
        encabezado.setAlignment(Pos.CENTER_LEFT);

        // Mensaje
        Label etiquetaMensaje = new Label(mensaje);
        etiquetaMensaje.setWrapText(true);
        etiquetaMensaje.setMaxWidth(Double.MAX_VALUE);
        etiquetaMensaje.setStyle(
                "-fx-text-fill: #CBD5E1;"
                + "-fx-font-size: 13px;"
        );

        // Botón
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

        // Contenedor principal
        VBox contenido = new VBox(20, encabezado, etiquetaMensaje, botones);
        contenido.setPadding(new Insets(24));
        contenido.setStyle(
                "-fx-background-color: #101827;"
                + "-fx-background-radius: 12;"
                + "-fx-border-color: #334155;"
                + "-fx-border-radius: 12;"
                + "-fx-border-width: 1;"
        );

        Scene escena = new Scene(contenido, 420, 215);
        escena.setFill(javafx.scene.paint.Color.TRANSPARENT);

        ventana.setScene(escena);
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