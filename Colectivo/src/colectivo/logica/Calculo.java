package colectivo.logica;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import colectivo.modelo.Tramo;

public class Calculo {

    /**
     * Calcula los posibles recorridos entre una parada de origen y una de destino
     * en un dia y hora determinados.
     *
     * @param paradaOrigen       Parada de origen
     * @param paradaDestino      Parada de destino
     * @param diaSemana          Dia de la semana (1 a 7)
     * @param horaLlegaParada    Hora de llegada a la parada de origen
     * @param tramos             Mapa de tramos entre paradas
     * @return Lista de listas de recorridos posibles
     */
	public static List<List<Recorrido>> calcularRecorrido(Parada paradaOrigen, Parada paradaDestino, int diaSemana, LocalTime horaLlegaParada, Map<String, Tramo> tramos) {
        List<List<Recorrido>> recorridos = new ArrayList<>();

        //defino la frecuencia de los colecetivos
        Boolean frecuenciaBaja = true;
        if( 2 <= diaSemana &&  diaSemana <= 6){
            frecuenciaBaja = false;
        }




		return recorridos;
	}

}
