package colectivo.datos;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;

public class CargarDatos {

	public static Map<Integer, Parada> cargarParadas(String nombreArchivo) throws IOException {
		Map<Integer, Parada> paradas = new HashMap<>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(nombreArchivo))) {
			String linea;
			while ((linea = br.readLine()) != null) {
				String[] partes = linea.split(";");
				if (partes.length >= 4) {
					int codigo = Integer.parseInt(partes[0]);
					String direccion = partes[1];
					double latitud = Double.parseDouble(partes[2]);
					double longitud = Double.parseDouble(partes[3]);
					
					Parada parada = new Parada(codigo, direccion, latitud, longitud);
					paradas.put(codigo, parada);
				}
			}
		}
		
		return paradas;
	}

	public static Map<String, Tramo> cargarTramos(String nombreArchivo, Map<Integer, Parada> paradas)
			throws FileNotFoundException {
		Map<String, Tramo> tramos = new HashMap<>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(nombreArchivo))) {
			String linea;
			while ((linea = br.readLine()) != null) {
				String[] partes = linea.split(";");
				if (partes.length >= 4) {
					int codigoInicio = Integer.parseInt(partes[0]);
					int codigoFin = Integer.parseInt(partes[1]);
					int tiempo = Integer.parseInt(partes[2]);
					int tipo = Integer.parseInt(partes[3]);
					
					Parada inicio = paradas.get(codigoInicio);
					Parada fin = paradas.get(codigoFin);
					
					if (inicio != null && fin != null) {
						Tramo tramo = new Tramo(inicio, fin, tiempo, tipo);
						String key = codigoInicio + ";" + codigoFin;
						tramos.put(key, tramo);
					}
				}
			}
		} catch (IOException e) {
			throw new FileNotFoundException("No se pudo leer el archivo: " + nombreArchivo);
		}
		
		return tramos;
	}

	public static Map<String, Linea> cargarLineas(String nombreArchivo, String nombreArchivoFrecuencia,
			Map<Integer, Parada> paradas) throws FileNotFoundException {
		Map<String, Linea> lineas = new HashMap<>();
		
		// Cargar líneas
		try (BufferedReader br = new BufferedReader(new FileReader(nombreArchivo))) {
			String linea;
			while ((linea = br.readLine()) != null) {
				String[] partes = linea.split(";");
				if (partes.length >= 3) {
					String codigo = partes[0];
					String nombre = partes[1];
					
					Linea lineaObj = new Linea(codigo, nombre);
					
					// Agregar paradas
					for (int i = 2; i < partes.length; i++) {
						if (!partes[i].trim().isEmpty()) {
							int codigoParada = Integer.parseInt(partes[i]);
							Parada parada = paradas.get(codigoParada);
							if (parada != null) {
								lineaObj.agregarParada(parada);
							}
						}
					}
					
					lineas.put(codigo, lineaObj);
				}
			}
		} catch (IOException e) {
			throw new FileNotFoundException("No se pudo leer el archivo: " + nombreArchivo);
		}
		
		// Cargar frecuencias
		try (BufferedReader br = new BufferedReader(new FileReader(nombreArchivoFrecuencia))) {
			String linea;
			while ((linea = br.readLine()) != null) {
				String[] partes = linea.split(";");
				if (partes.length >= 3) {
					String codigoLinea = partes[0];
					int diaSemana = Integer.parseInt(partes[1]);
					LocalTime hora = LocalTime.parse(partes[2]);
					
					Linea lineaObj = lineas.get(codigoLinea);
					if (lineaObj != null) {
						lineaObj.agregarFrecuencia(diaSemana, hora);
					}
				}
			}
		} catch (IOException e) {
			throw new FileNotFoundException("No se pudo leer el archivo: " + nombreArchivoFrecuencia);
		}
		
		return lineas;
	}

}
