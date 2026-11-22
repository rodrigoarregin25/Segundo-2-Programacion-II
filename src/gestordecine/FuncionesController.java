package gestordecine;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class FuncionesController {

    private Cine cine;
    private PersistenciaCine persistencia;
    private PrincipalController principalController;

    // Copias de las listas que se muestran en los desplegables.
    // La posicion elegida en el desplegable coincide con la posicion en la lista.
    private ArrayList<Pelicula> peliculasActivas;
    private ArrayList<Sala> salasActivas;

    @FXML
    private ListView<Funcion> listaFunciones;
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
     * (formato, idioma, hora y minutos) y arma como se ve cada funcion
     * en la lista. JavaFX lo llama solo al abrir la pantalla.
     */
    @FXML
    private void initialize() {
        comboFormato.getItems().addAll("2D", "3D", "D-BOX", "4D");
        comboIdioma.getItems().addAll("INGLES", "CASTELLANO", "INGLES(SUBTITULADA)");

        for (int hora = 0; hora < 24; hora++) {
            comboHora.getItems().add(String.format("%02d", hora));
        }
        comboMinutos.getItems().addAll("00", "15", "30", "45");

        // Cada fila de la lista se dibuja como una tarjeta
        listaFunciones.setCellFactory(lista -> new ListCell<Funcion>() {
            @Override
            protected void updateItem(Funcion funcion, boolean vacia) {
                super.updateItem(funcion, vacia);
                if (vacia || funcion == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                    return;
                }
                HBox tarjeta = crearTarjeta(funcion);
                tarjeta.setStyle(estiloTarjeta(isSelected()));
                tarjeta.prefWidthProperty().bind(
                        getListView().widthProperty().subtract(40));
                setGraphic(tarjeta);
                setText(null);
                setStyle("-fx-background-color: transparent; -fx-padding: 4 0 4 0;");
            }
        });

        // Al elegir otra tarjeta, se repinta la lista para marcar la elegida
        listaFunciones.getSelectionModel().selectedIndexProperty().addListener(
                (observable, anterior, nuevo) -> listaFunciones.refresh());
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
     * La posicion de cada tarjeta coincide con la posicion de la funcion en el cine.
     */
    private void cargarLista() {
        listaFunciones.setItems(FXCollections.observableArrayList(cine.getFunciones()));
    }

    /**
     * Que hace: arma la tarjeta de una funcion (datos a la izquierda, precio a la derecha).
     * Parametros: funcion (la funcion a mostrar).
     * Retorna: la tarjeta lista para mostrar en la lista.
     */
    private HBox crearTarjeta(Funcion funcion) {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        Label titulo = new Label(funcion.getPelicula().getTituloPelicula());
        titulo.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label detalle = new Label(funcion.getSala().getNombreSala()
                + "  ·  " + funcion.getFechaHora().format(formato));
        detalle.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 13px;");

        HBox etiquetas = new HBox(8,
                crearEtiqueta(funcion.getFormatoPelicula()),
                crearEtiqueta(funcion.getIdiomaPelicula()));

        VBox datos = new VBox(6, titulo, detalle, etiquetas);

        Label precio = new Label(String.format(Locale.of("es", "AR"),
                "$%,.2f", funcion.getPrecio()));
        precio.setStyle("-fx-text-fill: #60A5FA; -fx-font-size: 20px; -fx-font-weight: bold;");
        Label porEntrada = new Label("por entrada");
        porEntrada.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px;");
        VBox bloquePrecio = new VBox(2, precio, porEntrada);
        bloquePrecio.setAlignment(Pos.CENTER_RIGHT);

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        HBox tarjeta = new HBox(15, datos, espacio, bloquePrecio);
        tarjeta.setAlignment(Pos.CENTER_LEFT);
        return tarjeta;
    }

    /**
     * Que hace: arma una etiqueta chica (por ejemplo el formato o el idioma).
     * Parametros: texto (lo que dice la etiqueta).
     * Retorna: el Label con estilo.
     */
    private Label crearEtiqueta(String texto) {
        Label etiqueta = new Label(texto);
        etiqueta.setStyle("-fx-background-color: #1E293B; -fx-text-fill: #CBD5E1;"
                + " -fx-padding: 2 10 2 10; -fx-background-radius: 10; -fx-font-size: 11px;");
        return etiqueta;
    }

    /**
     * Que hace: devuelve el estilo de la tarjeta, con borde azul si esta elegida.
     * Parametros: seleccionada (true si es la tarjeta elegida).
     * Retorna: el estilo en texto.
     */
    private String estiloTarjeta(boolean seleccionada) {
        String borde = "#334155";
        if (seleccionada) {
            borde = "#3B82F6";
        }
        return "-fx-background-color: #111C30; -fx-background-radius: 10;"
                + " -fx-border-color: " + borde + "; -fx-border-radius: 10;"
                + " -fx-border-width: 1.5; -fx-padding: 14 18 14 18;";
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
     * Que hace: cambia el precio de la funcion elegida en la lista.
     * El precio nuevo se escribe en el campo Precio. Las reservas ya hechas
     * conservan su importe, porque cada entrada guarda el precio que se pago.
     */
    @FXML
    private void editarPrecio() {
        ocultarError();

        try {
            int posicion = listaFunciones.getSelectionModel().getSelectedIndex();
            if (posicion < 0) {
                throw new IllegalArgumentException("Seleccione una funcion de la lista");
            }

            double precio;
            try {
                precio = Double.parseDouble(campoPrecio.getText().trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Escriba el precio nuevo en el campo Precio");
            }

            // El ID de la funcion elegida; despues se busca dentro de la copia
            int idFuncion = cine.getFunciones().get(posicion).getIdFuncion();

            Cine cineTrabajo = cine.copiar();
            cineTrabajo.editarPrecioFuncion(idFuncion, precio);

            persistencia.guardar(cineTrabajo);
            principalController.actualizarCine(cineTrabajo);
            principalController.mostrarFunciones();

        } catch (IOException | ClassNotFoundException e) {
            mostrarError("Error al guardar los datos: " + e.getMessage());
        } catch (Exception e) {
            mostrarError(e.getMessage());
        }
    }

    /**
     * Que hace: elimina la funcion elegida en la lista.
     * Si la funcion tiene reservas, Cine avisa con un error y no se borra.
     */
    @FXML
    private void eliminarFuncion() {
        ocultarError();

        try {
            int posicion = listaFunciones.getSelectionModel().getSelectedIndex();
            if (posicion < 0) {
                throw new IllegalArgumentException("Seleccione una funcion de la lista");
            }

            int idFuncion = cine.getFunciones().get(posicion).getIdFuncion();

            Cine cineTrabajo = cine.copiar();
            cineTrabajo.eliminarFuncion(idFuncion);

            persistencia.guardar(cineTrabajo);
            principalController.actualizarCine(cineTrabajo);
            principalController.mostrarFunciones();

        } catch (IOException | ClassNotFoundException e) {
            mostrarError("Error al guardar los datos: " + e.getMessage());
        } catch (Exception e) {
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