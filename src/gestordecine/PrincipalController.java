package gestordecine;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

public class PrincipalController {

    private Cine cine;
    private PersistenciaCine persistencia;

    public void setCine(Cine cine) {
        this.cine = cine;
        this.persistencia = persistencia;
    }

    @FXML
    private BorderPane contenedorPrincipal;

    @FXML
    public void mostrarPeliculas() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("Peliculas.fxml")
            );

            Parent pantalla = loader.load();

            PeliculasController controlador = loader.getController();
            controlador.configurar(cine, persistencia);
            controlador.setPrincipalController(this);

            contenedorPrincipal.setCenter(pantalla);

        } catch (IOException e) {
            System.out.println("Error al cargar la pantalla de peliculas: "
                    + e.getMessage());
        }
    }

    public void configurar(Cine cine, PersistenciaCine persistencia) {
        this.cine = cine;
        this.persistencia = persistencia;
    }

    public void mostrarFormularioPelicula() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("FormularioPelicula.fxml")
            );

            Parent pantalla = loader.load();

            FormularioPeliculaController controlador = loader.getController();

            controlador.configurar(cine, persistencia, this);

            contenedorPrincipal.setCenter(pantalla);

        } catch (IOException e) {
            System.out.println("Error al cargar el formulario: "
                    + e.getMessage());
        }
    }

    public void mostrarFormularioEditarPelicula(Pelicula pelicula) {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("FormularioPelicula.fxml")
            );

            Parent pantalla = loader.load();

            FormularioPeliculaController controlador = loader.getController();

            controlador.configurar(cine, persistencia, this);
            controlador.configurarEdicion(pelicula);

            contenedorPrincipal.setCenter(pantalla);

        } catch (IOException e) {
            System.out.println("Error al cargar el formulario de edición: "
                    + e.getMessage());
        }
    }
}
