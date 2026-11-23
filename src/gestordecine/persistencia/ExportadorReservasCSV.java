package gestordecine.persistencia;

import gestordecine.Reserva;
import gestordecine.Entrada;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;

/**
 * Clase encargada de exportar las reservas registradas en CINEMA a un archivo
 * CSV.
 *
 * La exportación incluye reservas confirmadas y canceladas, sin modificar su
 * estado ni la disponibilidad de las butacas.
 *
 * El archivo CSV se utiliza únicamente para consultar información. La
 * persistencia principal continúa realizándose mediante el archivo serializado
 * del cine.
 *
 */
public class ExportadorReservasCSV {

    /**
     * Formato utilizado para representar la fecha y hora de las funciones.
     */
    private static final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Exporta una lista de reservas a un archivo CSV.
     *
     * Cada reserva se escribe en una fila con su código, estado, cliente, DNI,
     * película, sala, fecha, butacas, tipos de entrada e importe total.
     *
     * Si el archivo ya existe, reemplaza su contenido con la nueva exportación.
     * El archivo se cierra automáticamente al finalizar.
     *
     * @param reservas Lista de reservas que se desea exportar.
     * @param ruta Ruta donde se guardará el archivo CSV.
     *
     * @throws IOException Si ocurre un error al crear o escribir el archivo.
     */
    public static void exportar(List<Reserva> reservas, Path ruta)
            throws IOException {

        try (BufferedWriter escritor = Files.newBufferedWriter(
                ruta, StandardCharsets.UTF_8)) {

            // Escribir los encabezados de las columnas.
            escritor.write(
                    "Codigo;Estado;Cliente;DNI;Pelicula;Sala;"
                    + "FechaHora;Butacas;TiposEntrada;Total"
            );
            escritor.newLine();

            // Recorrer las reservas y escribir una fila por cada una.
            for (Reserva reserva : reservas) {

                // Obtener los identificadores de las butacas reservadas.
                String butacas = reserva.getEntradas().stream()
                        .map(entrada -> entrada.getButaca().getIdentificador())
                        .collect(Collectors.joining(", "));

                // Obtener los tipos de entrada incluidos en la reserva.
                String tipos = reserva.getEntradas().stream()
                        .map(Entrada::getTipo)
                        .collect(Collectors.joining(", "));

                // Preparar los valores correspondientes a cada columna.
                String[] columnas = {
                    reserva.getCodigoReserva(),
                    reserva.isConfirmada() ? "CONFIRMADA" : "CANCELADA",
                    reserva.getCliente().getNombre() + " "
                    + reserva.getCliente().getApellido(),
                    String.valueOf(reserva.getCliente().getDni()),
                    reserva.getFuncion().getPelicula().getTituloPelicula(),
                    reserva.getFuncion().getSala().getNombreSala(),
                    reserva.getFuncion().getFechaHora().format(FORMATO_FECHA),
                    butacas,
                    tipos,
                    String.valueOf(reserva.getImporteTotal())
                };

                // Escapar los valores y separarlos mediante punto y coma.
                escritor.write(
                        Arrays.stream(columnas)
                                .map(ExportadorReservasCSV::escapar)
                                .collect(Collectors.joining(";"))
                );

                escritor.newLine();
            }
        }
    }

    /**
     * Prepara un texto para escribirlo correctamente en una columna CSV.
     *
     * Encierra el texto entre comillas dobles y duplica las comillas internas
     * para evitar problemas con los separadores del archivo.
     *
     * @param texto Texto que se desea preparar para el archivo CSV.
     *
     * @return Texto escapado o una cadena vacía si el valor es nulo.
     */
    private static String escapar(String texto) {

        if (texto == null) {
            return "";
        }

        return "\"" + texto.replace("\"", "\"\"") + "\"";
    }
}
