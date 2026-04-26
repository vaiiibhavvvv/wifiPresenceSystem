# wifiPresenceSystem

# WiFi Presence System

A real-time WiFi-based presence detection system built using a microservices architecture.
The system ingests RSSI (signal strength) data, processes it to detect user presence, and exposes APIs along with real-time updates for visualization.

---

## Architecture Overview

This project follows a distributed microservices design:

RSSI Collector → Ingestion Service → Kafka → Processing Service → DB/Redis → API Service → Frontend

### Services

#### 1. Ingestion Service

* Accepts RSSI data via REST APIs
* Publishes events to Kafka
* Acts as the entry point of the system

#### 2. Processing Service

* Consumes RSSI events from Kafka
* Applies signal processing logic
* Determines presence status
* Stores data in database and Redis
* Handles expiry and state transitions

#### 3. API Service

* Exposes REST APIs for presence data
* Provides WebSocket for real-time updates
* Acts as a bridge between backend and frontend

#### 4. Frontend (Next.js)

* Displays presence, movement, and RSSI trends
* Uses WebSocket for live updates

#### 5. Collector Script

* Python-based RSSI data generator or simulator

---

## Tech Stack

* Backend: Java (Spring Boot), Microservices
* Messaging: Kafka
* Database: PostgreSQL
* Cache: Redis
* Frontend: Next.js
* Containerization: Docker, Docker Compose
* Scripting: Python

---

## Project Structure

wifiPresenceSystem/

├── ingestion-service/
├── processing-service/
├── api-service/
├── frontend/
├── collector-script/
├── docker-compose.yml
└── init.sql

---

## Setup and Run

### Prerequisites

* Docker and Docker Compose installed
* Git installed

### Run the system

```bash
docker-compose up --build
```

---

## Services and Ports

| Service            | Port |
| ------------------ | ---- |
| Ingestion Service  | 8081 |
| Processing Service | 8082 |
| API Service        | 8083 |
| Frontend           | 3000 |
| Kafka              | 9092 |
| PostgreSQL         | 5432 |
| Redis              | 6379 |

---

## API Example

### Send RSSI Data

```http
POST /ingest
Content-Type: application/json

{
  "deviceId": "device-1",
  "rssi": -45,
  "timestamp": 1710000000
}
```

---

## Data Flow

1. RSSI data is collected via the Python script
2. Sent to the Ingestion Service
3. Published to Kafka topic
4. Processing Service consumes and analyzes data
5. Presence state updated in database and Redis
6. API Service exposes data and pushes updates
7. Frontend visualizes real-time presence

---

## Security Note

* Sensitive configurations are managed via environment variables
* .env files are excluded from version control
* No credentials are hardcoded in the repository

---

## Features

* Real-time presence detection
* Signal smoothing and processing
* Event-driven architecture using Kafka
* Scalable microservices design
* WebSocket-based live updates
* Dockerized deployment

---

## Future Improvements

* Add authentication and authorization
* Deploy to cloud (AWS or GCP)
* Add monitoring using Prometheus and Grafana
* Improve signal accuracy using machine learning models

---

## Author

Vaibhav Singh

---

## Contributing

Feel free to fork the repository and raise pull requests.

---

## License

This project is for educational and demonstration purposes.
