import json
from flask import Flask, request, jsonify
from threading import Thread

def create_app():
    return Flask(__name__)

def run_app(app, port):
    app.run(port=port)

# Create Flask instances for each port
app_5000 = create_app()
app_5001 = create_app()
app_5002 = create_app()
app_5003 = create_app()

# Define routes for each app instance
for app in [app_5000, app_5001, app_5002, app_5003]:
    @app.route('/apostarRondas', methods=['GET'])
    def get_rondas_apostadas():
        id = request.args.get('id')
        mano = request.args.get('mano')
        mano = json.loads(mano) if mano else []
        rondasApostadasPorJugadores = request.args.get('rondasApostadasPorJugadores')
        triunfo = request.args.get('triunfo')
        NUM_RONDAS = request.args.get('NUM_RONDAS')
        return str(1)

    @app.route('/seleccionarCarta', methods=['GET'])
    def seleccionar_carta():
        id = request.args.get('id')
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
    # Create threads for each app
    threads = [
        Thread(target=run_app, args=(app_5000, 5000)),
        Thread(target=run_app, args=(app_5001, 5001)),
        Thread(target=run_app, args=(app_5002, 5002)),
        Thread(target=run_app, args=(app_5003, 5003))
    ]
    
    # Start all threads
    for thread in threads:
        thread.start()
    
    # Wait for all threads to complete
    for thread in threads:
        thread.join()
