import java.util.ArrayList;

public class Humano extends Jugador {
	public Humano(int id) {
		super(id);
	}

    @Override
    public int apostarRondas(int rondasApostadasPorJugadores, Carta.Palo triunfo, int NUM_RONDAS, boolean eresElUltimoEnApostar) {
        
        // TODO: (NUM_RONDAS == indiceAccion + rondasApostadasPorJugadores) && eresElUltimoEnApostar
        rondasApostadas = 0;
        return rondasApostadas;
    }

    @Override
    protected Carta seleccionarCarta(ArrayList<Carta> cartasPosibles, ArrayList<Carta> cartasJugadas,
            Carta.Palo triunfo) {

        return cartasPosibles.get(0);
    }
}
