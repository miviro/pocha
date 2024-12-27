import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

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
    private Random random = new Random();

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
        for (int i = 0; i < jugadores.size(); i++) {
            rondasApostadasPorJugadores += jugadores.get(i).apostarRondas(rondasApostadasPorJugadores, triunfo,
                    NUM_RONDAS, i == jugadores.size() - 1);
        }
        // confiamos en que los jugadores respeten las condiciones de numapostadas !=
        // numrondas,
        // pero comprobadmos por si acaso y lanzamos excepción si no se cumple
        if (rondasApostadasPorJugadores == NUM_RONDAS)

        {
            throw new IllegalStateException("Las rondas apostadas no pueden ser iguales a las rondas totales.");
        }
    }

    private int jugarRonda(int ganadorRondaAnterior) {
        ArrayList<Carta> cartasJugadas = new ArrayList<>();
        // Start with the player who won the previous round
        for (int i = 0; i < jugadores.size(); i++) {
            int jugadorIndex = (ganadorRondaAnterior + i) % jugadores.size();
            Carta carta = jugadores.get(jugadorIndex).jugarCarta(cartasJugadas, triunfo);
            if (i == 0) {
                // La primera carta jugada determina el palo que manda
                manda = carta.getPalo();
            }
            cartasJugadas.add(carta);
            // System.out.println("\t" + "Jugador " + jugadores.get(jugadorIndex).getId() +
            // " juega " + cartasJugadas.get(i));
        }

        mandarCartasJugadasAJugadores(cartasJugadas);
        int indiceGanador = resolverRonda(cartasJugadas);

        // System.out.println("\t\tEl ganador de la ronda es " + "Jugador " +
        // jugadores.get(indiceGanador).getId());
        jugadores.get(indiceGanador).ganoRonda();
        return (ganadorRondaAnterior + indiceGanador) % jugadores.size();
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
            jugadores.add(new Jugador(i));
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
            System.out.println("Jugador " + jugador.getId() + ": " + jugador.getMano());
            System.out.println("\tRondas ganadas: " + jugador.getRondasGanadas());
            System.out.println("\tRondas apostadas: " + jugador.getRondasApostadas());
        }
    }

    public void jugarPartida() {
        // TODO: que no sea aleatorio, que sea segun se reparten las cartas
        int ganadorRondaAnterior = random.nextInt(4); // primero aleatorio
        for (int i = 0; i < NUM_RONDAS; i++) {
            // imprimirEstado();
            ganadorRondaAnterior = jugarRonda(ganadorRondaAnterior);
            // imprimirResultados();
        }

        // cada 1000 partidas, imprimir resultados y guardar en CSV
        if (Pocha.currentPartida % 1000 == 0) {
            imprimirResultados();
            GeneradorRL.guardarCSV(Pocha.generador);
        }

        // TODO: comentar cuando se implemente la app
        for (Jugador jugador : jugadores) {
            ArrayList<Carta> manoInicial = jugador.getManoInicial();
            short[] key = Partida.manoToKey(manoInicial, triunfo);
            float[] oldValues = Pocha.generador.map.get(new GeneradorRL.ShortArrayKey(key));
            if (oldValues == null) {
                // init oldValues to default probabilities
                oldValues = new float[] { 0.09f, 0.09f, 0.09f, 0.09f, 0.09f, 0.09f, 0.09f, 0.09f, 0.09f, 0.09f, 0.09f };
            }
            int rondasGanadas = jugador.getRondasGanadas();

            // actualizar map
            // Update the Q-values using the Bellman equation
            for (int i = 0; i < oldValues.length; i++) {
                if (i == rondasGanadas) {
                    oldValues[i] = oldValues[i] + Pocha.learning_rate
                            * (1 - oldValues[i]);
                } else {
                    oldValues[i] = (1 - Pocha.learning_rate) * oldValues[i];
                }
            }

            // Update the map with the new Q-values
            Pocha.generador.map.put(new GeneradorRL.ShortArrayKey(key), oldValues);
        }
    }

    private void mandarCartasJugadasAJugadores(ArrayList<Carta> mandarCartasJugadasAJugadores) {
        for (Jugador jugador : jugadores) {
            jugador.recibirCartasJugadas(mandarCartasJugadasAJugadores);
        }
    }

    public static short[] manoToKey(ArrayList<Carta> mano, Carta.Palo triunfo) {
        short[] key = new short[10];
        for (Carta carta : mano) {
            Carta.Valor valor = carta.getValor();
            Carta.Palo palo = carta.getPalo();

            int indiceBase = triunfo == palo ? 0 : 5;

            switch (valor) {
                case AS:
                    key[indiceBase]++;
                    break;

                case TRES:
                    key[indiceBase + 1]++;
                    break;

                case REY:
                case CABALLO:
                    key[indiceBase + 2]++;
                    break;

                case SOTA:
                case SIETE:
                    key[indiceBase + 3]++;
                    break;
                case SEIS:
                case CINCO:
                case CUATRO:
                case DOS:
                    key[indiceBase + 4]++;
                    break;
                default:
                    break;
            }
        }
        return key;
    }

    private void imprimirResultados() {
        System.out.println("\nRonda " + Pocha.currentPartida + ":");
        for (Jugador jugador : jugadores) {
            int rondasGanadas = jugador.getRondasGanadas();
            int rondasApostadas = jugador.getRondasApostadas();
            int puntos;

            if (rondasGanadas == rondasApostadas) {
                puntos = 10 + rondasGanadas * 5;
            } else {
                puntos = -5 * Math.abs(rondasGanadas - rondasApostadas);
            }

            System.out.println("\t" + "Jugador " + jugador.getId() + ": " + puntos + " puntos\n\t\t\tRondas ganadas: "
                    + rondasGanadas + "\n\t\t\tRondas apostadas: " + rondasApostadas);
        }
    }
}
