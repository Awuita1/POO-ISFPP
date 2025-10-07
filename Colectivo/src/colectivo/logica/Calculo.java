package colectivo.logica;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        buscarRecorridos(paradaOrigen, paradaDestino, tramos, recorridoActual, recorridos, horaLlegaParada, diaSemana);
        return recorridos;
    }

    private static void buscarRecorridos(Parada actual, Parada destino, Map<String, Tramo> tramos,
                                         List<Recorrido> recorridoActual, List<List<Recorrido>> recorridos, LocalTime horaLlegaParada, int diaSemana) {
        if (actual.equals(destino)) {
            recorridos.add(new ArrayList<>(recorridoActual));
            return;
        }

        for (Tramo i : tramos.values()) {
            if (i.getInicio().equals(actual) && !recorridoActual.contains(i)) {
                //defino las paradas a agregar
                List<Parada> paradasTramo = new ArrayList<>();
                paradasTramo.add(i.getInicio());
                paradasTramo.add(i.getFin());

                //Defino las lineas que hay en las paradas para el recorrido
                Linea lineaTramo = obtenerLinea(i.getInicio(), i.getFin());

                Recorrido r = new Recorrido(lineaTramo, paradasTramo, horaLlegaParada, i.getTiempo());

                // Agregar recorrido actual, avanzar recursivamente y después quitarlo (backtracking)
                recorridoActual.add(r);
                buscarRecorridos(i.getFin(), destino, tramos, recorridoActual, recorridos, horaLlegaParada, diaSemana);
                recorridoActual.remove(recorridoActual.size() - 1);
            }
        }
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
