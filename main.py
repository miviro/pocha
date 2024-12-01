import json
import sys
from flask import Flask, request, jsonify

# Create Flask instances for each port
app = Flask(__name__)

@app.route('/apostarRondas', methods=['GET'])
def get_rondas_apostadas():
    mano = request.args.get('mano')
    mano = json.loads(mano) if mano else []
    rondasApostadasPorJugadores = request.args.get('rondasApostadasPorJugadores')
    triunfo = request.args.get('triunfo')
    NUM_RONDAS = request.args.get('NUM_RONDAS')
    return str(1)
@app.route('/seleccionarCarta', methods=['GET'])
def seleccionar_carta():
    cartasPosibles = request.args.get('cartasPosibles')
    cartasPosibles = json.loads(cartasPosibles) if cartasPosibles else []
    cartasJugadas = request.args.get('cartasJugadas')
    cartasJugadas = json.loads(cartasJugadas) if cartasJugadas else []
    triunfo = request.args.get('triunfo')
    rondasGanadas = request.args.get('rondasGanadas')
    rondasApostadas = request.args.get('rondasApostadas')
    return str(0)
@app.route('/resultados', methods=['POST'])
def recibir_resultados():
    puntos = request.args.get('puntos')
    return jsonify({"status": "success"})

if __name__ == '__main__':
    app.run(port=int(sys.argv[1]))
