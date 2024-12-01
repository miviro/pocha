import json
import sys
import random
from flask import Flask, request, jsonify
import torch
import torch.nn as nn
import torch.optim as optim
import numpy as np
import os
import logging

class DQN(nn.Module):
    def __init__(self, state_size, action_size):
        super(DQN, self).__init__()
        self.fc1 = nn.Linear(state_size, 24)
        self.relu = nn.ReLU()
        self.fc2 = nn.Linear(24, 24)
        self.fc3 = nn.Linear(24, action_size)
    
    def forward(self, x):
        x = self.relu(self.fc1(x))
        x = self.relu(self.fc2(x))
        x = self.fc3(x)
        return x
class Agent:
    def __init__(self, state_size, action_size, port):
        self.state_size = state_size
        self.action_size = action_size
        self.memory = []
        self.gamma = 0.95
        self.epsilon = 1.0
        self.epsilon_min = 0.01
        self.epsilon_decay = 0.995
        self.model = DQN(state_size, action_size)
        self.optimizer = optim.Adam(self.model.parameters(), lr=0.001)
        self.loss_fn = nn.MSELoss()
        self.port = port
        self.model_filename = f"dqn_model_port_{self.port}.pth"
        self.optimizer_filename = f"dqn_optimizer_port_{self.port}.pth"
        self.save_interval = 1000
        self.replay_count = 0
        self.load()

    def remember(self, state, action, reward, next_state, done):
        self.memory.append( (state, action, reward, next_state, done) )
        if len(self.memory) > 2000:
            self.memory.pop(0)

    def act(self, state, NUM_RONDAS):
        if np.random.rand() <= self.epsilon:
            return np.random.randint(0, NUM_RONDAS+1)
        state = torch.FloatTensor(state).unsqueeze(0)
        with torch.no_grad():
            act_values = self.model(state)
        action = torch.argmax(act_values[0]).item()
        return min(action, NUM_RONDAS)

    def replay(self, batch_size):
        if len(self.memory) < batch_size:
            return  # Not enough samples to replay
        minibatch = random.sample(self.memory, batch_size)
        for state, action, reward, next_state, done in minibatch:
            state = torch.FloatTensor(state).unsqueeze(0)
            next_state = torch.FloatTensor(next_state).unsqueeze(0)
            target = reward
            if not done:
                target = reward + self.gamma * torch.max(self.model(next_state)[0]).item()
            target_f = self.model(state)
            target_f = target_f.clone().detach()
            target_f[0][action] = target
            self.optimizer.zero_grad()
            outputs = self.model(state)
            loss = self.loss_fn(outputs, target_f)
            loss.backward()
            self.optimizer.step()
        if self.epsilon > self.epsilon_min:
            self.epsilon *= self.epsilon_decay

        # Save the model after each replay
        self.replay_count += 1
        if self.replay_count % self.save_interval == 0:
            self.save()
    def save(self):
        try:
            torch.save(self.model.state_dict(), self.model_filename)
            torch.save(self.optimizer.state_dict(), self.optimizer_filename)
            print(f"Model and optimizer saved to {self.model_filename} and {self.optimizer_filename}")
        except Exception as e:
            print(f"Error saving model and optimizer: {e}")
        print(f"Model and optimizer saved to {self.model_filename} and {self.optimizer_filename}")

    def load(self):
        if os.path.exists(self.model_filename):
            self.model.load_state_dict(torch.load(self.model_filename))
            print(f"Loaded model from {self.model_filename}")
        else:
            print(f"No model found at {self.model_filename}, starting fresh.")
        
        if os.path.exists(self.optimizer_filename):
            self.optimizer.load_state_dict(torch.load(self.optimizer_filename))
            print(f"Loaded optimizer from {self.optimizer_filename}")
        else:
            print(f"No optimizer found at {self.optimizer_filename}, starting fresh.")

# Initialize Flask app
app = Flask(__name__)
log = logging.getLogger('werkzeug')
log.setLevel(logging.ERROR)
# Global variables to store the agent and last state-action pair
state_size = 4
action_size = 20  # Adjust based on expected maximum NUM_RONDAS

if len(sys.argv) < 2:
    print("Usage: python app.py <port>")
    sys.exit(1)
port = int(sys.argv[1])

agent = Agent(state_size, action_size, port)
last_state = None
last_action = None

@app.route('/apostarRondas', methods=['GET'])
def get_rondas_apostadas():
    global last_state, last_action

    mano = request.args.get('mano')
    mano = json.loads(mano) if mano else []
    rondasApostadasPorJugadores = request.args.get('rondasApostadasPorJugadores')
    rondasApostadasPorJugadores = json.loads(rondasApostadasPorJugadores) if rondasApostadasPorJugadores else []
    triunfo = request.args.get('triunfo')
    NUM_RONDAS = int(request.args.get('NUM_RONDAS'))

    state = get_state(mano, triunfo, NUM_RONDAS)
    action = agent.act(state, NUM_RONDAS)

    last_state = state
    last_action = action

    # TODO: si NUM_RONDAS == rondasApostadasPorJugadores + nuestra apuesta, reducir o aumentar apuesta en uno?
    if action + rondasApostadasPorJugadores == NUM_RONDAS:
        action = NUM_RONDAS - 1
        if action < 0:
            action = 1

    return str(action)

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
    global last_state, last_action

    puntos = request.args.get('puntos')
    if puntos is None:
        return jsonify({"status": "error", "message": "No 'puntos' provided"}), 400
    puntos = int(puntos)
    reward = puntos

    # Assuming the game ends after this step; if not, adjust accordingly
    next_state = np.zeros(state_size, dtype=np.float32)  # Placeholder for next state
    done = True  # Assuming the episode ends; set appropriately

    if last_state is not None and last_action is not None:
        agent.remember(last_state, last_action, reward, next_state, done)
        agent.replay(32)

    return jsonify({"status": "success"})

def get_state(mano, triunfo, NUM_RONDAS):
    # TODO: mejorar estado
    # Number of cards in hand
    num_cards = len(mano)

    card_values = {
        'AS': 11, 'DOS': 2, 'TRES': 3, 'CUATRO': 4, 'CINCO': 5,
        'SEIS': 6, 'SIETE': 7, 'SOTA': 8, 'CABALLO': 9, 'REY': 10
    }

    sum_trump_values = sum(
        card_values.get(card['valor'].upper(), 0)
        for card in mano if card['palo'] == triunfo
    )

    sum_non_trump_values = sum(
        card_values.get(card['valor'].upper(), 0)
        for card in mano if card['palo'] != triunfo
    )

    # Total number of rounds
    total_rondas = NUM_RONDAS

    # State vector
    state = np.array([num_cards, sum_trump_values, sum_non_trump_values, total_rondas], dtype=np.float32)

    return state


if __name__ == '__main__':
    app.run(port=port)
