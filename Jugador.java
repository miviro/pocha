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
		// TODO: borrar estados que no se den despues de muchas partidas (muy raros)
		// si nos encontramos un estado de esos jugando, usar una formula sencilla
		// (triunfos * 3 + demas? )
		ArrayList<Carta> manoInicial = getManoInicial();
		short[] key = Partida.manoToKey(manoInicial, triunfo);
		float[] oldValues = Pocha.generador.map.get(key);
		int indiceAccion = 0;

		double rand = Math.random();
		double cumulativeProbability = 0.0;
		for (int i = 0; i < oldValues.length; i++) {
			cumulativeProbability += oldValues[i];
			if (rand <= cumulativeProbability) {
				indiceAccion = i;
				break;
			}
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
		Carta cartaAJugar = null;

		outerloop: for (int index = 0; index < 1; index++) {

			boolean quieroGanar = (rondasApostadas - rondasGanadas) > 0;
			boolean soyPrimero = cartasJugadas.isEmpty();
			boolean soyUltimo = cartasJugadas.size() == 3;

			// tengo la carta mayor de algun palo?
			ArrayList<Carta> cartasMayores = new ArrayList<Carta>();
			for (Carta.Palo palo : Carta.Palo.values()) {
				Carta cartaMayor = null;
				for (Carta carta : mano) {
					if (carta.getPalo() == palo) {
						if (cartaMayor == null || carta.compareTo(cartaMayor) > 0) {
							cartaMayor = carta;
						}
					}
				}
				if (cartaMayor != null) {
					cartasMayores.add(cartaMayor);
				} else {
					// no tenemos cartas de ese palo
				}
			}

			// en cartas mayroes esta la mayor carta que tenemos de cada palo
			HashMap<Carta, Boolean> tengoCartaMayor = new HashMap<Carta, Boolean>();

			for (Carta carta : cartasMayores) {
				// comprobar si hay alguna carta de ese palo mayor a la nuestra en cartas vistas
				boolean tengoLaMayor = true;
				for (Map.Entry<Carta, Informacion> entry : cartasVistas.entrySet()) {
					Carta cartaVista = entry.getKey();
					if (cartaVista.getPalo() == carta.getPalo() && cartaVista.compareTo(carta) > 0) {
						tengoLaMayor = false;
						break outerloop;
					}
				}
				tengoCartaMayor.put(carta, tengoLaMayor);
			}

			if (quieroGanar) {
				if (soyPrimero) {
					for (Map.Entry<Carta, Boolean> entry : tengoCartaMayor.entrySet()) {
						Carta carta = entry.getKey();
						// si tenemos la carta mayor de Carta.palo
						if (entry.getValue()) {
							if (carta.getPalo() == triunfo) {
								cartaAJugar = carta;
								break outerloop;
							} else {
								// conseguir todas las cartas vistas del palo del que tenemos la mas alta
								long jugadasPalo = cartasVistas.entrySet().stream()
										.filter(e -> e.getValue() == Informacion.JUGADA
												&& e.getKey().getPalo() == carta.getPalo())
										.count();
								// conseguir todos los triunfos que hemos visto jugar
								long jugadasTriunfos = cartasVistas.entrySet().stream()
										.filter(e -> e.getValue() == Informacion.JUGADA
												&& e.getKey().getPalo() == triunfo)
										.count();
								// valores arbitrarios de cartas jugadas del palo que tenemos la mas alta y
								// triunfos
								if (jugadasPalo > 3 && jugadasTriunfos < 7) {
									for (Map.Entry<Carta, Boolean> entry2 : tengoCartaMayor.entrySet()) {
										if (!entry2.getValue()) {
											Carta.Palo palo = entry2.getKey().getPalo();

											Carta cartaMasBaja = null;
											for (Carta carta3 : cartasPosibles) {
												if (carta3.getPalo() == palo) {
													if (cartaMasBaja == null || carta3.compareTo(cartaMasBaja) < 0) {
														cartaMasBaja = carta3;
													}
												}
											}
											if (cartaMasBaja != null) {
												cartaAJugar = cartaMasBaja;
												break outerloop;
											} else {
												throw new RuntimeException("No deberia pasar");
											}
										}
									}
								}
							}
						}
					}
				} else if (soyUltimo) {
					// si soy el ultimo y quiero ganar, jugar la carta mas baja que gane
					boolean seHanJugadoTriunfos = getSeJugaronTriunfos(cartasJugadas, triunfo);

					if (seHanJugadoTriunfos) {
						Carta triunfoMasAlto = null;
						for (Carta carta : cartasJugadas) {
							if (carta.getPalo() == triunfo
									&& (triunfoMasAlto == null || carta.compareTo(triunfoMasAlto) > 0)) {
								triunfoMasAlto = carta;
							}
						}

						for (Carta carta : cartasPosibles) {
							if (carta.getPalo() == triunfo) {
								if (carta.compareTo(triunfoMasAlto) > 0) {
									cartaAJugar = carta;
									break outerloop;
								}
							}
						}

						// la mas baja del palo que no tengamos ganador
						for (Map.Entry<Carta, Boolean> entry2 : tengoCartaMayor.entrySet()) {
							if (!entry2.getValue()) {
								Carta.Palo palo = entry2.getKey().getPalo();

								Carta cartaMasBaja = null;
								for (Carta carta3 : cartasPosibles) {
									if (carta3.getPalo() == palo) {
										if (cartaMasBaja == null || carta3.compareTo(cartaMasBaja) < 0) {
											cartaMasBaja = carta3;
										}
									}
								}
								if (cartaMasBaja != null) {
									cartaAJugar = cartaMasBaja;
									break outerloop;

								}
							}
						}

						for (Map.Entry<Carta, Boolean> entry2 : tengoCartaMayor.entrySet()) {
							if (!entry2.getValue()) {
								Carta cartaMasBaja = null;
								for (Carta carta3 : cartasPosibles) {
									if (cartaMasBaja == null || carta3.compareTo(cartaMasBaja) < 0) {
										cartaMasBaja = carta3;
									}
								}
								if (cartaMasBaja != null) {
									cartaAJugar = cartaMasBaja;
									break outerloop;

								} else {
									throw new RuntimeException("No deberia pasar");
								}
							}
						}
					}
				} else { // el resto
					if (!cartasMayores.isEmpty()) {
						if (cartasPosibles.contains(cartasMayores.get(0))) {
							cartaAJugar = cartasMayores.get(0);
							break outerloop;
						}
					}
					// Jugar la carta más baja de cartasPosibles
					cartaAJugar = cartasPosibles.stream().min(Carta::compareTo)
							.orElseThrow(() -> new RuntimeException("No deberia pasar"));
					break outerloop;
				}
			} else { // no quiero ganar
				if (soyPrimero) {
					for (Map.Entry<Carta, Boolean> entry2 : tengoCartaMayor.entrySet()) {
						if (!entry2.getValue()) {
							Carta.Palo palo = entry2.getKey().getPalo();

							Carta cartaMasAlta = null;
							for (Carta carta3 : cartasPosibles) {
								if (carta3.getPalo() == palo) {
									if (cartaMasAlta == null || carta3.compareTo(cartaMasAlta) > 0) {
										cartaMasAlta = carta3;
									}
								}
							}
							if (cartaMasAlta != null) {
								cartaAJugar = cartaMasAlta;
								break outerloop;

							} else {
								throw new RuntimeException("No deberia pasar");
							}
						}
					}
				} else {
					if (getSeJugaronTriunfos(cartasJugadas, triunfo)) {
						Carta triunfoMasAlto = null;
						for (Carta carta : cartasJugadas) {
							if (carta.getPalo() == triunfo
									&& (triunfoMasAlto == null || carta.compareTo(triunfoMasAlto) > 0)) {
								triunfoMasAlto = carta;
							}
						}

						for (Carta carta : cartasPosibles) {
							if (carta.getPalo() == triunfo) {
								if (carta.compareTo(triunfoMasAlto) > 0) {
									Carta cartaMasBaja = null;
									for (Carta carta3 : cartasPosibles) {
										if (cartaMasBaja == null || carta3.compareTo(cartaMasBaja) < 0) {
											cartaMasBaja = carta3;
										}
									}
									if (cartaMasBaja != null) {
										cartaAJugar = cartaMasBaja;
										break outerloop;

									} else {
										throw new RuntimeException("No deberia pasar");
									}
								}
							}
						}

						// si no tenemos triunfos mas altos, jugar la mas alta de cualquier palo
						if (cartasMayores.isEmpty()) {
							cartaAJugar = cartasPosibles.stream().max(Carta::compareTo)
									.orElseThrow(() -> new RuntimeException("No deberia pasar"));
							break outerloop;

						} else {
							cartaAJugar = cartasMayores.get(0);
							break outerloop;

						}
					}
				}
			}
		}

		if (cartaAJugar == null || !cartasPosibles.contains(cartaAJugar)) {
			cartaAJugar = cartasPosibles.get(0);
		}
		return cartaAJugar;
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
