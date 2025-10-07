package colectivo.modelo;

import java.time.LocalTime;

    public  class Frecuencia {

        private int diaSemana;
        private LocalTime hora;

        public Frecuencia(int diaSemana, LocalTime hora) {
            super();
            this.diaSemana = diaSemana;
            this.hora = hora;
        }

        public int getDiaSemana() {
            return diaSemana;
        }

        public void setDiaSemana(int diaSemana) {
            this.diaSemana = diaSemana;
        }

        public LocalTime getHora() {
            return hora;
        }

        public void setHora(LocalTime hora) {
            this.hora = hora;
        }

        @Override
        public String toString() {
        return "Frecuencia [diaSemana=" + diaSemana + ", hora=" + hora + "]";
    }
}