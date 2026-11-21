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

    public void actualizarCine(Cine cineActualizado) {
        this.cine = cineActualizado;
    }

    public void recuperarCineGuardado() throws IOException, ClassNotFoundException {

        this.cine = persistencia.cargarOCrear();
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

    @FXML
    public void mostrarSalas() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("Salas.fxml")
            );

            Parent pantalla = loader.load();
            SalasController controlador = loader.getController();
            controlador.configurar(cine, persistencia);
            controlador.setPrincipalController(this);

            contenedorPrincipal.setCenter(pantalla);

        } catch (IOException e) {
            System.out.println("Error al cargar la pantalla de salas:");
            e.printStackTrace();
        }
    }
    
    /**
     * Que hace: abre la pantalla de funciones en el centro de la ventana.
     */
    @FXML
    public void mostrarFunciones() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("Funciones.fxml")
            );

            Parent pantalla = loader.load();

            FuncionesController controlador = loader.getController();
            controlador.configurar(cine, persistencia);
            controlador.setPrincipalController(this);

            contenedorPrincipal.setCenter(pantalla);

        } catch (IOException e) {
            System.out.println("Error al cargar la pantalla de funciones:");
            e.printStackTrace();
        }
    }    
    
    

    public void mostrarFormularioSala() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("FormularioSala.fxml")
            );

            Parent pantalla = loader.load();

            FormularioSalaController controlador = loader.getController();

            controlador.configurar(cine, persistencia, this);

            contenedorPrincipal.setCenter(pantalla);

        } catch (IOException e) {
            System.out.println("Error al cargar el formulario de sala:");
            e.printStackTrace();
        }
    }

    public void mostrarFormularioEditarSala(Sala sala) {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("FormularioSala.fxml")
            );

            Parent pantalla = loader.load();

            FormularioSalaController controlador = loader.getController();

            controlador.configurar(cine, persistencia, this);
            controlador.configurarEdicion(sala);

            contenedorPrincipal.setCenter(pantalla);

        } catch (IOException e) {
            System.out.println("Error al cargar el formulario de edición de sala:");
            e.printStackTrace();
        }
    }

    @FXML
    public void mostrarCartelera() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("Cartelera.fxml")
            );

            Parent pantalla = loader.load();

            CarteleraController controlador = loader.getController();
            controlador.setPrincipalController(this);
            controlador.configurar(cine);
            contenedorPrincipal.setCenter(pantalla);

        } catch (IOException e) {
            System.out.println("Error al cargar la cartelera:");
            e.printStackTrace();
        }
    }
    
    public void mostrarDetallePelicula(Pelicula pelicula) {

    try {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("DetallePelicula.fxml")
        );

        Parent pantalla = loader.load();

        DetallePeliculaController controlador = loader.getController();

        controlador.setPrincipalController(this);
        controlador.configurar(pelicula);

        contenedorPrincipal.setCenter(pantalla);

    } catch (IOException e) {
        System.out.println("Error al cargar el detalle de película:");
        e.printStackTrace();
    }
}
}
