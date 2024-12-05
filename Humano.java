import java.util.ArrayList;
import java.util.Scanner;

public class Humano extends Jugador {
	public Humano(int id) {
		super(id);
	}

    @Override
    public int apostarRondas(int rondasApostadasPorJugadores, Carta.Palo triunfo, int NUM_RONDAS, boolean eresElUltimoEnApostar) {
        Scanner scanner = new Scanner(System.in);
        int rondasApostadas;
        do {
            System.out.print("Introduce un número entre 0 y 10 distinto a " + (NUM_RONDAS - rondasApostadasPorJugadores) + ": ");
            rondasApostadas = scanner.nextInt();
        } while ((rondasApostadas < 0 || rondasApostadas > 10) && ((NUM_RONDAS == rondasApostadas + rondasApostadasPorJugadores) && eresElUltimoEnApostar));

        scanner.close();
        return rondasApostadas;
    }

    @Override
    protected Carta seleccionarCarta(ArrayList<Carta> cartasPosibles, ArrayList<Carta> cartasJugadas,
            Carta.Palo triunfo) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Cartas posibles:");
        for (int i = 0; i < cartasPosibles.size(); i++) {
            System.out.println(i + ": " + cartasPosibles.get(i));
        }
        System.out.print("Selecciona una carta: ");
        int seleccion = scanner.nextInt();
        scanner.close();
        return cartasPosibles.get(seleccion);
    }
}
