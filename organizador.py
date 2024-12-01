import subprocess
import sys
from concurrent.futures import ThreadPoolExecutor

def run_instance(port):
    subprocess.run([sys.executable, "main.py", str(port)])

def main():
    ports = [5000, 5001, 5002, 5003]
    
    with ThreadPoolExecutor(max_workers=4) as executor:
        executor.map(run_instance, ports)

if __name__ == "__main__":
    main()