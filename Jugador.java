import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Jugador {
	protected int id;
	protected ArrayList<Carta> mano;
	protected ArrayList<Carta> manoInicial;
	protected int rondasGanadas;
	protected int rondasApostadas;
	protected Map<Carta, Informacion> cartasVistas = new HashMap<Carta, Jugador.Informacion>();

	enum Informacion {
		JUGADA, TENGO_YO, NO_SE
	}

	public Jugador(int id) {
		this.id = id;
		this.mano = new ArrayList<Carta>();
		this.manoInicial = new ArrayList<Carta>();

		for (Carta.Palo palo : Carta.Palo.values()) {
			for (Carta.Valor valor : Carta.Valor.values()) { // Assuming the card values range from 1 to 12
				Carta carta = new Carta(palo, valor);
				cartasVistas.put(carta, Informacion.NO_SE);
			}
		}
	}

	public int apostarRondas(int rondasApostadasPorJugadores, Carta.Palo triunfo, int NUM_RONDAS,
			boolean eresElUltimoEnApostar) {
		int indiceAccion = 0;
		try { // funcionamiento normal, casos que se han entrenado
			ArrayList<Carta> manoInicial = getManoInicial();
			short[] key = Partida.manoToKey(manoInicial, triunfo);
			float[] oldValues = Pocha.generador.map.get(new GeneradorRL.ShortArrayKey(key));

			double rand = Math.random();
			double cumulativeProbability = 0.0;
			for (int i = 0; i < oldValues.length; i++) {
				cumulativeProbability += oldValues[i];
				if (rand <= cumulativeProbability) {
					indiceAccion = i;
					break;
				}
			}

		} catch (Exception e) {
			// casos que no se han entrenado
			// formula para casos que dan error
			// muy sencilla ya que es muy raro darse
			int triunfos = 0;
			int demas = 0;
			for (Carta carta : mano) {
				if (carta.getPalo() == triunfo) {
					triunfos++;
				} else {
					demas++;
				}
			}
			indiceAccion = (triunfos * 3 + demas) / 5;
		}

		if ((NUM_RONDAS == indiceAccion + rondasApostadasPorJugadores) && eresElUltimoEnApostar) {
			if (indiceAccion == 0) {
				indiceAccion++;
			} else {
				indiceAccion--;
			}
		}

		rondasApostadas = indiceAccion;

		// Actualizar cartasVistas con las cartas que tenemos
		for (Carta carta : mano) {
			if (cartasVistas.containsKey(carta)) {
				cartasVistas.put(carta, Informacion.TENGO_YO);
			}
		}
		return rondasApostadas;
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

	protected Carta seleccionarCarta(ArrayList<Carta> cartasPosibles, ArrayList<Carta> cartasJugadas,
			Carta.Palo triunfo) {
		// Determine context
		boolean quieroGanar = (rondasApostadas - rondasGanadas) > 0;
		boolean soyPrimero = cartasJugadas.isEmpty();
		boolean soyUltimo = (cartasJugadas.size() == 3);
		Carta cartaAJugar = null;

		// Recopilar la carta más alta por palo en la mano
		ArrayList<Carta> cartasMayores = new ArrayList<>();
		for (Carta.Palo palo : Carta.Palo.values()) {
			Carta cartaMayor = null;
			for (Carta c : mano) {
				if (c.getPalo() == palo && (cartaMayor == null || c.compareTo(cartaMayor) > 0)) {
					cartaMayor = c;
				}
			}
			if (cartaMayor != null) {
				cartasMayores.add(cartaMayor);
			}
		}

		// Determinar si realmente tenemos la más alta de cada palo (no existe en
		// cartasVistas una mayor)
		HashMap<Carta, Boolean> tengoCartaMayor = new HashMap<>();
		for (Carta carta : cartasMayores) {
			boolean esMayor = true;
			for (Map.Entry<Carta, Informacion> entry : cartasVistas.entrySet()) {
				if (entry.getKey().getPalo() == carta.getPalo() && entry.getKey().compareTo(carta) > 0) {
					esMayor = false;
					break;
				}
			}
			tengoCartaMayor.put(carta, esMayor);
		}

		// Estrategia de selección
		if (quieroGanar) {
			// Caso: soy el primero en jugar
			if (soyPrimero) {
				// Buscar alguna de las cartas que tengamos más alta
				for (Map.Entry<Carta, Boolean> entry : tengoCartaMayor.entrySet()) {
					Carta c = entry.getKey();
					boolean esMayor = entry.getValue();
					// Si es más alta de su palo
					if (esMayor) {
						// Preferimos triunfos si los tenemos
						if (c.getPalo() == triunfo) {
							return c; // Jugamos la carta triunfal más alta
						} else {
							// Ver cartas jugadas de ese palo y triunfos
							long jugadasPalo = cartasVistas.entrySet().stream()
									.filter(e -> e.getValue() == Informacion.JUGADA
											&& e.getKey().getPalo() == c.getPalo())
									.count();
							long jugadasTriunfos = cartasVistas.entrySet().stream()
									.filter(e -> e.getValue() == Informacion.JUGADA && e.getKey().getPalo() == triunfo)
									.count();
							if (jugadasPalo > 3 && jugadasTriunfos < 7) {
								// Jugar la más baja de otro palo que no tengamos carta mayor
								for (Map.Entry<Carta, Boolean> entry2 : tengoCartaMayor.entrySet()) {
									if (!entry2.getValue()) {
										Carta.Palo palo = entry2.getKey().getPalo();
										Carta cartaMasBaja = null;
										for (Carta c3 : cartasPosibles) {
											if (c3.getPalo() == palo
													&& (cartaMasBaja == null || c3.compareTo(cartaMasBaja) < 0)) {
												cartaMasBaja = c3;
											}
										}
										if (cartaMasBaja != null) {
											return cartaMasBaja;
										}
									}
								}
							}
						}
					}
				}
			}
			// Caso: soy el último en jugar
			else if (soyUltimo) {
				boolean seHanJugadoTriunfos = getSeJugaronTriunfos(cartasJugadas, triunfo);
				if (seHanJugadoTriunfos) {
					// Buscar el triunfo más alto contrincante
					Carta triunfoMasAlto = null;
					for (Carta c : cartasJugadas) {
						if (c.getPalo() == triunfo && (triunfoMasAlto == null || c.compareTo(triunfoMasAlto) > 0)) {
							triunfoMasAlto = c;
						}
					}
					// Intentar ganarle con un triunfo mayor
					for (Carta c : cartasPosibles) {
						if (c.getPalo() == triunfo && triunfoMasAlto != null && c.compareTo(triunfoMasAlto) > 0) {
							return c;
						}
					}
					// Jugar la más baja de algún palo que no tengamos ganador
					for (Map.Entry<Carta, Boolean> entry2 : tengoCartaMayor.entrySet()) {
						if (!entry2.getValue()) {
							Carta.Palo palo = entry2.getKey().getPalo();
							Carta cartaMasBaja = null;
							for (Carta c3 : cartasPosibles) {
								if (c3.getPalo() == palo && (cartaMasBaja == null || c3.compareTo(cartaMasBaja) < 0)) {
									cartaMasBaja = c3;
								}
							}
							if (cartaMasBaja != null) {
								return cartaMasBaja;
							}
						}
					}
					// Fallback: la más baja de todas
					Carta cartaMasBaja = null;
					for (Carta c3 : cartasPosibles) {
						if (cartaMasBaja == null || c3.compareTo(cartaMasBaja) < 0) {
							cartaMasBaja = c3;
						}
					}
					if (cartaMasBaja != null) {
						return cartaMasBaja;
					}
				}
			}
			// Caso: posición intermedia
			if (!cartasMayores.isEmpty() && cartasPosibles.contains(cartasMayores.get(0))) {
				return cartasMayores.get(0);
			}
			Carta minima = cartasPosibles.stream().min(Carta::compareTo).orElse(null);
			if (minima != null) {
				return minima;
			}
		} else { // No quiero ganar
			if (soyPrimero) {
				// Jugar la más alta de un palo que no controlemos
				for (Map.Entry<Carta, Boolean> entry2 : tengoCartaMayor.entrySet()) {
					if (!entry2.getValue()) {
						Carta.Palo palo = entry2.getKey().getPalo();
						Carta cartaMasAlta = null;
						for (Carta c3 : cartasPosibles) {
							if (c3.getPalo() == palo && (cartaMasAlta == null || c3.compareTo(cartaMasAlta) > 0)) {
								cartaMasAlta = c3;
							}
						}
						if (cartaMasAlta != null) {
							return cartaMasAlta;
						} else {
							// Si no tenemos cartas de ese palo, jugar la más baja posible
							return cartasPosibles.stream()
									.min(Carta::compareTo)
									.orElseThrow(() -> new RuntimeException("No deberia pasar"));
						}
					}
					return cartasPosibles.stream()
							.min(Carta::compareTo)
							.orElseThrow(() -> new RuntimeException("No deberia pasar"));
				}
			} else {
				// Ver si se jugaron triunfos
				boolean seJugaronTriunfos = getSeJugaronTriunfos(cartasJugadas, triunfo);
				if (seJugaronTriunfos) {
					// Buscar el triunfo más alto
					Carta triunfoMasAlto = null;
					for (Carta c : cartasJugadas) {
						if (c.getPalo() == triunfo && (triunfoMasAlto == null || c.compareTo(triunfoMasAlto) > 0)) {
							triunfoMasAlto = c;
						}
					}
					// Si podemos superarlo, jugamos otra cosa
					for (Carta c : cartasPosibles) {
						if (c.getPalo() == triunfo && triunfoMasAlto != null && c.compareTo(triunfoMasAlto) > 0) {
							Carta cartaMasBaja = null;
							for (Carta c3 : cartasPosibles) {
								if (cartaMasBaja == null || c3.compareTo(cartaMasBaja) < 0) {
									cartaMasBaja = c3;
								}
							}
							if (cartaMasBaja != null) {
								return cartaMasBaja;
							}
						}
					}
					// Si no podemos superarlo, jugar la más alta posible
					if (cartasMayores.isEmpty()) {
						return cartasPosibles.stream()
								.max(Carta::compareTo)
								.orElseThrow(() -> new RuntimeException("No deberia pasar"));
					} else {
						return cartasMayores.get(0);
					}
				} else {
					// Si no se jugaron triunfos, jugar la más baja posible (seguramente no ganemos)
					return cartasPosibles.stream()
							.min(Carta::compareTo)
							.orElseThrow(() -> new RuntimeException("No deberia pasar"));
				}
			}
		}

		// Fallback: si no hemos seleccionado nada, usar la primera carta posible
		if (cartaAJugar == null || !cartasPosibles.contains(cartaAJugar)) {
			System.out.println("NO DEBERIA PASAR");
			return cartasPosibles.get(0);
		}
		System.out.println("NO DEBERIA PASAR");
		return cartasPosibles.get(0);
	}

	// llamado al final de cada ronda
	protected void recibirCartasJugadas(ArrayList<Carta> cartasJugadas) {
		for (Carta carta : cartasJugadas) {
			if (cartasVistas.containsKey(carta)) {
				cartasVistas.put(carta, Informacion.JUGADA);
			}
		}
	}

	public int getResultados() {
		int rondasGanadas = getRondasGanadas();
		int rondasApostadas = getRondasApostadas();
		int puntos;

		if (rondasGanadas == rondasApostadas) {
			puntos = 10 + rondasGanadas * 5;
		} else {
			puntos = -5 * Math.abs(rondasGanadas - rondasApostadas);
		}
		return puntos;
	}

	public Carta jugarCarta(ArrayList<Carta> cartasJugadas, Carta.Palo triunfo) {
		ArrayList<Carta> cartasPosibles = getCartasPosibles(cartasJugadas, triunfo);
		Carta cartaSeleccionada = seleccionarCarta(cartasPosibles, cartasJugadas, triunfo);

		mano.remove(cartaSeleccionada);
		return cartaSeleccionada;
	}

	protected ArrayList<Carta> getCartasPosibles(ArrayList<Carta> cartasJugadas, Carta.Palo triunfo) {
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

	protected boolean getSeJugaronTriunfos(ArrayList<Carta> cartasJugadas, Carta.Palo triunfo) {
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
		manoInicial.add(carta);
	}

	public void ganoRonda() {
		rondasGanadas++;
	}

	public int getRondasGanadas() {
		return rondasGanadas;
	}

	public ArrayList<Carta> getManoInicial() {
		return manoInicial;
	}
}
