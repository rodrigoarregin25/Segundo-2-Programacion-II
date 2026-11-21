package gestordecine;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GestorDeCine extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        PersistenciaCine persistencia = new PersistenciaCine("cine_app.dat");

        Cine cine = persistencia.cargarOCrear();

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("Principal.fxml")
        );

        Parent raiz = loader.load();

        PrincipalController controlador = loader.getController();
        controlador.configurar(cine, persistencia);
        controlador.mostrarCartelera();

        Scene escena = new Scene(raiz, 1200, 750);

        stage.setTitle("CINEMA | Sistema de Reservas");
        stage.setScene(escena);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
