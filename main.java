public class Main {
    public static final int NUM_PARTIDAS = 10;

    public static void main(String[] args) {
        for (int i = 0; i < NUM_PARTIDAS; i++) {
            System.out.println("Partida " + (i + 1));
            Partida partida = new Partida(Partida.NUM_JUGADORES);
            partida.jugarPartida();
        }
    }
}