package servicios;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import entidades.TemperaturaCiudad;


public class TemperaturaCiudadServicio {

        public static List<TemperaturaCiudad> getDatos(String nombreArchivo) {
                DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("d/M/yyyy");
                try {
                        Stream<String> lineas = Files.lines(Paths.get(nombreArchivo));
                        return lineas.skip(1)
                                        .map(linea -> linea.split(","))
                                        .map(textos -> new TemperaturaCiudad(textos[0],
                                                        LocalDate.parse(textos[1], formatoFecha),
                                                        Double.parseDouble(textos[2])))
                                        .collect(Collectors.toList());
                } catch (Exception ex) {
                        return Collections.emptyList();
                }
        }

        public static List<TemperaturaCiudad> filtrar(LocalDate desde, LocalDate hasta,
                        List<TemperaturaCiudad> datos) {
                return datos.stream()
                                .filter(dato -> !dato.getFecha().isBefore(desde)
                                                && !dato.getFecha().isAfter(hasta))
                                .collect(Collectors.toList());
        }

        public static double getPromedioTemperaturaCiudad(List<TemperaturaCiudad> datos, String ciudad, LocalDate desde,
                        LocalDate hasta) {
                List<TemperaturaCiudad> datosFiltrados = filtrar(desde, hasta, datos);

                return datosFiltrados.stream()
                                .filter(dato -> dato.getCiudad().equalsIgnoreCase(ciudad))
                                .mapToDouble(TemperaturaCiudad::getTemperatura)
                                .average()
                                .orElse(0.0);
        }

        public static Map<String, Double> getPromediosTemperaturas(LocalDate desde, LocalDate hasta,
                        List<TemperaturaCiudad> datos) {

                List<String> ciudades = datos.stream()
                                .map(TemperaturaCiudad::getCiudad)
                                .distinct()
                                .sorted()
                                .collect(Collectors.toList());

                return ciudades.stream()
                                .collect(Collectors.toMap(
                                                ciudad -> ciudad,
                                                ciudad -> getPromedioTemperaturaCiudad(datos, ciudad, desde, hasta)));
        }

        public static Map<String, String> getTemperaturasExtremas(LocalDate desde, List<TemperaturaCiudad> datos) {

                List<TemperaturaCiudad> filtrarDatosFecha = datos.stream()
                                .filter(dato -> dato.getFecha().equals(desde))
                                .collect(Collectors.toList());

                Optional<TemperaturaCiudad> max = filtrarDatosFecha.stream()
                                .max(Comparator.comparingDouble(TemperaturaCiudad::getTemperatura));

                Optional<TemperaturaCiudad> min = filtrarDatosFecha.stream()
                                .min(Comparator.comparingDouble(TemperaturaCiudad::getTemperatura));

                String fechaFormateada = desde.toString();

                Map<String, String> temperaturasExtremas = new LinkedHashMap<>();

                temperaturasExtremas.put("Fecha: ", fechaFormateada);
                temperaturasExtremas.put("Ciudad más calurosa:  ",
                                max.get().getCiudad() + " (" + max.get().getTemperatura() + "°C)\n");
                temperaturasExtremas.put("Ciudad más fría:  ",
                                min.get().getCiudad() + " (" + min.get().getTemperatura() + "°C)");

                return temperaturasExtremas;
        }
}
