import json
import torch
import torch.nn as nn
import torch.optim as optim
import random
import numpy as np
from collections import deque
from flask import Flask, request, jsonify, session

app = Flask(__name__)

# Define the neural network for the agents
class DQN(nn.Module):
    def __init__(self, input_size, output_size):
        super(DQN, self).__init__()
        self.fc = nn.Sequential(
            nn.Linear(input_size, 64),
            nn.ReLU(),
            nn.Linear(64, output_size)
        )

    def forward(self, x):
        return self.fc(x)

# Initialize agents
betting_agent = DQN(input_size=10, output_size=10)
betting_optimizer = optim.Adam(betting_agent.parameters(), lr=0.001)
betting_memory = deque(maxlen=10000)

card_agent = DQN(input_size=10, output_size=1)
card_optimizer = optim.Adam(card_agent.parameters(), lr=0.001)
card_memory = deque(maxlen=10000)

BATCH_SIZE = 64
GAMMA = 0.99

def select_action(agent, state, n_actions, epsilon=0.1):
    if random.random() < epsilon:
        return random.randrange(n_actions)
    else:
        state = torch.tensor([state], dtype=torch.float)
        with torch.no_grad():
            q_values = agent(state)
        return q_values.argmax().item()

def optimize_model(agent, optimizer, memory):
    if len(memory) < BATCH_SIZE:
        return
    transitions = random.sample(memory, BATCH_SIZE)
    batch_state, batch_action, batch_reward, batch_next_state, batch_done = zip(*transitions)

    batch_state = torch.tensor(batch_state, dtype=torch.float)
    batch_action = torch.tensor(batch_action, dtype=torch.long).unsqueeze(1)
    batch_reward = torch.tensor(batch_reward, dtype=torch.float)
    batch_next_state = torch.tensor(batch_next_state, dtype=torch.float)
    batch_done = torch.tensor(batch_done, dtype=torch.float)

    q_values = agent(batch_state).gather(1, batch_action)
    next_q_values = agent(batch_next_state).max(1)[0].detach()
    expected_q_values = batch_reward + (GAMMA * next_q_values * (1 - batch_done))

    loss = nn.MSELoss()(q_values.squeeze(), expected_q_values)
    optimizer.zero_grad()
    loss.backward()
    optimizer.step()

@app.route('/apostarRondas', methods=['GET'])
def get_rondas_apostadas():
    id = request.args.get('id')
    mano = request.args.get('mano')
    mano = json.loads(mano) if mano else []
    rondasApostadasPorJugadores = request.args.get('rondasApostadasPorJugadores')
    triunfo = request.args.get('triunfo')
    NUM_RONDAS = request.args.get('NUM_RONDAS')

    state = [...] # TODO:
    rondasApostadas = select_action(betting_agent, state, 10)
    session['betting_state'] = state
    session['betting_action'] = rondasApostadas
    return str(rondasApostadas)

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

    state = [...]  # TODO:
    cartaIndex = select_action(card_agent, state, len(state))
    session['card_state'] = state
    session['card_action'] = cartaIndex
    return str(cartaIndex)

@app.route('/resultados', methods=['POST'])
def recibir_resultados():
    global game_counter
    data = request.get_json()
    jugadores = data.get('jugadores', [])
    puntos = jugadores[0].get('puntos')
    reward = puntos

    # Update betting agent
    state = session.get('betting_state')
    action = session.get('betting_action')
    next_state = [...]  # Next state representation
    done = True
    betting_memory.append((state, action, reward, next_state, done))
    betting_loss = optimize_model(betting_agent, betting_optimizer, betting_memory)

    # Update card agent
    state = session.get('card_state')
    action = session.get('card_action')
    next_state = [...]  # Next state representation
    card_memory.append((state, action, reward, next_state, done))
    card_loss = optimize_model(card_agent, card_optimizer, card_memory)

    game_counter += 1

    if game_counter % 10 == 0:
        # Print stats
        print(f"Game: {game_counter}")
        if betting_loss is not None:
            print(f"Betting Agent Loss: {betting_loss:.4f}")
        if card_loss is not None:
            print(f"Card Agent Loss: {card_loss:.4f}")
        # Save models
        torch.save(betting_agent.state_dict(), f"betting_agent_{game_counter}.pth")
        torch.save(card_agent.state_dict(), f"card_agent_{game_counter}.pth")
        print("Models saved.")

    return jsonify({"status": "success"})

if __name__ == '__main__':
    app.run()