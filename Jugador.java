import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Jugador {
    private int id;
    private ArrayList<Carta> mano;
    private int rondasGanadas;
    private int rondasApostadas;
    private int puertoPersonal;

    public Jugador(int id) {
        this.id = id;
        this.mano = new ArrayList<Carta>();
        this.puertoPersonal = Main.PUERTO_BASE + id;
    }

    public int apostarRondas(int rondasApostadasPorJugadores, Carta.Palo triunfo, int NUM_RONDAS) {
        String urlBase = "http://localhost:" + puertoPersonal + "/apostarRondas?";

        try {
            URL url = new URL(urlBase + "rondasApostadasPorJugadores="
                    + rondasApostadasPorJugadores + "&triunfo=" + triunfo + "&NUM_RONDAS=" + NUM_RONDAS + "&id=" + id
                    + "&mano=" + Carta.serializarCartas(mano));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Connection", "close");
            
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }

                rondasApostadas = Integer.parseInt(content.toString());
                return rondasApostadas;
            } finally {
                conn.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al apostar rondas");
        }
    }

    public int getRondasApostadas() {
        return rondasApostadas;
    }

    public int getId() {
        return id;
    }

    public ArrayList<Carta> getMano() {
        return mano;
    }

    public void setMano(ArrayList<Carta> mano) {
        this.mano = mano;
    }

    private Carta seleccionarCarta(ArrayList<Carta> cartasPosibles, ArrayList<Carta> cartasJugadas,
            Carta.Palo triunfo) {
        String urlBase = "http://localhost:" + puertoPersonal + "/seleccionarCarta?";
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlBase + "cartasPosibles="
                    + Carta.serializarCartas(cartasPosibles) + "&cartasJugadas=" + Carta.serializarCartas(cartasJugadas)
                    + "&triunfo=" + triunfo + "&id=" + id + "&rondasGanadas=" + rondasGanadas
                    + "&rondasApostadas=" + rondasApostadas);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Use try-with-resources to ensure streams are properly closed
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }

                int cartaIndex = Integer.parseInt(content.toString());
                return cartasPosibles.get(cartaIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al seleccionar carta");
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public void mandarResultados() {
        String urlBase = "http://localhost:" + puertoPersonal + "/resultados?";

        int rondasGanadas = getRondasGanadas();
        int rondasApostadas = getRondasApostadas();
        int puntos;

        if (rondasGanadas == rondasApostadas) {
            puntos = 10 + rondasGanadas * 5;
        } else {
            puntos = -5 * Math.abs(rondasGanadas - rondasApostadas);
        }

        try {
            URL url = new URL(urlBase + "puntos=" + puntos);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write("".getBytes());
            }

            // Read the response
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    System.err.println("Error al enviar resultados: " + responseCode);
                }
            } finally {
                conn.disconnect();
            }
        } catch (Exception e) {
            System.err.println("Error de conexión: " + e.getMessage());
        }
    }

    public Carta jugarCarta(ArrayList<Carta> cartasJugadas, Carta.Palo triunfo) {
        ArrayList<Carta> cartasPosibles = getCartasPosibles(cartasJugadas, triunfo);
        Carta cartaSeleccionada = seleccionarCarta(cartasPosibles, cartasJugadas, triunfo);

        mano.remove(cartaSeleccionada);
        return cartaSeleccionada;
    }

    private ArrayList<Carta> getCartasPosibles(ArrayList<Carta> cartasJugadas, Carta.Palo triunfo) {
        // Si no hay cartas jugadas, se pueden jugar todas las cartas
        if (cartasJugadas.isEmpty()) {
            return mano;
        }

        Carta.Palo paloManda = cartasJugadas.get(0).getPalo();
        if (paloManda == triunfo) {
            ArrayList<Carta> cartasTriunfo = new ArrayList<Carta>();
            for (Carta carta : mano) {
                if (carta.getPalo() == triunfo) {
                    cartasTriunfo.add(carta);
                }
            }
            // Si no tenemos cartas del triunfo cuando mandan los triunfos, podemos jugar
            // cualquier cosa
            if (cartasTriunfo.isEmpty()) {
                return mano;
            }
            // Si tenemos cartas del triunfo:
            // si tenemos triunfos mas altos que el mayor triunfo jugado, los podemos jugar
            // si no, podemos jugar cualquier triunfo
            Carta maxTriunfoJugado = null;
            for (Carta carta : cartasJugadas) {
                if (carta.getPalo() == triunfo
                        && (maxTriunfoJugado == null || (carta.compareTo(maxTriunfoJugado) > 0))) {
                    maxTriunfoJugado = carta;
                }
            }

            ArrayList<Carta> triunfosMasAltos = new ArrayList<Carta>();
            for (Carta carta : cartasTriunfo) {
                if (maxTriunfoJugado == null || (carta.compareTo(maxTriunfoJugado) > 0)) {
                    triunfosMasAltos.add(carta);
                }
            }

            if (triunfosMasAltos.isEmpty()) {
                return cartasTriunfo;
            } else {
                return triunfosMasAltos;
            }
        } else { // palo que manda no es el de triunfos
            ArrayList<Carta> cartasPaloManda = new ArrayList<Carta>();
            for (Carta carta : mano) {
                if (carta.getPalo() == paloManda) {
                    cartasPaloManda.add(carta);
                }
            }
            // Si tenemos cartas del palo que manda:
            if (!cartasPaloManda.isEmpty()) {
                boolean seJugaronTriunfos = getSeJugaronTriunfos(cartasJugadas, triunfo);
                if (seJugaronTriunfos) {
                    // Si se jugaron triunfos (ya se ha fallado), debemos jugar cartas del palo que
                    // manda
                    return cartasPaloManda;
                } else {
                    // Si no se jugaron triunfos, debemos ver si tenemos alguna carta mayor a la más
                    // alta jugada del palo que manda
                    Carta maxPaloMandaJugado = null;
                    for (Carta carta : cartasJugadas) {
                        if (carta.getPalo() == paloManda
                                && (maxPaloMandaJugado == null || (carta.compareTo(maxPaloMandaJugado) > 0))) {
                            maxPaloMandaJugado = carta;
                        }
                    }
                    ArrayList<Carta> cartasMasAltas = new ArrayList<Carta>();
                    for (Carta carta : cartasPaloManda) {
                        if (maxPaloMandaJugado == null || (carta.compareTo(maxPaloMandaJugado) > 0)) {
                            cartasMasAltas.add(carta);
                        }
                    }

                    // si no tenemos ninguna mas alta podenos jugar cualquier carta del palo que
                    // manda
                    if (cartasMasAltas.isEmpty()) {
                        return cartasPaloManda;
                    } else {
                        return cartasMasAltas;
                    }
                }
            } else { // Si no tenemos cartas del palo que manda:
                // Si se jugaron triunfos, debemos jugar triunfos si tenemos
                boolean seJugaronTriunfos = getSeJugaronTriunfos(cartasJugadas, triunfo);
                if (seJugaronTriunfos) {
                    ArrayList<Carta> cartasTriunfo = new ArrayList<Carta>();
                    for (Carta carta : mano) {
                        if (carta.getPalo() == triunfo) {
                            cartasTriunfo.add(carta);
                        }
                    }

                    Carta maxTriunfoJugado = null;
                    for (Carta carta : cartasJugadas) {
                        if (carta.getPalo() == triunfo &&
                                (maxTriunfoJugado == null || carta.compareTo(maxTriunfoJugado) > 0)) {
                            maxTriunfoJugado = carta;
                        }
                    }

                    ArrayList<Carta> triunfosMasAltos = new ArrayList<Carta>();
                    for (Carta carta : cartasTriunfo) {
                        if (maxTriunfoJugado == null || carta.compareTo(maxTriunfoJugado) > 0) {
                            triunfosMasAltos.add(carta);
                        }
                    }

                    // si no tenemoss triunfo mas altos, podemos jugar todas
                    if (triunfosMasAltos.isEmpty()) {
                        return mano;
                    } else { // si tenemos triunfos mas altos, los podemos jugar
                        return triunfosMasAltos;
                    }
                } else {
                    // Si no se jugaron triunfos, podemos jugar cualquier carta
                    ArrayList<Carta> cartasTriunfo = new ArrayList<Carta>();
                    for (Carta carta : mano) {
                        if (carta.getPalo() == triunfo) {
                            cartasTriunfo.add(carta);
                        }
                    }

                    // si no tenemos triunfos, podemos jugar cualquier carta
                    if (cartasTriunfo.isEmpty()) {
                        return mano;
                    } else { // si tenemos triunfos, los podemos jugar
                        return cartasTriunfo;
                    }
                }
            }
        }
    }

    private boolean getSeJugaronTriunfos(ArrayList<Carta> cartasJugadas, Carta.Palo triunfo) {
        boolean seJugaronTriunfos = false;
        for (Carta carta : cartasJugadas) {
            if (carta.getPalo() == triunfo) {
                seJugaronTriunfos = true;
                break;
            }
        }
        return seJugaronTriunfos;
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
