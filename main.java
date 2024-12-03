public class Main {
    public static GeneradorRL generador = new GeneradorRL();

    public static final int NUM_PARTIDAS = 10;
    public static float learning_rate = 0.1f;
    public static float discount_factor = 0.9f;

    public static float epsilon = 0.1f;
    public static final float max_epsilon = 1.0f;
    public static final float min_epsilon = 0.1f;
    public static final float epsilon_decay = 0.005f;

    public static int currentPartida = 0;

    public static void main(String[] args) {
        GeneradorRL.cargarCSV(generador);
        for (int i = 0; i < NUM_PARTIDAS; i++) {
            System.out.println("Partida " + (i + 1));
            Partida partida = new Partida(Partida.NUM_JUGADORES);
            partida.jugarPartida();
            currentPartida++;
        }
        GeneradorRL.guardarCSV(generador);
    }
}