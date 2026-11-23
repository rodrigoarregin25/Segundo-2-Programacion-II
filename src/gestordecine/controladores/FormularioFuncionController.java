package gestordecine.controladores;

import gestordecine.Cine;
import gestordecine.Funcion;
import gestordecine.Pelicula;
import gestordecine.PersistenciaCine;
import gestordecine.Sala;
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

/**
 * Controlador del formulario de registro y edición de funciones.
 *
 * Permite programar nuevas funciones seleccionando una película, una sala, una
 * fecha, un horario, un formato, un idioma y un precio.
 *
 * También permite modificar los datos de una función existente, respetando las
 * restricciones relacionadas con su estado y las reservas asociadas.
 *
 * Las validaciones de las reglas de negocio se realizan principalmente mediante
 * las clases Cine y Funcion.
 *
 * Trabaja sobre una copia del cine para evitar modificar el estado original
 * antes de persistir los cambios.
 *
 * Se comunica con PrincipalController para actualizar el cine y gestionar la
 * navegación entre pantallas.
 */
public class FormularioFuncionController {

    // Referencias al sistema y al controlador principal.
    private Cine cine;
    private PersistenciaCine persistencia;
    private PrincipalController principalController;

    // Función seleccionada cuando el formulario está en modo edición.
    private Funcion funcionEnEdicion;

    // Las posiciones de estas listas coinciden con las de los ComboBox.
    private ArrayList<Pelicula> peliculasActivas;
    private ArrayList<Sala> salasActivas;

    // Componentes de la interfaz.
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
     * Inicializa los desplegables con los valores fijos del formulario.
     *
     * Carga los formatos e idiomas disponibles, las horas del día y los minutos
     * permitidos en intervalos de quince minutos.
     *
     * JavaFX ejecuta este método automáticamente después de cargar los
     * componentes definidos en el archivo FXML.
     */
    @FXML
    private void initialize() {

        comboFormato.getItems().addAll(
                "2D", "3D", "D-BOX", "4D"
        );

        comboIdioma.getItems().addAll(
                "INGLES", "CASTELLANO", "INGLES(SUBTITULADA)"
        );

        for (int hora = 0; hora < 24; hora++) {
            comboHora.getItems().add(
                    String.format("%02d", hora)
            );
        }

        comboMinutos.getItems().addAll(
                "00", "15", "30", "45"
        );
    }

    /**
     * Configura el formulario con las referencias del sistema.
     *
     * Recibe el cine actual, la persistencia y el controlador principal.
     * Además, obtiene las películas y salas activas para completar los
     * desplegables correspondientes.
     *
     * Las posiciones de los elementos en los ComboBox coinciden con las
     * posiciones de las listas internas.
     *
     * @param cine Cine que contiene las películas, salas y funciones.
     * @param persistencia Objeto encargado de guardar el estado del cine.
     * @param principalController Controlador principal de la aplicación.
     */
    public void configurar(
            Cine cine,
            PersistenciaCine persistencia,
            PrincipalController principalController) {

        this.cine = cine;
        this.persistencia = persistencia;
        this.principalController = principalController;

        peliculasActivas = new ArrayList<>();

        for (Pelicula pelicula : cine.getPeliculas()) {

            if (pelicula.isActiva()) {

                peliculasActivas.add(pelicula);

                comboPelicula.getItems().add(
                        pelicula.getTituloPelicula()
                );
            }
        }

        salasActivas = new ArrayList<>();

        for (Sala sala : cine.getSalas()) {

            if (sala.isActiva()) {

                salasActivas.add(sala);

                comboSala.getItems().add(
                        sala.getNombreSala()
                );
            }
        }
    }

    /**
     * Configura el formulario para editar una función existente.
     *
     * Carga los datos actuales de la función en los campos correspondientes y
     * selecciona su película y sala.
     *
     * Si la película o sala de la función están inactivas, se incorporan a los
     * desplegables para poder mostrar sus valores actuales.
     *
     * Cuando la función tiene reservas asociadas o se encuentra desactivada,
     * bloquea la edición de su programación y presenta el formulario como
     * edición de precio.
     *
     * Si no existen estas restricciones, permite modificar todos los datos de
     * la función.
     *
     * @param funcion Función que se desea editar.
     */
    public void configurarEdicion(Funcion funcion) {

        this.funcionEnEdicion = funcion;

        // Incorporar la película actual si no está en la lista.
        if (!peliculasActivas.contains(funcion.getPelicula())) {

            peliculasActivas.add(funcion.getPelicula());

            comboPelicula.getItems().add(
                    funcion.getPelicula().getTituloPelicula()
            );
        }

        // Incorporar la sala actual si no está en la lista.
        if (!salasActivas.contains(funcion.getSala())) {

            salasActivas.add(funcion.getSala());

            comboSala.getItems().add(
                    funcion.getSala().getNombreSala()
            );
        }

        // Seleccionar la película y sala de la función.
        comboPelicula.getSelectionModel().select(
                peliculasActivas.indexOf(funcion.getPelicula())
        );

        comboSala.getSelectionModel().select(
                salasActivas.indexOf(funcion.getSala())
        );

        // Cargar la fecha y hora de la función.
        LocalDateTime fechaHora = funcion.getFechaHora();

        selectorFecha.setValue(fechaHora.toLocalDate());

        comboHora.setValue(
                String.format("%02d", fechaHora.getHour())
        );

        String minutos = String.format(
                "%02d", fechaHora.getMinute()
        );

        // Incorporar los minutos actuales si no están disponibles.
        if (!comboMinutos.getItems().contains(minutos)) {
            comboMinutos.getItems().add(minutos);
        }

        comboMinutos.setValue(minutos);

        // Cargar el resto de los datos.
        comboFormato.setValue(funcion.getFormatoPelicula());

        comboIdioma.setValue(funcion.getIdiomaPelicula());

        campoPrecio.setText(
                String.valueOf(funcion.getPrecio())
        );

        // Verificar si existen restricciones para la edición.
        boolean tieneReservas
                = cine.getServicioDeReservas().tieneReservas(funcion);

        boolean bloquearProgramacion
                = tieneReservas || !funcion.isActiva();

        // Bloquear los datos de programación cuando corresponda.
        comboPelicula.setDisable(bloquearProgramacion);
        comboSala.setDisable(bloquearProgramacion);
        selectorFecha.setDisable(bloquearProgramacion);
        comboHora.setDisable(bloquearProgramacion);
        comboMinutos.setDisable(bloquearProgramacion);
        comboFormato.setDisable(bloquearProgramacion);
        comboIdioma.setDisable(bloquearProgramacion);

        tituloFormulario.setText(
                bloquearProgramacion
                        ? "Editar precio de la función"
                        : "Editar función"
        );

        botonGuardar.setText("Guardar cambios");
    }

    /**
     * Cancela el registro o la edición de una función.
     *
     * Regresa al listado de funciones sin guardar las modificaciones del
     * formulario.
     */
    @FXML
    private void cancelar() {
        principalController.mostrarFunciones();
    }

    /**
     * Registra una nueva función o modifica una existente.
     *
     * Obtiene los datos ingresados en el formulario y crea una copia del cine
     * para realizar las modificaciones sin alterar directamente el estado
     * original.
     *
     * Si se está editando una función con reservas asociadas, únicamente
     * permite modificar su precio.
     *
     * Si la función no tiene reservas y está activa, permite modificar su
     * programación completa.
     *
     * Para registrar una nueva función, verifica que los campos obligatorios
     * estén completos, obtiene los objetos de película y sala dentro de la
     * copia y genera el siguiente identificador.
     *
     * Las clases Cine y Funcion verifican las reglas de negocio, incluyendo los
     * datos válidos y las superposiciones de horarios.
     *
     * Una vez guardados los cambios, actualiza el cine activo y regresa al
     * listado de funciones.
     *
     * Si ocurre un error, lo muestra en el formulario.
     */
    @FXML
    private void guardarFuncion() {

        ocultarError();

        try {

            double precio;

            try {

                precio = Double.parseDouble(
                        campoPrecio.getText().trim().replace(',', '.')
                );

            } catch (NumberFormatException e) {

                throw new IllegalArgumentException(
                        "El precio debe ser un numero"
                );
            }

            // Trabajar sobre una copia del cine.
            Cine cineTrabajo = cine.copiar();

            if (funcionEnEdicion != null) {

                // EDITAR FUNCIÓN
                Funcion actual = cineTrabajo.buscarFuncionPorId(
                        funcionEnEdicion.getIdFuncion()
                );

                boolean tieneReservas
                        = cineTrabajo.getServicioDeReservas()
                                .tieneReservas(actual);

                if (tieneReservas || !actual.isActiva()) {

                    // Modificar únicamente el precio.
                    cineTrabajo.editarPrecioFuncion(
                            actual.getIdFuncion(),
                            precio
                    );

                } else {

                    // Modificar la programación completa.
                    int posPelicula
                            = comboPelicula.getSelectionModel()
                                    .getSelectedIndex();

                    int posSala
                            = comboSala.getSelectionModel()
                                    .getSelectedIndex();

                    LocalDate fecha = selectorFecha.getValue();

                    if (posPelicula < 0
                            || posSala < 0
                            || fecha == null
                            || comboHora.getValue() == null
                            || comboMinutos.getValue() == null
                            || comboFormato.getValue() == null
                            || comboIdioma.getValue() == null) {

                        throw new IllegalArgumentException(
                                "Complete todos los datos de la función"
                        );
                    }

                    LocalDateTime fechaHora = fecha.atTime(
                            Integer.parseInt(comboHora.getValue()),
                            Integer.parseInt(comboMinutos.getValue())
                    );

                    Pelicula pelicula
                            = peliculasActivas.get(posPelicula);

                    Sala sala
                            = salasActivas.get(posSala);

                    cineTrabajo.editarFuncion(
                            actual.getIdFuncion(),
                            sala.getIdSala(),
                            pelicula.getIdPelicula(),
                            precio,
                            fechaHora,
                            comboFormato.getValue(),
                            comboIdioma.getValue()
                    );
                }

            } else {

                // REGISTRAR NUEVA FUNCIÓN
                int posPelicula
                        = comboPelicula.getSelectionModel()
                                .getSelectedIndex();

                int posSala
                        = comboSala.getSelectionModel()
                                .getSelectedIndex();

                LocalDate fecha = selectorFecha.getValue();

                if (posPelicula < 0) {
                    throw new IllegalArgumentException(
                            "Seleccione una pelicula"
                    );
                }

                if (posSala < 0) {
                    throw new IllegalArgumentException(
                            "Seleccione una sala"
                    );
                }

                if (fecha == null) {
                    throw new IllegalArgumentException(
                            "Seleccione una fecha"
                    );
                }

                if (comboHora.getValue() == null
                        || comboMinutos.getValue() == null) {

                    throw new IllegalArgumentException(
                            "Seleccione la hora de inicio"
                    );
                }

                if (comboFormato.getValue() == null) {
                    throw new IllegalArgumentException(
                            "Seleccione un formato"
                    );
                }

                if (comboIdioma.getValue() == null) {
                    throw new IllegalArgumentException(
                            "Seleccione un idioma"
                    );
                }

                // Construir la fecha y hora de inicio.
                int hora = Integer.parseInt(
                        comboHora.getValue()
                );

                int minutos = Integer.parseInt(
                        comboMinutos.getValue()
                );

                LocalDateTime fechaHora = fecha.atTime(
                        hora, minutos
                );

                // Buscar película y sala dentro de la copia,
                // porque Cine las compara por identidad.
                Pelicula pelicula
                        = cineTrabajo.buscarPeliculaPorId(
                                peliculasActivas.get(posPelicula)
                                        .getIdPelicula()
                        );

                Sala sala
                        = cineTrabajo.buscarSalaPorId(
                                salasActivas.get(posSala)
                                        .getIdSala()
                        );

                // Crear la función con un identificador nuevo.
                Funcion funcion = new Funcion(
                        obtenerSiguienteId(cineTrabajo),
                        sala,
                        pelicula,
                        precio,
                        fechaHora,
                        comboFormato.getValue(),
                        comboIdioma.getValue()
                );

                // Registrar la función después de las validaciones.
                cineTrabajo.programarFuncion(funcion);
            }

            // Persistir antes de actualizar el cine activo.
            persistencia.guardar(cineTrabajo);

            principalController.actualizarCine(cineTrabajo);

            principalController.mostrarFunciones();

        } catch (IOException | ClassNotFoundException e) {

            // Error relacionado con la copia o persistencia.
            mostrarError(
                    "Error al guardar los datos: " + e.getMessage()
            );

        } catch (Exception e) {

            // Mostrar errores de validación o reglas de negocio.
            mostrarError(e.getMessage());
        }
    }

    /**
     * Calcula el siguiente identificador disponible para una función.
     *
     * Recorre las funciones registradas, obtiene el mayor identificador
     * existente y devuelve ese valor incrementado en uno.
     *
     * @param unCine Cine cuyas funciones se desean consultar.
     * 
     * @return Mayor identificador de función incrementado en uno.
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

    /**
     * Muestra un mensaje de error dentro del formulario.
     *
     * Asigna el texto recibido a la etiqueta de error y habilita su
     * visualización en la interfaz.
     *
     * @param mensaje Texto del error que se desea mostrar.
     */
    private void mostrarError(String mensaje) {

        mensajeError.setText(mensaje);

        mensajeError.setVisible(true);

        mensajeError.setManaged(true);
    }

    /**
     * Oculta el mensaje de error del formulario.
     *
     * También evita que la etiqueta ocupe espacio dentro de la distribución de
     * la interfaz.
     */
    private void ocultarError() {

        mensajeError.setVisible(false);

        mensajeError.setManaged(false);
    }
}
