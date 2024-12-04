public class Pocha {
    public static GeneradorRL generador = new GeneradorRL();

    public static final int NUM_PARTIDAS = 1000000;
    public static float learning_rate = 0.1f;

    public static int currentPartida = 0;

    public static void main(String[] args) {
        GeneradorRL.cargarCSV(generador);
        for (int i = 0; i < NUM_PARTIDAS; i++) {
            Partida partida = new Partida(Partida.NUM_JUGADORES);
            partida.jugarPartida();
            currentPartida++;
        }
        GeneradorRL.guardarCSV(generador);
    }
}