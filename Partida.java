import java.util.ArrayList;
import java.util.Collections;

public class Partida {
    // Pensado para solo 4 jugadores, más o menos sobran cartas
    // y no está implementado el reparto de cartas
    public final static int NUM_JUGADORES = 4;
    public final static int NUM_CARTAS = 40;
    public final static int NUM_RONDAS = NUM_CARTAS / NUM_JUGADORES;
    private ArrayList<Jugador> jugadores;
    private ArrayList<Carta> mazo;
    private Carta.Palo triunfo;
    private Carta.Palo manda;

    public Partida(int numJugadores) {
        jugadores = new ArrayList<Jugador>();
        crearJugadores(numJugadores);
        mazo = new ArrayList<Carta>();
        generarMazo();
        barajarMazo();
        repartirCartas();
        apostarRondas();
    }

    private void apostarRondas() {
        int rondasApostadasPorJugadores = 0;
        for (Jugador jugador : jugadores) {
            rondasApostadasPorJugadores += jugador.apostarRondas(rondasApostadasPorJugadores);
        }
    }

    private void jugarRonda() {
        ArrayList<Carta> cartasJugadas = new ArrayList<>();
        for (int i = 0; i < jugadores.size(); i++) {
            Carta carta = jugadores.get(i).jugarCarta(cartasJugadas, triunfo);
            if (i == 0) {
                // La primera carta jugada determina el palo que manda
                manda = carta.getPalo();
            }
            cartasJugadas.add(carta);
            System.out.println("\t" + jugadores.get(i).getNombre() + " juega " + cartasJugadas.get(i));
        }

        int indiceGanador = resolverRonda(cartasJugadas);
        System.out.println("\t\tEl ganador de la ronda es " + jugadores.get(indiceGanador).getNombre());
        jugadores.get(indiceGanador).ganoRonda();
    }

    private int resolverRonda(ArrayList<Carta> cartasJugadas) {
        Carta ganadora = cartasJugadas.get(0);
        int indiceGanador = 0;
        for (int i = 1; i < cartasJugadas.size(); i++) {
            Carta nuevaGanadora = Carta.pelea(ganadora, cartasJugadas.get(i), triunfo, manda);
            if (nuevaGanadora != ganadora) {
                ganadora = nuevaGanadora;
                indiceGanador = i;
            }
        }
        return indiceGanador;
    }

    private void crearJugadores(int numJugadores) {
        for (int i = 0; i < numJugadores; i++) {
            jugadores.add(new Jugador("Jugador " + (i + 1)));
        }
    }

    private void generarMazo() {
        for (Carta.Palo palo : Carta.Palo.values()) {
            for (Carta.Valor valor : Carta.Valor.values()) {
                mazo.add(new Carta(palo, valor));
            }
        }
    }

    private void repartirCartas() {
        int numJugadores = jugadores.size();
        int numCartas = mazo.size();
        for (int i = 0; i < numCartas; i++) {
            jugadores.get(i % numJugadores).recibirCarta(mazo.get(i));
        }
        triunfo = mazo.get(mazo.size() - 1).getPalo();
    }

    private void barajarMazo() {
        Collections.shuffle(mazo);
    }

    public void imprimirEstado() {
        System.out.println("Triunfo: " + triunfo);
        for (Jugador jugador : jugadores) {
            System.out.println(jugador.getNombre() + ": " + jugador.getMano());
            System.out.println("\tRondas ganadas: " + jugador.getRondasGanadas());
            System.out.println("\tRondas apostadas: " + jugador.getRondasApostadas());
        }
    }

    public void jugarPartida() {
        for (int i = 0; i < NUM_RONDAS; i++) {
            System.out.println("Ronda " + (i + 1));
            imprimirEstado();
            jugarRonda();
        }

        System.out.println("Fin de la partida");
        imprimirResultados();
    }

    private void imprimirResultados() {
        System.out.println("\nPuntuación final:");
        for (Jugador jugador : jugadores) {
            int rondasGanadas = jugador.getRondasGanadas();
            int rondasApostadas = jugador.getRondasApostadas();
            int puntos;

            if (rondasGanadas == rondasApostadas) {
                puntos = 10 + rondasGanadas * 5;
            } else {
                puntos = -5 * Math.abs(rondasGanadas - rondasApostadas);
            }

            System.out.println("\t" + jugador.getNombre() + ": " + puntos + " puntos\n\t\t\tRondas ganadas: " + rondasGanadas + "\n\t\t\tRondas apostadas: " + rondasApostadas);
        }
    }
}
