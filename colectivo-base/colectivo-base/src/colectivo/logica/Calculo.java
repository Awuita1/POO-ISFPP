package colectivo.logica;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import colectivo.aplicacion.Constantes;
import colectivo.modelo.Frecuencia;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Recorrido;
import colectivo.modelo.Tramo;

public class Calculo {

	public static List<List<Recorrido>> calcularRecorrido(Parada paradaOrigen, Parada paradaDestino, int diaSemana,
			LocalTime horaLlegaParada, Map<String, Tramo> tramos) {

		List<List<Recorrido>> resultados = new ArrayList<>();
		
		// Buscar rutas directas (sin transbordo)
		List<List<Recorrido>> rutasDirectas = buscarRutasDirectas(paradaOrigen, paradaDestino, diaSemana, horaLlegaParada, tramos);
		
		// Si hay rutas directas, retornarlas. Si no, buscar rutas con transbordo
		if (!rutasDirectas.isEmpty()) {
			resultados.addAll(rutasDirectas);
		} else {
			// Buscar rutas con un transbordo
			List<List<Recorrido>> rutasConTransbordo = buscarRutasConTransbordo(paradaOrigen, paradaDestino, diaSemana, horaLlegaParada, tramos);
			resultados.addAll(rutasConTransbordo);
		}
		
		return resultados;
	}

	private static List<List<Recorrido>> buscarRutasDirectas(Parada paradaOrigen, Parada paradaDestino, 
			int diaSemana, LocalTime horaLlegaParada, Map<String, Tramo> tramos) {
		
		List<List<Recorrido>> rutas = new ArrayList<>();
		
		// Encontrar líneas comunes
		Set<Linea> lineasOrigen = new HashSet<>(paradaOrigen.getLineas());
		Set<Linea> lineasDestino = new HashSet<>(paradaDestino.getLineas());
		lineasOrigen.retainAll(lineasDestino);
		
		// Para cada línea común
		for (Linea linea : lineasOrigen) {
			List<Parada> paradasLinea = linea.getParadas();
			int indexOrigen = paradasLinea.indexOf(paradaOrigen);
			int indexDestino = paradasLinea.indexOf(paradaDestino);
			
			// Verificar que el destino está después del origen en la ruta
			if (indexOrigen < indexDestino) {
				// Calcular el recorrido
				List<Parada> paradasRecorrido = paradasLinea.subList(indexOrigen, indexDestino + 1);
				
				// Calcular duración total del recorrido de origen a destino
				int duracion = calcularDuracion(paradasRecorrido, tramos);
				
				// Calcular el tiempo desde el inicio de la línea hasta el origen
				List<Parada> paradasHastaOrigen = paradasLinea.subList(0, indexOrigen + 1);
				int tiempoHastaOrigen = calcularDuracion(paradasHastaOrigen, tramos);
				
				// Encontrar la próxima frecuencia disponible (desde el inicio de la línea)
				LocalTime horaInicioLinea = encontrarProximaFrecuenciaParaLlegada(linea, diaSemana, horaLlegaParada, tiempoHastaOrigen);
				
				if (horaInicioLinea != null) {
					// La hora de salida desde el origen es la hora de inicio + tiempo hasta origen
					LocalTime horaSalidaOrigen = horaInicioLinea.plusSeconds(tiempoHastaOrigen);
					
					Recorrido recorrido = new Recorrido(linea, new ArrayList<>(paradasRecorrido), horaSalidaOrigen, duracion);
					List<Recorrido> ruta = new ArrayList<>();
					ruta.add(recorrido);
					rutas.add(ruta);
				}
			}
		}
		
		return rutas;
	}

	private static List<List<Recorrido>> buscarRutasConTransbordo(Parada paradaOrigen, Parada paradaDestino, 
			int diaSemana, LocalTime horaLlegaParada, Map<String, Tramo> tramos) {
		
		List<List<Recorrido>> rutas = new ArrayList<>();
		
		// Buscar paradas intermedias (transbordo regular)
		Set<Parada> paradasIntermediasRegulares = encontrarParadasIntermedias(paradaOrigen, paradaDestino);
		
		for (Parada paradaIntermedia : paradasIntermediasRegulares) {
			// Buscar ruta de origen a intermedia
			List<List<Recorrido>> rutasOrigen = buscarRutasDirectas(paradaOrigen, paradaIntermedia, diaSemana, horaLlegaParada, tramos);
			
			for (List<Recorrido> rutaOrigen : rutasOrigen) {
				Recorrido primerRecorrido = rutaOrigen.get(0);
				LocalTime horaLlegadaIntermedia = primerRecorrido.getHoraSalida().plusSeconds(primerRecorrido.getDuracion());
				
				// Buscar ruta de intermedia a destino
				List<List<Recorrido>> rutasDestino = buscarRutasDirectas(paradaIntermedia, paradaDestino, diaSemana, horaLlegadaIntermedia, tramos);
				
				for (List<Recorrido> rutaDestino : rutasDestino) {
					List<Recorrido> rutaCompleta = new ArrayList<>();
					rutaCompleta.addAll(rutaOrigen);
					rutaCompleta.addAll(rutaDestino);
					rutas.add(rutaCompleta);
				}
			}
		}
		
		// Buscar paradas intermedias caminando
		List<Parada> paradasCaminando = paradaOrigen.getParadaCaminando();
		for (Parada paradaCaminando : paradasCaminando) {
			// Ruta caminando
			Tramo tramoCaminando = encontrarTramo(paradaOrigen, paradaCaminando, tramos);
			if (tramoCaminando != null && tramoCaminando.getTipo() == Constantes.CAMINANDO) {
				// Buscar ruta de origen a parada caminando y luego a destino
				List<List<Recorrido>> rutasConCaminata = buscarRutasConCaminata(
					paradaOrigen, paradaCaminando, paradaDestino, diaSemana, horaLlegaParada, tramos, tramoCaminando);
				rutas.addAll(rutasConCaminata);
			}
		}
		
		return rutas;
	}

	private static List<List<Recorrido>> buscarRutasConCaminata(Parada paradaOrigen, Parada paradaCaminando, 
			Parada paradaDestino, int diaSemana, LocalTime horaLlegaParada, 
			Map<String, Tramo> tramos, Tramo tramoCaminando) {
		
		List<List<Recorrido>> rutas = new ArrayList<>();
		
		// Primero buscar de origen a una parada intermedia
		Set<Parada> paradasIntermedias = encontrarParadasIntermedias(paradaOrigen, paradaCaminando);
		
		for (Parada paradaIntermedia : paradasIntermedias) {
			// Ruta de origen a intermedia
			List<List<Recorrido>> rutasOrigen = buscarRutasDirectas(paradaOrigen, paradaIntermedia, diaSemana, horaLlegaParada, tramos);
			
			for (List<Recorrido> rutaOrigen : rutasOrigen) {
				Recorrido primerRecorrido = rutaOrigen.get(0);
				LocalTime horaLlegadaIntermedia = primerRecorrido.getHoraSalida().plusSeconds(primerRecorrido.getDuracion());
				
				// Crear recorrido caminando
				List<Parada> paradasCaminata = new ArrayList<>();
				paradasCaminata.add(paradaIntermedia);
				paradasCaminata.add(paradaCaminando);
				Recorrido recorridoCaminando = new Recorrido(null, paradasCaminata, horaLlegadaIntermedia, tramoCaminando.getTiempo());
				
				LocalTime horaLlegadaCaminando = horaLlegadaIntermedia.plusSeconds(tramoCaminando.getTiempo());
				
				// Ruta de parada caminando a destino
				List<List<Recorrido>> rutasDestino = buscarRutasDirectas(paradaCaminando, paradaDestino, diaSemana, horaLlegadaCaminando, tramos);
				
				for (List<Recorrido> rutaDestino : rutasDestino) {
					List<Recorrido> rutaCompleta = new ArrayList<>();
					rutaCompleta.addAll(rutaOrigen);
					rutaCompleta.add(recorridoCaminando);
					rutaCompleta.addAll(rutaDestino);
					rutas.add(rutaCompleta);
				}
			}
		}
		
		return rutas;
	}

	private static Set<Parada> encontrarParadasIntermedias(Parada paradaOrigen, Parada paradaDestino) {
		Set<Parada> intermedias = new HashSet<>();
		
		// Paradas que comparten línea con origen
		for (Linea lineaOrigen : paradaOrigen.getLineas()) {
			List<Parada> paradasLineaOrigen = lineaOrigen.getParadas();
			int indexOrigen = paradasLineaOrigen.indexOf(paradaOrigen);
			
			// Paradas después del origen en esta línea
			for (int i = indexOrigen + 1; i < paradasLineaOrigen.size(); i++) {
				Parada candidata = paradasLineaOrigen.get(i);
				
				// Verificar si esta parada comparte línea con destino (pero diferente a lineaOrigen)
				for (Linea lineaDestino : paradaDestino.getLineas()) {
					if (!lineaDestino.equals(lineaOrigen) && candidata.getLineas().contains(lineaDestino)) {
						List<Parada> paradasLineaDestino = lineaDestino.getParadas();
						int indexCandidataDestino = paradasLineaDestino.indexOf(candidata);
						int indexDestino = paradasLineaDestino.indexOf(paradaDestino);
						
						// Verificar que el destino está después de la candidata
						if (indexCandidataDestino >= 0 && indexCandidataDestino < indexDestino) {
							intermedias.add(candidata);
						}
					}
				}
			}
		}
		
		return intermedias;
	}

	private static int calcularDuracion(List<Parada> paradas, Map<String, Tramo> tramos) {
		int duracion = 0;
		
		for (int i = 0; i < paradas.size() - 1; i++) {
			Parada inicio = paradas.get(i);
			Parada fin = paradas.get(i + 1);
			Tramo tramo = encontrarTramo(inicio, fin, tramos);
			if (tramo != null) {
				duracion += tramo.getTiempo();
			}
		}
		
		return duracion;
	}

	private static Tramo encontrarTramo(Parada inicio, Parada fin, Map<String, Tramo> tramos) {
		String key = inicio.getCodigo() + ";" + fin.getCodigo();
		return tramos.get(key);
	}

	private static LocalTime encontrarProximaFrecuencia(Linea linea, int diaSemana, LocalTime horaLlegada) {
		LocalTime proximaHora = null;
		
		for (Frecuencia frecuencia : linea.getFrecuencias()) {
			if (frecuencia.getDiaSemana() == diaSemana) {
				LocalTime horaFrecuencia = frecuencia.getHora();
				if (horaFrecuencia.isAfter(horaLlegada) || horaFrecuencia.equals(horaLlegada)) {
					if (proximaHora == null || horaFrecuencia.isBefore(proximaHora)) {
						proximaHora = horaFrecuencia;
					}
				}
			}
		}
		
		return proximaHora;
	}

	private static LocalTime encontrarProximaFrecuenciaParaLlegada(Linea linea, int diaSemana, LocalTime horaLlegadaAParada, int tiempoHastaParada) {
		LocalTime proximaHoraInicio = null;
		
		for (Frecuencia frecuencia : linea.getFrecuencias()) {
			if (frecuencia.getDiaSemana() == diaSemana) {
				LocalTime horaInicio = frecuencia.getHora();
				LocalTime horaLlegada = horaInicio.plusSeconds(tiempoHastaParada);
				
				// Verificar si este bus llega a la parada después de la hora requerida
				if (horaLlegada.isAfter(horaLlegadaAParada) || horaLlegada.equals(horaLlegadaAParada)) {
					if (proximaHoraInicio == null || horaInicio.isBefore(proximaHoraInicio)) {
						proximaHoraInicio = horaInicio;
					}
				}
			}
		}
		
		return proximaHoraInicio;
	}

}
