# Traffic Simulation
Locust.io for load testing traffic light controller.

minikube is a tool that runs a single-node Kubernetes cluster locally. It is a lightweight and easy-to-use tool for testing Kubernetes in a local environment.

## Setup Instructions
1.  **Install Locust**:
    ```bash
    pip install locust
    ```

2.  **Start Locust**:
    ```bash
    locust -f locustfile.py
    ```

3.  **Open Web UI**:
    - Go to `http://localhost:8089`
    - Enter the number of users and hatch rate.

## Locustfile
```python
import time
import random
from locust import HttpUser, task, between

class TrafficLightUser(HttpUser):
    wait_time = between(1, 5)

    @task
    def cycle_green_north_south(self):
        # Send a command to change the traffic light state
        payload = {
            "light_id": "intersection_01",
            "state": "NS_GREEN",
            "duration_ms": 10000
        }
        self.client.post("/traffic/command", json=payload, timeout=1.0)

    @task
    def get_status(self):
        # Check the status of all traffic lights
        self.client.get("/traffic/status", timeout=1.0)
```