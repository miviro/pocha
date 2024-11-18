public class Main {
    private static final int numJugadores = 4;

    public static void main(String[] args) {
        Partida partida = new Partida(numJugadores);
        partida.imprimirEstado();
        partida.jugarRonda();
        partida.imprimirEstado();
    }
}