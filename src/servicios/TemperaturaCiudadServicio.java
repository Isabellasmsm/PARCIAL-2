package servicios;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JOptionPane;

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

        public static double getMaximo(List<Double> datos) {
                return datos.isEmpty() ? 0 : datos.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        }

        public static double getMinimo(List<Double> datos) {
                return datos.isEmpty() ? 0 : datos.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        }

        public static Map<String, Double> getPromediosTemperaturas(LocalDate desde, LocalDate hasta,
                        List<TemperaturaCiudad> datos) {
                List<String> ciudades = Arrays.asList("Barranquilla", "Bogotá", "Cali", "Cartagena", "Medellín");
                Map<String, Double> promedios = new LinkedHashMap<>();

                ciudades.forEach(ciudad -> {
                        double promedio = getPromedioTemperaturaCiudad(datos, ciudad, desde, hasta);
                        promedios.put(ciudad, promedio);
                });

                return promedios;

        }

        public static Map<String, String> getTemperaturasExtremas(LocalDate desde, List<TemperaturaCiudad> datos) {

                List<TemperaturaCiudad> filtrarDatosFecha = datos.stream()
                                .filter(dato -> dato.getFecha().equals(desde))
                                .collect(Collectors.toList());
                if (filtrarDatosFecha.size() > 0) {
                        double maxTemperatura = TemperaturaCiudadServicio.getMaximo(
                                        filtrarDatosFecha.stream().map(TemperaturaCiudad::getTemperatura)
                                                        .collect(Collectors.toList()));
                        double minTemperatura = TemperaturaCiudadServicio.getMinimo(
                                        filtrarDatosFecha.stream().map(TemperaturaCiudad::getTemperatura)
                                                        .collect(Collectors.toList()));

                        TemperaturaCiudad max = filtrarDatosFecha.stream()
                                        .filter(dato -> dato.getTemperatura() == maxTemperatura)
                                        .collect(Collectors.toList()).get(0);
                        ;
                        TemperaturaCiudad min = filtrarDatosFecha.stream()
                                        .filter(dato -> dato.getTemperatura() == minTemperatura)
                                        .collect(Collectors.toList()).get(0);
                        String fechaFormateada = desde.toString();
                        Map<String, String> temperaturasExtremas = new LinkedHashMap<>();
                        temperaturasExtremas.put("Fecha: ", fechaFormateada);
                        temperaturasExtremas.put("Ciudad más calurosa:  ",
                                        max.getCiudad() + " (" + max.getTemperatura() + "°C)\n");
                        temperaturasExtremas.put("Ciudad más fría:  ",
                                        min.getCiudad() + " (" + min.getTemperatura() + "°C)");

                        return temperaturasExtremas;
                }
                JOptionPane.showMessageDialog(null, "No hay datos en esas fechas", "INFORMATION_MESSAGE", JOptionPane.INFORMATION_MESSAGE);
                return new HashMap<>();
        }
}
