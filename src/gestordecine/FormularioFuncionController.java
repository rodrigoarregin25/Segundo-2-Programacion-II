package gestordecine;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class FormularioFuncionController {

    private Cine cine;
    private PersistenciaCine persistencia;
    private PrincipalController principalController;
    private Funcion funcionEnEdicion;

    // La posicion elegida en el desplegable coincide con la posicion en la lista
    private ArrayList<Pelicula> peliculasActivas;
    private ArrayList<Sala> salasActivas;

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
    @FXML
    private Label tituloFormulario;
    @FXML
    private Button botonGuardar;

    /**
     * Que hace: carga los desplegables fijos (formato, idioma, hora y minutos).
     * JavaFX lo llama solo al abrir la pantalla.
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
     * Que hace: recibe el cine, la persistencia y el controlador principal,
     * y llena los desplegables de pelicula y sala solo con las activas.
     */
    public void configurar(Cine cine, PersistenciaCine persistencia,
            PrincipalController principalController) {

        this.cine = cine;
        this.persistencia = persistencia;
        this.principalController = principalController;

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
     * Que hace: prepara el formulario para editar el precio de una funcion existente.
     * Muestra sus datos y deja habilitado solo el precio.
     * Parametros: funcion (la funcion a editar).
     */
    public void configurarEdicion(Funcion funcion) {
        this.funcionEnEdicion = funcion;

        // Se muestran los datos de la funcion (aunque su pelicula o sala este inactiva)
        comboPelicula.getItems().clear();
        comboPelicula.getItems().add(funcion.getPelicula().getTituloPelicula());
        comboPelicula.setValue(funcion.getPelicula().getTituloPelicula());

        comboSala.getItems().clear();
        comboSala.getItems().add(funcion.getSala().getNombreSala());
        comboSala.setValue(funcion.getSala().getNombreSala());

        LocalDateTime fechaHora = funcion.getFechaHora();
        selectorFecha.setValue(fechaHora.toLocalDate());
        comboHora.setValue(String.format("%02d", fechaHora.getHour()));
        comboMinutos.setValue(String.format("%02d", fechaHora.getMinute()));
        comboFormato.setValue(funcion.getFormatoPelicula());
        comboIdioma.setValue(funcion.getIdiomaPelicula());
        campoPrecio.setText(String.valueOf(funcion.getPrecio()));

        // Solo se puede cambiar el precio
        comboPelicula.setDisable(true);
        comboSala.setDisable(true);
        selectorFecha.setDisable(true);
        comboHora.setDisable(true);
        comboMinutos.setDisable(true);
        comboFormato.setDisable(true);
        comboIdioma.setDisable(true);

        tituloFormulario.setText("Editar precio de la función");
        botonGuardar.setText("Guardar precio");
    }

    @FXML
    private void cancelar() {
        principalController.mostrarFunciones();
    }

    /**
     * Que hace: guarda la funcion nueva o el precio nuevo, segun el modo.
     * Las reglas (horarios superpuestos, ID repetido, etc.) las valida Cine y Funcion;
     * aca solo se muestran los errores en pantalla.
     */
    @FXML
    private void guardarFuncion() {
        ocultarError();

        try {
            double precio;
            try {
                precio = Double.parseDouble(campoPrecio.getText().trim().replace(',', '.'));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("El precio debe ser un numero");
            }

            // Se trabaja sobre una copia del cine
            Cine cineTrabajo = cine.copiar();

            if (funcionEnEdicion != null) {

                // EDITAR: solo cambia el precio
                cineTrabajo.editarPrecioFuncion(funcionEnEdicion.getIdFuncion(), precio);

            } else {

                // NUEVA FUNCION
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

                int hora = Integer.parseInt(comboHora.getValue());
                int minutos = Integer.parseInt(comboMinutos.getValue());
                LocalDateTime fechaHora = fecha.atTime(hora, minutos);

                // La pelicula y la sala se buscan DENTRO de la copia,
                // porque Cine las compara por identidad.
                Pelicula pelicula = cineTrabajo.buscarPeliculaPorId(
                        peliculasActivas.get(posPelicula).getIdPelicula());
                Sala sala = cineTrabajo.buscarSalaPorId(
                        salasActivas.get(posSala).getIdSala());

                Funcion funcion = new Funcion(obtenerSiguienteId(cineTrabajo), sala, pelicula,
                        precio, fechaHora, comboFormato.getValue(), comboIdioma.getValue());

                cineTrabajo.programarFuncion(funcion);
            }

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