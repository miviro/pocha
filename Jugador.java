import java.util.ArrayList;

public class Jugador {
    private String nombre;
    private ArrayList<Carta> mano;
    private int rondasGanadas;

    public Jugador(String nombre) {
        this.nombre = nombre;
        this.mano = new ArrayList<Carta>();
    }

    public String getNombre() {
        return nombre;
    }

    public ArrayList<Carta> getMano() {
        return mano;
    }

    public void setMano(ArrayList<Carta> mano) {
        this.mano = mano;
    }

    public Carta jugarCarta(ArrayList<Carta> cartasJugadas, Carta.Palo triunfo) {
        ArrayList<Carta> cartasJugables = getCartasJugables(mano, triunfo);

        return mano.remove(0); // TODO: Implementar lógica de juego
    }

    private ArrayList<Carta> getCartasJugables(ArrayList<Carta> cartasJugadas, Carta.Palo triunfo) {
        ArrayList<Carta> cartasJugables = new ArrayList<Carta>();
        // si no hay cartas jugadas (somos el primer jugador), podemos jugar cualquier
        // carta
        if (cartasJugadas.size() == 0) {
            return mano;
        }

        // conseguir palo que manda
        Carta.Palo manda = cartasJugadas.get(0).getPalo();
        // podemos jugar todas mayores que mayor carta que sea del palo que manda
        for (Carta carta : mano) {
            if (carta.getPalo() == manda) {
                cartasJugables.add(carta);
            }
        }

        // si no tenemos cartas del palo que manda
        if (cartasJugables.size() == 0) {
            // añadimos los triunfos
            for (Carta carta : mano) {
                if (carta.getPalo() == triunfo) {
                    cartasJugables.add(carta);
                }
            }
            // si no tenemos triunfos, podemos jugar cualquier carta
            if (cartasJugables.size() == 0) {
                return mano;
            }
        }
        return cartasJugables;
    }

    public void recibirCarta(Carta carta) {
        mano.add(carta);
    }

    public void ganoRonda() {
        rondasGanadas++;
    }

    public int getRondasGanadas() {
        return rondasGanadas;
    }
}
