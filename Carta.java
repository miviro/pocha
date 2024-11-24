public class Carta implements Comparable<Carta> {
    public static enum Palo {
        OROS, COPAS, ESPADAS, BASTOS
    }
    public static enum Valor {
        AS, DOS, TRES, CUATRO, CINCO, SEIS, SIETE, SOTA, CABALLO, REY
    }
    private Palo palo;
    private Valor valor;

    public Carta(Palo palo, Valor valor) {
        this.palo = palo;
        this.valor = valor;
    }

    public Palo getPalo() {
        return palo;
    }
    public Valor getValor() {
        return valor;
    }

    public String toString() {
        return valor + " de " + palo;
    }

    public boolean equals(Object obj) {
        if (obj instanceof Carta) {
            Carta c = (Carta) obj;
            return palo == c.palo && valor == c.valor;
        }
        return false;
    }

    @Override
    public int compareTo(Carta otra) {
        Valor[] orden = {Valor.AS, Valor.TRES, Valor.REY, Valor.CABALLO, Valor.SOTA, Valor.SIETE, Valor.SEIS, Valor.CINCO, Valor.CUATRO, Valor.DOS}; // orden valores, el primero es el que más vale

        int pos1 = -1, pos2 = -1;
        for (int i = 0; i < orden.length; i++) {
            if (orden[i] == this.valor) pos1 = i;
            if (orden[i] == otra.valor) pos2 = i;
        }
        return Integer.compare(pos2, pos1);
    }

    public static Carta pelea(Carta carta1, Carta carta2, Palo triunfo, Palo manda) {
        if (carta1.getPalo() == carta2.getPalo()) {
            return carta1.compareTo(carta2) > 0 ? carta1 : carta2;
        } else if (carta1.getPalo() == triunfo) {
            return carta1;
        } else if (carta2.getPalo() == triunfo) {
            return carta2;
        } else if (carta1.getPalo() == manda) {
            return carta1;
        } else if (carta2.getPalo() == manda) {
            return carta2;
        } else {
            return carta1;
        }
    }
}
