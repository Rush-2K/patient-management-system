# Patient Management System (Microservices)

> **Learning Project:** This project is a hands-on exploration of high-performance Microservices architecture. It focuses on the transition from traditional REST to **gRPC** and **Event-Driven patterns**, while implementing production-grade security via **JWT** and an **API Gateway**.

---

## 🏗 System Architecture & Flow

This system demonstrates a hybrid communication model, using synchronous gRPC for critical internal logic, asynchronous Kafka for background processing, and a centralized Gateway for security.



### 🔐 Security & Data Flow:
1. **Authentication:** The client sends credentials to the `auth-service`. Upon successful validation, the service issues a **JSON Web Token (JWT)**.
2. **Gateway Entry:** All subsequent requests are sent to the **API Gateway** with the JWT in the `Authorization` header.
3. **Validation & Routing:** The Gateway validates the token with the `auth-service`. If valid, it routes the request to the internal microservice, hiding internal IP addresses from the client.
4. **Internal Sync (gRPC):** The `patient-service` calls the `billing-service` via **gRPC** for high-speed, strictly typed account creation.
5. **Internal Async (Kafka):** The `patient-service` publishes events to the **Kafka** `patient` topic.
6. **Analytics:** The `analytics-service` consumes events for long-term tracking without blocking the main user flow.

---

## 🛠 Tech Stack

* **Backend:** Java / Spring Boot 3.x
* **Security:** Spring Security & **JSON Web Tokens (JWT)**
* **Gateway:** **Spring Cloud Gateway** (Centralized Routing & Security)
* **Communication:**
    * **gRPC & Protobuf:** Low-latency internal service-to-service calls.
    * **Apache Kafka:** Asynchronous event-driven analytics.
* **Database:** PostgreSQL (Dockerized)
* **DevOps:** Docker (Bitnami Kafka image)

---

## 📂 Service Overview

| Service | Protocol | Responsibility |
| :--- | :--- | :--- |
| **`api-gateway`** | HTTP | Single entry point; handles JWT validation, routing, and rate limiting. |
| **`auth-service`** | REST | Manages user credentials, issues JWTs, and validates tokens. |
| **`patient-service`** | REST / gRPC | Primary entry point; saves to Postgres; triggers billing & events. |
| **`billing-service`** | gRPC (Server) | Listens for Protobuf messages to initialize billing accounts. |
| **`analytics-service`**| Kafka Consumer | Processes patient events for data insights and reporting. |

---

### 🐳 Docker Execution Strategy
This project uses individual **Dockerfiles** for each service, managed via IDE Run Configurations.

**For each Microservice:**
1. **Target:** Point to the `Dockerfile` in the service root.
2. **Bind Ports:** - Gateway: `4004:4004`
   - Patient Service: `4000:4000` (Internal)
3. **Network:** Ensure all containers are attached to the same Docker network (e.g., `medical-net`) to allow gRPC and Kafka communication.
4. **Env Variables:** Set `JWT_SECRET`, `DB_URL`, and `KAFKA_BOOTSTRAP_SERVERS` in the Run Configuration.

**For Infrastructure (DB & Kafka):**
- Pull and run the official `postgres:latest` and `bitnami/kafka:latest` images from Docker Hub.
- Ensure the container names match the `application.yml` hostnames (e.g., name the container `patient-service-db`).

---

### 💡 Key Learnings & Implementation Details
* **Centralized Edge Concerns**: Implemented the **API Gateway** pattern to centralize authentication, logging, and rate limiting. This hides internal service addresses and ensures business services like patient-service remain focused solely on domain logic.

* **Stateless Security with JWT**: Mastered the flow of issuing and validating **JSON Web Tokens**. I configured the Gateway to act as a "secure entry point" that validates tokens with the auth-service before routing requests, eliminating the need for session state.

* **gRPC & Protobuf vs. REST & JSON:** Implemented **gRPC** to take advantage of **HTTP/2 multiplexing** and used **Protocol Buffers (Protobuf)** as the binary serialization format. This combination significantly reduces payload size and latency compared to the text-based JSON/HTTP 1.1 approach used in traditional REST
* **Decoupling with Kafka**: Leveraged the **Bitnami Kafka** image to implement an event-driven architecture. This ensures the analytics-service is fully decoupled; even if it goes down, the main patient-service continues to function without latency or interruption.

* **Manual Orchestration & Networking**: Instead of relying on automated scripts, I manually configured **Docker networking** and **Port Bindings** within IntelliJ. This gave me a deep understanding of how Docker DNS resolves service names (e.g., http://patient-service:4000) within a bridge network.

---

## 🚀 DevOps & Running the Project

Each service contains its own **multi-stage Dockerfile** to handle environment setup and dependency installation.

### Building Services
Each service is a Maven project. You can build them individually:
```bash
mvn clean install
```
## 📚 Resources & Credits
* **Tutorial Source:** [Build & Deploy a Production-Ready Patient Management System](https://www.youtube.com/watch?v=tseqdcFfTUY) by **Chris Blakely**.
* **Project Context:** This project was developed as a hands-on exercise to master Spring Boot microservices, specifically focusing on gRPC for internal calls and Kafka for event streaming.
