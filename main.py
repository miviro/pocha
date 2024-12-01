import json
from flask import Flask, request, jsonify

app = Flask(__name__)

@app.route('/apostarRondas', methods=['GET'])
def get_rondas_apostadas():
    nombre = request.args.get('nombre')
    mano = request.args.get('mano')
    mano = json.loads(mano) if mano else []
    rondasApostadasPorJugadores = request.args.get('rondasApostadasPorJugadores')
    triunfo = request.args.get('triunfo')
    NUM_RONDAS = request.args.get('NUM_RONDAS')
    
    # Implement your logic here
    rondasApostadas = 1  # Example logic
    
    return str(rondasApostadas)

@app.route('/seleccionarCarta', methods=['GET'])
def seleccionar_carta():
    nombre = request.args.get('nombre')
    cartasPosibles = request.args.get('cartasPosibles')
    cartasPosibles = json.loads(cartasPosibles) if cartasPosibles else []
    cartasJugadas = request.args.get('cartasJugadas')
    cartasJugadas = json.loads(cartasJugadas) if cartasJugadas else []
    triunfo = request.args.get('triunfo')
    
    # Implement your logic here
    cartaIndex = 0  # Example logic
    
    return str(cartaIndex)

@app.route('/resultados', methods=['POST'])
def recibir_resultados():
    data = request.get_json()
    jugadores = data.get('jugadores', [])
    
    # Process the jugadores array containing nombre and puntos
    for jugador in jugadores:
        nombre = jugador.get('nombre')
        puntos = jugador.get('puntos')
        # Process each jugador's data as needed
        # You can add your logic here, like storing in a database
        
    return jsonify({"status": "success"})

if __name__ == '__main__':
    app.run(port=9999)