package gestordecine.misreservas;

import gestordecine.Cine;
import gestordecine.Cliente;
import gestordecine.controladores.NuevaReservaController;
import gestordecine.controladores.PrincipalController;
import gestordecine.Reserva;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import gestordecine.PersistenciaCine;
import gestordecine.VentanaComprobante;
import gestordecine.DialogoCinema;
import gestordecine.sesion.SesionCliente;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import java.util.Locale;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * Controlador de la pantalla Mis reservas.
 *
 * Muestra únicamente las reservas asociadas al cliente que inició sesión en
 * CINEMA.
 *
 * @author Rodrigo Arregin
 */
public class MisReservasController {

    private Cine cine;
    private PrincipalController principalController;
    private PersistenciaCine persistencia;

    private static final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    private Label etiquetaCliente;

    @FXML
    private VBox contenedorReservas;

    /**
     * Configura la pantalla y carga las reservas del cliente autenticado.
     *
     * @param cine Cine con los datos actuales.
     * @param persistencia Administrador del archivo de datos.
     * @param principalController Controlador de navegación principal.
     */
    public void configurar(Cine cine, PersistenciaCine persistencia,
            PrincipalController principalController) {

        this.cine = cine;
        this.persistencia = persistencia;
        this.principalController = principalController;
        cargarReservas();
    }

    /**
     * Consulta y muestra las reservas del cliente que inició sesión. No permite
     * visualizar las reservas de otros clientes.
     */
    private void cargarReservas() {

        contenedorReservas.getChildren().clear();

        Cliente clienteSesion = NuevaReservaController.getClienteActual();

        if (clienteSesion == null) {
            etiquetaCliente.setText("No hay una sesión iniciada.");
            mostrarMensaje("Iniciá sesión desde Nueva reserva para consultar tus compras.");
            return;
        }

        // Consultar los datos vigentes del cliente dentro del cine.
        Cliente cliente = cine.getServicioDeReservas()
                .buscarClientePorDni(clienteSesion.getDni());

        if (cliente == null) {
            etiquetaCliente.setText("Cliente no encontrado");
            mostrarMensaje("No se encontró el cliente en los datos del cine.");
            return;
        }

        etiquetaCliente.setText(
                cliente.getNombre() + " " + cliente.getApellido()
                + " | DNI " + cliente.getDni()
        );

        ArrayList<Reserva> reservas = cine.getServicioDeReservas()
                .buscarPorDni(cliente.getDni());

        if (reservas.isEmpty()) {
            mostrarMensaje("Todavía no tenés reservas registradas.");
            return;
        }

        // Mostrar una tarjeta por cada reserva, incluidas las canceladas.
        for (Reserva reserva : reservas) {
            contenedorReservas.getChildren().add(crearTarjeta(reserva));
        }
    }

    /**
     * Crea una tarjeta visual con los datos de una reserva.
     *
     * Organiza la información en secciones, diferencia el estado de
     * confirmación y conserva las acciones de comprobante y cancelación en la
     * parte inferior.
     *
     * @param reserva Reserva que se desea mostrar.
     * @return Tarjeta con los datos y acciones disponibles.
     */
    private VBox crearTarjeta(Reserva reserva) {

        boolean confirmada = reserva.isConfirmada();

        // ENCABEZADO: PELÍCULA Y CÓDIGO
        Label titulo = new Label(
                reserva.getFuncion().getPelicula().getTituloPelicula()
        );

        titulo.setStyle(
                "-fx-text-fill: white;"
                + "-fx-font-size: 18px;"
                + "-fx-font-weight: bold;"
        );
        titulo.setWrapText(true);

        Label codigo = new Label(
                "Reserva " + reserva.getCodigoReserva()
        );
        codigo.setStyle(
                "-fx-text-fill: #94A3B8;"
                + "-fx-font-size: 12px;"
        );

        VBox informacionPrincipal = new VBox(5, titulo, codigo);
        HBox.setHgrow(informacionPrincipal, Priority.ALWAYS);

        // ESTADO DE LA RESERVA
        Label estado = new Label(
                confirmada ? "CONFIRMADA" : "CANCELADA"
        );

        estado.setStyle(
                "-fx-background-color: "
                + (confirmada ? "#14532D;" : "#7F1D1D;")
                + "-fx-text-fill: "
                + (confirmada ? "#4ADE80;" : "#FCA5A5;")
                + "-fx-font-weight: bold;"
                + "-fx-font-size: 12px;"
                + "-fx-background-radius: 12;"
                + "-fx-padding: 6 12;"
        );

        HBox encabezado = new HBox(20, informacionPrincipal, estado);
        encabezado.setAlignment(Pos.CENTER_LEFT);

        // SEPARADOR
        Region separadorSuperior = new Region();
        separadorSuperior.setPrefHeight(1);
        separadorSuperior.setStyle(
                "-fx-background-color: #334155;"
        );

        // DATOS DE LA RESERVA
        String butacas = reserva.getEntradas().stream()
                .map(entrada -> entrada.getButaca().getIdentificador())
                .collect(Collectors.joining(", "));

        VBox datoFecha = crearDatoReserva(
                "FECHA Y HORA",
                reserva.getFuncion().getFechaHora().format(FORMATO_FECHA)
        );

        VBox datoSala = crearDatoReserva(
                "SALA",
                reserva.getFuncion().getSala().getNombreSala()
        );

        VBox datoButacas = crearDatoReserva(
                "BUTACAS",
                butacas
        );

        VBox datoEntradas = crearDatoReserva(
                "ENTRADAS",
                String.valueOf(reserva.getEntradas().size())
        );

        GridPane datos = new GridPane();
        datos.setHgap(30);
        datos.setVgap(18);

        datos.add(datoFecha, 0, 0);
        datos.add(datoSala, 1, 0);
        datos.add(datoButacas, 0, 1);
        datos.add(datoEntradas, 1, 1);

        // TOTAL
        Label etiquetaTotal = new Label("TOTAL DE LA RESERVA");
        etiquetaTotal.setStyle(
                "-fx-text-fill: #94A3B8;"
                + "-fx-font-size: 12px;"
        );

        Label importeTotal = new Label(
                String.format(
                        Locale.of("es", "AR"),
                        "$%,.2f",
                        reserva.getImporteTotal()
                )
        );

        importeTotal.setStyle(
                "-fx-text-fill: white;"
                + "-fx-font-size: 20px;"
                + "-fx-font-weight: bold;"
        );

        VBox total = new VBox(5, etiquetaTotal, importeTotal);

        // ACCIONES: MANTENER LOS BOTONES ABAJO A LA IZQUIERDA
        Button botonComprobante = new Button("Ver comprobante");
        botonComprobante.getStyleClass().add("boton-secundario");
        botonComprobante.setPrefHeight(40);

        botonComprobante.setOnAction(
                evento -> verComprobante(reserva)
        );

        HBox botones = new HBox(12, botonComprobante);
        botones.setAlignment(Pos.CENTER_LEFT);

        // Solo se pueden cancelar reservas confirmadas y futuras.
        boolean puedeCancelar = confirmada
                && reserva.getFuncion().getFechaHora()
                        .isAfter(LocalDateTime.now());

        if (puedeCancelar) {

            Button botonCancelar = new Button("Cancelar reserva");
            botonCancelar.getStyleClass().add("boton-peligro");
            botonCancelar.setPrefHeight(40);

            botonCancelar.setOnAction(
                    evento -> cancelarReserva(reserva)
            );

            botones.getChildren().add(botonCancelar);
        }

        // TARJETA FINAL
        VBox tarjeta = new VBox(
                16,
                encabezado,
                separadorSuperior,
                datos,
                total,
                botones
        );

        tarjeta.setPadding(new Insets(20));

        tarjeta.setStyle(
                "-fx-background-color: #151F30;"
                + "-fx-background-radius: 12;"
                + "-fx-border-color: #334155;"
                + "-fx-border-radius: 12;"
        );

        return tarjeta;
    }

    /**
     * Crea un bloque de información para una tarjeta de reserva.
     *
     * @param etiqueta Nombre del dato que se muestra.
     * @param valor Contenido correspondiente al dato.
     * @return Contenedor con la etiqueta y su valor.
     */
    private VBox crearDatoReserva(String etiqueta, String valor) {

        Label tituloDato = new Label(etiqueta);
        tituloDato.setStyle(
                "-fx-text-fill: #94A3B8;"
                + "-fx-font-size: 11px;"
        );

        Label valorDato = new Label(valor);
        valorDato.setStyle(
                "-fx-text-fill: #E2E8F0;"
                + "-fx-font-size: 14px;"
                + "-fx-font-weight: bold;"
        );
        valorDato.setWrapText(true);

        return new VBox(5, tituloDato, valorDato);
    }

    /**
     * Cancela una reserva del cliente autenticado.
     *
     * Verifica que la reserva pertenezca al cliente y que la función todavía no
     * haya comenzado.
     *
     * Trabaja sobre una copia del cine para evitar modificar los datos
     * originales si ocurre un error al guardar.
     *
     * @param reserva Reserva que el cliente desea cancelar.
     */
    private void cancelarReserva(Reserva reserva) {

        Cliente clienteActual = SesionCliente.getClienteActual();

        if (clienteActual == null) {
            DialogoCinema.mostrarError(
                    contenedorReservas.getScene().getWindow(),
                    "Sesión requerida",
                    "Debés iniciar sesión para cancelar una reserva."
            );
            principalController.mostrarCartelera();
            return;
        }

        // Confirmar que la reserva pertenezca al cliente.
        if (reserva.getCliente().getDni() != clienteActual.getDni()) {
            DialogoCinema.mostrarError(
                    contenedorReservas.getScene().getWindow(),
                    "Acceso denegado",
                    "No podés cancelar una reserva de otro cliente."
            );
            return;
        }

        // Solicitar confirmación antes de cancelar.
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);

        confirmacion.setTitle("Cancelar reserva");
        confirmacion.setHeaderText("¿Querés cancelar esta reserva?");
        confirmacion.setContentText(
                "Código: " + reserva.getCodigoReserva()
                + "\nEsta operación liberará las butacas reservadas."
        );

        confirmacion.initOwner(
                contenedorReservas.getScene().getWindow()
        );

        ButtonType respuesta = confirmacion.showAndWait()
                .orElse(ButtonType.CANCEL);

        if (respuesta != ButtonType.OK) {
            return;
        }

        try {

            // Trabajar sobre una copia del cine.
            Cine cineTrabajo = cine.copiar();

            Reserva reservaTrabajo = cineTrabajo
                    .getServicioDeReservas()
                    .buscarPorCodigo(reserva.getCodigoReserva());

            if (reservaTrabajo == null) {
                throw new IllegalArgumentException(
                        "La reserva ya no existe."
                );
            }

            // Revalidar que pertenezca al cliente autenticado.
            if (reservaTrabajo.getCliente().getDni()
                    != clienteActual.getDni()) {

                throw new IllegalArgumentException(
                        "La reserva no pertenece al cliente autenticado."
                );
            }

            // El servicio verifica el estado y la fecha.
            cineTrabajo.getServicioDeReservas()
                    .cancelarReserva(reservaTrabajo.getCodigoReserva());

            // Guardar los cambios solamente si la cancelación fue válida.
            persistencia.guardar(cineTrabajo);

            principalController.actualizarCine(cineTrabajo);
            this.cine = cineTrabajo;

            // Actualizar las tarjetas con los nuevos estados.
            cargarReservas();

            DialogoCinema.mostrarExito(
                    contenedorReservas.getScene().getWindow(),
                    "Reserva cancelada",
                    "La reserva se canceló correctamente y sus "
                    + "butacas quedaron disponibles."
            );

        } catch (IOException | ClassNotFoundException e) {

            DialogoCinema.mostrarError(
                    contenedorReservas.getScene().getWindow(),
                    "Error al guardar",
                    "No se pudo guardar la cancelación: " + e.getMessage()
            );

        } catch (IllegalArgumentException | IllegalStateException e) {

            DialogoCinema.mostrarError(
                    contenedorReservas.getScene().getWindow(),
                    "No se puede cancelar la reserva",
                    e.getMessage()
            );
        }
    }

    /**
     * Muestra el comprobante de una reserva perteneciente al cliente que inició
     * sesión.
     *
     * @param reserva Reserva cuyo comprobante se desea consultar.
     */
    private void verComprobante(Reserva reserva) {

        Cliente cliente = SesionCliente.getClienteActual();

        if (cliente == null
                || reserva.getCliente().getDni() != cliente.getDni()) {

            DialogoCinema.mostrarError(
                    contenedorReservas.getScene().getWindow(),
                    "Acceso denegado",
                    "No podés consultar esta reserva."
            );
            return;
        }

        VentanaComprobante.mostrar(reserva);
    }

    /**
     * Muestra un mensaje cuando no existen reservas para visualizar.
     *
     * @param mensaje Texto informativo que se desea mostrar.
     */
    private void mostrarMensaje(String mensaje) {
        Label etiqueta = new Label(mensaje);
        etiqueta.setStyle(
                "-fx-text-fill: #94A3B8;"
                + "-fx-font-size: 14px;"
        );
        contenedorReservas.getChildren().add(etiqueta);
    }

    /**
     * Regresa a la cartelera principal.
     */
    @FXML
    private void volverCartelera() {
        principalController.volver();
    }
}
