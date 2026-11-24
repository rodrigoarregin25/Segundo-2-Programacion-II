package gestordecine;

import gestordecine.controladores.PrincipalController;
import java.io.File;
import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * Clase principal de la aplicación. Carga los datos del cine desde el archivo
 * y abre la ventana con la cartelera.
 */

public class GestorDeCine extends Application {
    
    /**
     * Arranca la aplicación. Lee el cine desde cine_app.dat (o lo crea si no
     * existe), carga la pantalla principal y muestra la cartelera. Si el
     * archivo no se puede leer, avisa y cierra la aplicación sin tocarlo.
     *
     * @param stage Ventana principal de la aplicación.
     * @throws Exception Si falla la carga de la pantalla principal.
     */

    @Override
    public void start(Stage stage) throws Exception {

        PersistenciaCine persistencia = new PersistenciaCine("cine_app.dat");

        Cine cine;

        try {
            cine = persistencia.cargarOCrear();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            // no se sigue: asi no se pisa el archivo con datos vacios
            mostrarErrorDeArchivo(e);
            Platform.exit();
            return;
        }

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/gestordecine/vistas/Principal.fxml")
        );

        Parent raiz = loader.load();

        PrincipalController controlador = loader.getController();
        controlador.configurar(cine, persistencia);
        controlador.mostrarCartelera();

        Scene escena = new Scene(raiz, 1200, 750);

        stage.setTitle("CINEMA | Sistema de Reservas");
        stage.setMaximized(true);
        stage.setScene(escena);
        stage.show();
    }

    /**
     * Avisa en una ventana que no se pudo leer el archivo de datos. Muestra la
     * ruta completa del archivo y el detalle del error.
     *
     * @param error Excepción que se produjo al leer el archivo.
     */
    private void mostrarErrorDeArchivo(Exception error) {
        File archivo = new File("cine_app.dat");

        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error al abrir los datos");
        alerta.setHeaderText("No se pudo leer el archivo de datos del cine");
        alerta.setContentText("Archivo: " + archivo.getAbsolutePath()
                + "\nEl archivo no se modificó. Revisalo o restaurá una copia "
                + "y volvé a abrir la aplicación."
                + "\n\nDetalle: " + error.getMessage());
        alerta.showAndWait();
    }
    
    /**
     * Punto de entrada del programa. Lanza la aplicación JavaFX.
     *
     * @param args Argumentos de la línea de comandos. Se le pasan a launch,
     * pero la aplicación no necesita ninguno.
     */

    public static void main(String[] args) {
        launch(args);
    }
}