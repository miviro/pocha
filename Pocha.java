public class Pocha {
    public static GeneradorRL generador = new GeneradorRL();

    public static final int NUM_PARTIDAS = 10000000;
    public static float learning_rate = 0.1f;

    public static int currentPartida = 0;

    public static void main(String[] args) {
        GeneradorRL.cargarCSV(generador);
        while (true) {
            Partida partida = new Partida(Partida.NUM_JUGADORES);
            partida.jugarPartida();
            currentPartida++;
        }
    }
}