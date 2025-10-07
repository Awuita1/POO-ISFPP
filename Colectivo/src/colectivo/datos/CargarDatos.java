package colectivo.datos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalTime;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;

public class CargarDatos {

	public static Map<Integer, Parada> cargarParadas(String nombreArchivo) throws IOException {
        Map<Integer, Parada> paradas = new TreeMap<>();
        Scanner read = new Scanner(new File(nombreArchivo));
        read.useDelimiter(";");

        while(read.hasNext()) {
            int id = read.nextInt();
            String direccion = read.next();
            double latitud = read.nextDouble();
            double longitud = read.nextDouble();

            Parada parada = new Parada(id, direccion, latitud, longitud);
            paradas.put(id, parada);
        }

		return paradas;
	}

	public static Map<String, Tramo> cargarTramos(String nombreArchivo, Map<Integer, Parada> paradas) throws FileNotFoundException {
        Map<String, Tramo> tramos = new TreeMap<>();
        Scanner read = new Scanner(new File(nombreArchivo));
        read.useDelimiter("\\s*;\\s*");

        while(read.hasNext()) {
            int idParadaOrigen = read.nextInt();
            int idParadaDestino = read.nextInt();
            int tiempo = read.nextInt();
            int tipo = read.nextInt();

            Parada paradaOrigen = paradas.get(idParadaOrigen);
            Parada paradaDestino = paradas.get(idParadaDestino);

            if (paradaOrigen != null && paradaDestino != null) {
                Tramo tramo = new Tramo(paradaOrigen, paradaDestino, tiempo, tipo);
                String key = idParadaOrigen + "-" + idParadaDestino;
                tramos.put(key, tramo);
            }
        }

        return tramos;
	}

	public static Map<String, Linea> cargarLineas(String nombreArchivo, String nombreArchivoFrecuencia, Map<Integer, Parada> paradas) throws FileNotFoundException {
        Map<String, Linea> lineas = new TreeMap<>();
        Scanner read = new Scanner(new File(nombreArchivo));
        read.useDelimiter("\\s*;\\s*");

        while (read.hasNext()) {
            // Paso 1: leer código y nombre
            String codigo = read.next();
            String nombre = read.next();
            Linea linea = new Linea(codigo, nombre);

            // Paso 2: leer todos los IDs de parada hasta fin de línea
            while (read.hasNextInt()) {
                int idParada = read.nextInt();
                Parada parada = paradas.get(idParada);
                if (parada != null) {
                    linea.agregarParada(parada);
                }
            }
            // Cuando termina la línea, el delimitador ya cortó

            lineas.put(codigo, linea);
            // Si hay delimitadores al final, puede ser necesario saltar el resto de la línea
            if (read.hasNextLine()) read.nextLine();
        }
        read.close();

        // Paso 3: leer frecuencias y asignarlas
        Scanner freqRead = new Scanner(new File(nombreArchivoFrecuencia));
        freqRead.useDelimiter("\\s*;\\s*");

        while (freqRead.hasNext()) {
            String freqCodigo = freqRead.next();
            int diaSemana = freqRead.nextInt();
            String horaStr = freqRead.next();

            Linea linea = lineas.get(freqCodigo);
            if (linea != null) {
                LocalTime hora = LocalTime.parse(horaStr); // Formato HH:mm
                linea.agregarFrecuencia(diaSemana, hora);
            }
            if (freqRead.hasNextLine()) freqRead.nextLine();
        }
        freqRead.close();

        return lineas;
    }

}
