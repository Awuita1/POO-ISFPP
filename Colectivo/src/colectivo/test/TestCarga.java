package colectivo.test;

import colectivo.datos.CargarDatos;
import colectivo.datos.CargarParametros;
import colectivo.modelo.Linea;
import colectivo.modelo.Parada;
import colectivo.modelo.Tramo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalTime;
import java.util.Map;

class TestCarga {
    private Map<Integer, Parada> paradas;
    private Map<String, Linea> lineas;
    private Map<String, Tramo> tramos;

    private int diaSemana;
    private LocalTime horaLlegaParada;

    @BeforeEach
    void setUp() throws Exception {

        try {
            CargarParametros.parametros(); // Carga los parametros de texto
        } catch (IOException e) {
            System.err.print("Error al cargar parametros");
            System.exit(-1);
        }

        paradas = CargarDatos.cargarParadas(CargarParametros.getArchivoParada());

        lineas = CargarDatos.cargarLineas(CargarParametros.getArchivoLinea(), CargarParametros.getArchivoFrecuencia(),
                paradas);

        tramos = CargarDatos.cargarTramos(CargarParametros.getArchivoTramo(), paradas);

        diaSemana = 1; // lunes
        horaLlegaParada = LocalTime.of(10, 35); // hora de llegada a la parada
    }

    @Test
    void printCarga(){
        System.out.println("Paradas cargadas: " + paradas.size());
        System.out.println("Lineas cargadas: " + lineas.size());
        System.out.println("Tramos cargados: " + tramos.size());
    }
}
