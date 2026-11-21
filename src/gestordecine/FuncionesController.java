package gestordecine;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class FuncionesController {

    private Cine cine;
    private PersistenciaCine persistencia;
    private PrincipalController principalController;

    // Copias de las listas que se muestran en los desplegables.
    // La posicion elegida en el desplegable coincide con la posicion en la lista.
    private ArrayList<Pelicula> peliculasActivas;
    private ArrayList<Sala> salasActivas;

    @FXML
    private ListView<String> listaFunciones;
    @FXML
    private ComboBox<String> comboPelicula;
    @FXML
    private ComboBox<String> comboSala;
    @FXML
    private DatePicker selectorFecha;
    @FXML
    private ComboBox<String> comboHora;
    @FXML
    private ComboBox<String> comboMinutos;
    @FXML
    private TextField campoPrecio;
    @FXML
    private ComboBox<String> comboFormato;
    @FXML
    private ComboBox<String> comboIdioma;
    @FXML
    private Label mensajeError;

    /**
     * Que hace: carga los desplegables que no dependen del cine
     * (formato, idioma, hora y minutos). JavaFX lo llama solo al abrir la pantalla.
     */
    @FXML
    private void initialize() {
        comboFormato.getItems().addAll("2D", "3D", "D-BOX", "4D");
        comboIdioma.getItems().addAll("INGLES", "CASTELLANO", "INGLES(SUBTITULADA)");

        for (int hora = 0; hora < 24; hora++) {
            comboHora.getItems().add(String.format("%02d", hora));
        }
        comboMinutos.getItems().addAll("00", "15", "30", "45");
    }

    /**
     * Que hace: recibe el cine y la persistencia, y muestra los datos.
     * Parametros: cine (el cine actual), persistencia (para guardar los cambios).
     */
    public void configurar(Cine cine, PersistenciaCine persistencia) {
        this.cine = cine;
        this.persistencia = persistencia;

        cargarDesplegables();
        cargarLista();
    }

    public void setPrincipalController(PrincipalController principalController) {
        this.principalController = principalController;
    }

    /**
     * Que hace: llena los desplegables de pelicula y sala solo con las activas.
     */
    private void cargarDesplegables() {
        peliculasActivas = new ArrayList<>();
        for (Pelicula pelicula : cine.getPeliculas()) {
            if (pelicula.isActiva()) {
                peliculasActivas.add(pelicula);
                comboPelicula.getItems().add(pelicula.getTituloPelicula());
            }
        }

        salasActivas = new ArrayList<>();
        for (Sala sala : cine.getSalas()) {
            if (sala.isActiva()) {
                salasActivas.add(sala);
                comboSala.getItems().add(sala.getNombreSala());
            }
        }
    }

    /**
     * Que hace: muestra en la lista todas las funciones programadas.
     */
    private void cargarLista() {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        ArrayList<String> textos = new ArrayList<>();

        for (Funcion funcion : cine.getFunciones()) {
            textos.add(funcion.getPelicula().getTituloPelicula()
                    + " | " + funcion.getSala().getNombreSala()
                    + " | " + funcion.getFechaHora().format(formato)
                    + " | " + funcion.getFormatoPelicula()
                    + " | " + funcion.getIdiomaPelicula()
                    + " | $" + funcion.getPrecio());
        }

        listaFunciones.setItems(FXCollections.observableArrayList(textos));
    }

    /**
     * Que hace: lee el formulario, crea la funcion y la programa en el cine.
     * Las reglas (horarios superpuestos, ID repetido, etc.) las valida Cine y Funcion;
     * aca solo se muestran los errores en pantalla.
     */
    @FXML
    private void programarFuncion() {
        ocultarError();

        try {
            int posPelicula = comboPelicula.getSelectionModel().getSelectedIndex();
            int posSala = comboSala.getSelectionModel().getSelectedIndex();
            LocalDate fecha = selectorFecha.getValue();

            if (posPelicula < 0) {
                throw new IllegalArgumentException("Seleccione una pelicula");
            }
            if (posSala < 0) {
                throw new IllegalArgumentException("Seleccione una sala");
            }
            if (fecha == null) {
                throw new IllegalArgumentException("Seleccione una fecha");
            }
            if (comboHora.getValue() == null || comboMinutos.getValue() == null) {
                throw new IllegalArgumentException("Seleccione la hora de inicio");
            }
            if (comboFormato.getValue() == null) {
                throw new IllegalArgumentException("Seleccione un formato");
            }
            if (comboIdioma.getValue() == null) {
                throw new IllegalArgumentException("Seleccione un idioma");
            }

            double precio;
            try {
                precio = Double.parseDouble(campoPrecio.getText().trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("El precio debe ser un numero");
            }

            int hora = Integer.parseInt(comboHora.getValue());
            int minutos = Integer.parseInt(comboMinutos.getValue());
            LocalDateTime fechaHora = fecha.atTime(hora, minutos);

            // Se trabaja sobre una copia, igual que en Salas. La pelicula y la sala
            // se buscan DENTRO de la copia, porque Cine las compara por identidad.
            Cine cineTrabajo = cine.copiar();
            Pelicula pelicula = cineTrabajo.buscarPeliculaPorId(
                    peliculasActivas.get(posPelicula).getIdPelicula());
            Sala sala = cineTrabajo.buscarSalaPorId(
                    salasActivas.get(posSala).getIdSala());

            Funcion funcion = new Funcion(obtenerSiguienteId(cineTrabajo), sala, pelicula,
                    precio, fechaHora, comboFormato.getValue(), comboIdioma.getValue());

            cineTrabajo.programarFuncion(funcion);

            persistencia.guardar(cineTrabajo);
            principalController.actualizarCine(cineTrabajo);
            principalController.mostrarFunciones();

        } catch (IOException | ClassNotFoundException e) {
            // Error de datos (archivo / copia), distinto de un error de reglas
            mostrarError("Error al guardar los datos: " + e.getMessage());
        } catch (Exception e) {
            // Error de reglas de negocio: horario ocupado, fecha pasada, etc.
            mostrarError(e.getMessage());
        }
    }

    /**
     * Que hace: calcula el ID de la nueva funcion (el mayor existente + 1).
     * Retorna: el ID siguiente.
     */
    private int obtenerSiguienteId(Cine unCine) {
        int mayorId = 0;
        for (Funcion funcion : unCine.getFunciones()) {
            if (funcion.getIdFuncion() > mayorId) {
                mayorId = funcion.getIdFuncion();
            }
        }
        return mayorId + 1;
    }

    private void mostrarError(String mensaje) {
        mensajeError.setText(mensaje);
        mensajeError.setVisible(true);
        mensajeError.setManaged(true);
    }

    private void ocultarError() {
        mensajeError.setVisible(false);
        mensajeError.setManaged(false);
    }
}