# Modernized Spring Boot Microservices Mesh

A multi-module Spring Boot application written in Java 25 and Spring Boot 4.1+, orchestrated using Docker Compose with Spring Cloud Gateway, Eureka Service Discovery, RabbitMQ messaging, and Jakarta Mail integration.

---

## 🏛 Architecture Overview
```

                    +-----------------------+
                    |  Spring Cloud Gateway |
                    |       (:8085)         |
                    +-----------+-----------+
                                |
        +-----------------------+-----------------------+
        |                       |                       |
        v                       v                       v
+------------------+    +------------------+    +-------------------+
| Accounts Service |    |  Store Service   |    | Appointment Serv. |
|     (:8081)      |    |     (:8082)      |    |      (:8083)      |
+--------+---------+    +------------------+    +-------------------+
|
| (AMQP Events)
v
+------------------+    +-----------------------+
| RabbitMQ Broker  +--->| Notification Service  |
| (:5672 / :15672) |    |        (:8084)        |
+------------------+    +-----------------------+

```

---

## 📦 Project Structure

```text
.
├── pom.xml                       # Maven Multi-Module Parent POM
├── docker-compose.yml            # Container Orchestration Spec
├── build_and_run.sh              # Unified Build & Execution Script
├── .env.example                  # Environment Variable Blueprint
│
├── eureka-server/                # Spring Cloud Netflix Eureka (:8761)
├── gateway-service/              # Spring Cloud Gateway (:8085)
├── accounts-service/             # Accounts & User Management (:8081)
├── store-service/                # Inventory & Store Management (:8082)
├── appointment-service/          # Scheduling & Appointments (:8083)
└── notification-service/         # Async AMQP & Mail Notifications (:8084)

```

---

## 🚀 Tech Stack & Requirements

* **JDK 25** (Eclipse Temurin Base)
* **Spring Boot 4.1+** & **Spring Cloud**
* **Apache Maven 3.9+**
* **Docker & Docker Compose v2+**
* **RabbitMQ 4** (Management Alpine)
* **Jakarta Mail / H2 Database**

---

## ⚙️ Service Ports & Dashboards

| Service | Host Port | Internal Port | URL / Health Endpoint |
| --- | --- | --- | --- |
| **Eureka Registry** | `8761` | `8761` | http://localhost:8761 |
| **API Gateway** | `8085` | `8085` | http://localhost:8085/actuator/health |
| **Accounts Service** | `8081` | `8081` | http://localhost:8081/h2-console |
| **Store Service** | `8082` | `8082` | http://localhost:8082 |
| **Appointment Service** | `8083` | `8083` | http://localhost:8083 |
| **Notification Service** | `8084` | `8084` | http://localhost:8084 |
| **RabbitMQ Dashboard** | `15672` | `15672` | http://localhost:15672 *(guest/guest)* |

---

## 🛠 Local Setup & Running

### 1. Environment Configuration

Create a `.env` file in the project root based on `.env.example`:

```bash
cp .env.example .env

```

Define your SMTP secrets for the Notification Service:

```env
GMAIL_USERNAME=your-email@gmail.com
GMAIL_PASSWORD=your-app-password

```

---

### 2. Quick Start (Build & Run via Script)

Run the unified build script to clean, compile, build Docker runtime layers, and launch the stack:

```bash
chmod +x build_and_run.sh
./build_and_run.sh

```

---

### 3. Manual Build & Execution

**Step 1:** Build all module JARs locally from the multi-module parent root:

```bash
mvn clean package -DskipTests

```

**Step 2:** Start all container services via Docker Compose:

```bash
docker compose up -d --build

```

**Step 3:** Inspect running container state:

```bash
docker compose ps

```

---

## 💻 Accessing H2 Database Consoles

For services using the embedded H2 database (e.g., `accounts-service`), the H2 console can be accessed directly from your host browser:

* **URL:** http://localhost:8081/h2-console
* **JDBC URL:** `jdbc:h2:mem:accountdb`
* **Username:** `sa`
* **Password:** *(blank)*

---

## 📜 Useful Commands & Troubleshooting

```bash
# View aggregated real-time logs across all services
docker compose logs -f

# View logs for a specific service (e.g., notification-service)
docker compose logs -f notification-service

# Restart a single service after making configuration changes
docker compose restart notification-service

# Tear down the stack and remove network bridges
docker compose down -v
```