package colectivo.logica;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import colectivo.modelo.Frecuencia;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import colectivo.modelo.Tramo;

public class Calculo {

    /**
     * Calcula los posibles recorridos entre una parada de origen y una de destino
     * en un dia y hora determinados.
     *
     * @param paradaOrigen    Parada de origen
     * @param paradaDestino   Parada de destino
     * @param diaSemana       Dia de la semana (1 a 7)
     * @param horaLlegaParada Hora de llegada a la parada de origen
     * @param tramos          Mapa de tramos entre paradas
     * @return Lista de listas de recorridos posibles
     */
    public static List<List<Recorrido>> calcularRecorrido(Parada paradaOrigen, Parada paradaDestino, int diaSemana,
                                                          LocalTime horaLlegaParada, Map<String, Tramo> tramos) {
        List<List<Recorrido>> recorridos = new ArrayList<>();
        List<Recorrido> recorridoActual = new ArrayList<>();
        // Track visited stops to avoid cycles
        List<Parada> paradasVisitadas = new ArrayList<>();
        buscarRecorridos(paradaOrigen, paradaDestino, tramos, recorridoActual, recorridos, horaLlegaParada, diaSemana, paradasVisitadas);
        return recorridos;
    }

    private static void buscarRecorridos(Parada actual, Parada destino, Map<String, Tramo> tramos,
                                         List<Recorrido> recorridoActual, List<List<Recorrido>> recorridos,
                                         LocalTime horaLlegaParada, int diaSemana, List<Parada> paradasVisitadas) {
        //Condicion de parada de recursividad
        if (actual.equals(destino)) {
            recorridos.add(new ArrayList<>(recorridoActual));
            return;
        }

        // Mark current stop as visited
        paradasVisitadas.add(actual);

        // Find all tramos starting from current stop
        for (Tramo tramoInicial : tramos.values()) {
            if (tramoInicial.getInicio().equals(actual) && !paradasVisitadas.contains(tramoInicial.getFin())) {
                // Get ALL possible lines for this tramo
                List<Linea> lineasPosibles = obtenerTodasLasLineas(tramoInicial.getInicio(), tramoInicial.getFin());

                // Try each line separately
                for (Linea lineaActual : lineasPosibles) {
                    // Build a recorrido following the same line as far as possible
                    List<Parada> paradasRecorrido = new ArrayList<>();
                    paradasRecorrido.add(tramoInicial.getInicio());

                    int duracionTotal = 0;
                    Parada paradaActual = tramoInicial.getInicio();
                    List<Parada> paradasNuevas = new ArrayList<>();

                    // Calculate the departure time for this line at the current stop
                    LocalTime horaSalida = calcularProximaSalida(lineaActual, paradaActual, horaLlegaParada, diaSemana, tramos);

                    // Follow the line as far as we can
                    while (paradaActual != null && !paradaActual.equals(destino)) {
                        Tramo tramoSiguiente = null;

                        // Find next tramo on the same line
                        for (Tramo t : tramos.values()) {
                            if (t.getInicio().equals(paradaActual) && !paradasVisitadas.contains(t.getFin()) && !paradasNuevas.contains(t.getFin())) {
                                List<Linea> lineasTramo = obtenerTodasLasLineas(t.getInicio(), t.getFin());
                                if (lineasTramo.contains(lineaActual)) {
                                    tramoSiguiente = t;
                                    break;
                                }
                            }
                        }

                        if (tramoSiguiente != null) {
                            paradasNuevas.add(tramoSiguiente.getFin());
                            paradasRecorrido.add(tramoSiguiente.getFin());
                            duracionTotal += tramoSiguiente.getTiempo();
                            paradaActual = tramoSiguiente.getFin();
                        } else {
                            break;
                        }
                    }

                    // Only create a Recorrido if we moved at least one tramo
                    if (!paradasNuevas.isEmpty()) {
                        Recorrido r = new Recorrido(lineaActual, paradasRecorrido, horaSalida, duracionTotal);

                        recorridoActual.add(r);
                        paradasVisitadas.addAll(paradasNuevas);

                        buscarRecorridos(paradaActual, destino, tramos, recorridoActual, recorridos, horaSalida, diaSemana, paradasVisitadas);

                        recorridoActual.remove(recorridoActual.size() - 1);
                        paradasVisitadas.removeAll(paradasNuevas);
                    }
                }
            }
        }

        // Unmark current stop when backtracking
        paradasVisitadas.remove(actual);
    }

    private static List<Linea> obtenerTodasLasLineas(Parada inicio, Parada fin) {
        List<Linea> lineasComunes = new ArrayList<>();
        for(Linea l: inicio.getLineas()){
            if(fin.getLineas().contains(l)){
                // Check if the stops are in the correct order on this line
                if (verificarOrdenEnLinea(l, inicio, fin)) {
                    lineasComunes.add(l);
                }
            }
        }
        return lineasComunes;
    }

    /**
     * Verifica si dos paradas están en el orden correcto en una línea
     * @param linea La línea a verificar
     * @param inicio La parada de inicio
     * @param fin La parada de fin
     * @return true si inicio aparece antes que fin en la línea, false en caso contrario
     */
    private static boolean verificarOrdenEnLinea(Linea linea, Parada inicio, Parada fin) {
        List<Parada> paradas = linea.getParadas();
        int indiceInicio = paradas.indexOf(inicio);
        int indiceFin = paradas.indexOf(fin);

        // Ambas paradas deben existir en la línea y inicio debe estar antes que fin
        return indiceInicio >= 0 && indiceFin >= 0 && indiceInicio < indiceFin;
    }

    /**
     * Calcula la próxima hora de salida de un colectivo de una línea desde una parada específica
     * @param linea La línea de colectivo
     * @param parada La parada desde donde sale
     * @param horaLlegada La hora de llegada del usuario a la parada
     * @param diaSemana El día de la semana
     * @param tramos Mapa de tramos para calcular tiempos de viaje
     * @return La hora de salida del próximo colectivo, o horaLlegada si no hay frecuencias
     */
    private static LocalTime calcularProximaSalida(Linea linea, Parada parada, LocalTime horaLlegada, int diaSemana, Map<String, Tramo> tramos) {
        if (linea == null) {
            // Si no hay línea (caminando), la salida es inmediata
            return horaLlegada;
        }

        // Obtener el tiempo de viaje desde el origen de la línea hasta la parada actual
        int tiempoHastaParada = calcularTiempoHastaParada(linea, parada, tramos);

        // Buscar la próxima frecuencia que llegue después de horaLlegada
        LocalTime proximaSalida = null;
        for (Frecuencia f : linea.getFrecuencias()) {
            if (f.getDiaSemana() == diaSemana) {
                LocalTime horaSalidaOrigen = f.getHora();
                LocalTime horaLlegadaParada = horaSalidaOrigen.plusSeconds(tiempoHastaParada);

                if (horaLlegadaParada.isAfter(horaLlegada) || horaLlegadaParada.equals(horaLlegada)) {
                    if (proximaSalida == null || horaLlegadaParada.isBefore(proximaSalida)) {
                        proximaSalida = horaLlegadaParada;
                    }
                }
            }
        }

        return proximaSalida != null ? proximaSalida : horaLlegada;
    }

    /**
     * Calcula el tiempo de viaje desde el inicio de una línea hasta una parada específica
     * @param linea La línea de colectivo
     * @param paradaDestino La parada de destino
     * @param tramos Mapa de tramos
     * @return Tiempo en segundos desde el origen de la línea hasta la parada
     */
    private static int calcularTiempoHastaParada(Linea linea, Parada paradaDestino, Map<String, Tramo> tramos) {
        List<Parada> paradasLinea = linea.getParadas();
        int tiempoTotal = 0;

        // Recorrer las paradas de la línea hasta encontrar la parada destino
        for (int i = 0; i < paradasLinea.size() - 1; i++) {
            Parada paradaActual = paradasLinea.get(i);
            Parada paradaSiguiente = paradasLinea.get(i + 1);

            // Buscar el tramo entre estas dos paradas
            for (Tramo t : tramos.values()) {
                if (t.getInicio().equals(paradaActual) && t.getFin().equals(paradaSiguiente)) {
                    tiempoTotal += t.getTiempo();
                    break;
                }
            }

            // Si llegamos a la parada destino, retornar el tiempo acumulado
            if (paradaSiguiente.equals(paradaDestino)) {
                return tiempoTotal;
            }
        }

        return tiempoTotal;
    }

    private static Linea obtenerLinea(Parada inicio, Parada fin) {
        //La parada de inicio y la de fin comparte una sola linea
        for(Linea l: inicio.getLineas()){
            if(fin.getLineas().contains(l)){
                return l;
            }
        }
        return null;
    }

}