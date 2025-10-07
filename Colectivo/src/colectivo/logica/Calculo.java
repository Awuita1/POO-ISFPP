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
        buscarRecorridos(paradaOrigen, paradaDestino, tramos, recorridoActual, recorridos);
        return recorridos;
    }

    private static void buscarRecorridos(Parada actual, Parada destino, Map<String, Tramo> tramos,
                                         List<Recorrido> recorridoActual, List<List<Recorrido>> recorridos) {
        if (actual.equals(destino)) {
            recorridos.add(new ArrayList<>(recorridoActual));
            return;
        }

        for (Tramo i : tramos.values()) {
            if (i.getInicio().equals(actual) && !recorridoActual.contains(i)) {
                //defino las paradas a agregar
                List<Parada> paradasTramo = new ArrayList<>();
                paradasTramo.add(i.getInicio().ge);
                paradasTramo.add(i.getFin());

                //Defino las lineas que hay en las paradas para el recorrido
                Linea lineaTramo = ;

            }
        }
    }

}
