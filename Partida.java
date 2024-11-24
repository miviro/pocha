import java.util.ArrayList;
import java.util.Collections;

public class Partida {
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
    }

    public void jugarRonda() {
        ArrayList<Carta> cartasJugadas = new ArrayList<>();
        for (int i = 0; i < jugadores.size(); i++) {
            if (i == 0) {
                // La primera carta jugada determina el palo que manda
                manda = jugadores.get(i).jugarCarta(cartasJugadas, triunfo).getPalo();
                cartasJugadas.add(jugadores.get(i).jugarCarta(cartasJugadas, triunfo));
            } else {
                cartasJugadas.add(jugadores.get(i).jugarCarta(cartasJugadas, triunfo));
            }
            System.out.println(jugadores.get(i).getNombre() + " juega " + cartasJugadas.get(i));
        }

        Carta ganador = resolverRonda(cartasJugadas.toArray(new Carta[0]));
        for (int i = 0; i < cartasJugadas.size(); i++) {
            if (cartasJugadas.get(i).equals(ganador)) {
                System.out.println("El ganador de la ronda es " + jugadores.get(i).getNombre());
                jugadores.get(i).ganoRonda();
                break;
            }
        }
    }

    // TODO: devolver indice en vez de carta para no tener que volver a buscar quien
    // jugo la carta ganadora
    private Carta resolverRonda(Carta[] cartasJugadas) {
        Carta ganadora = cartasJugadas[0];
        for (int i = 1; i < cartasJugadas.length; i++) {
            ganadora = Carta.pelea(ganadora, cartasJugadas[i], triunfo, manda);
        }
        return ganadora;
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
        }
    }
}
