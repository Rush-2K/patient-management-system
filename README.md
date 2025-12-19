# Patient Management System (Microservices)

> **Learning Project:** This project is a hands-on exploration of high-performance Microservices architecture, following the https://www.youtube.com/watch?v=tseqdcFfTUY&t=19999s guide. It focuses on the transition from traditional REST to **gRPC** and **Event-Driven patterns**.

---

## 🏗 System Architecture & Flow

This system demonstrates how different communication protocols are used for specific use cases to optimize performance and reliability.



### Data Flow:
1.  **Client → Patient Service:** (REST/JSON) - Standard external communication.
2.  **Patient Service → Billing Service:** (**gRPC / Protobuf**) - Used for high-speed, strictly typed internal account creation upon patient registration.
3.  **Patient Service → Kafka:** (**Event Streaming**) - Patient data is published to the `patient` topic.
4.  **Analytics Service:** (**Kafka Consumer**) - Consumes data from the topic for long-term tracking and reporting without blocking the main workflow.

---

## 🛠 Tech Stack

* **Backend:** Java / Spring Boot 3.x
* **Communication:**
    * **gRPC & Protobuf:** Low-latency internal service-to-service calls.
    * **Apache Kafka:** Asynchronous event-driven analytics.
* **Database:** PostgreSQL (Dockerized)
* **DevOps:** Docker(Bitnami Kafka image)

---

## 📂 Service Overview

| Service | Protocol | Responsibility |
| :--- | :--- | :--- |
| **`patient-service`** | REST / gRPC | Primary entry point; saves to Postgres; triggers billing & events. |
| **`billing-service`** | gRPC (Server) | Listens for Protobuf messages to initialize billing accounts. |
| **`analytics-service`** | Kafka Consumer | Processes patient events for data insights and reporting. |

---

## 🚀 DevOps & Running the Project

Each service contains its own multi-stage `Dockerfile` to handle environment setup and dependency installation.

Each service is a Maven project. You can build them individually:
```bash
mvn clean install

##Note: This command will also trigger the profobuf-maven-plugin to compile your .proto files into Java classes
```

## 💡 Key Learnings & Implementation Details

* **Protobuf vs REST**: Implemented gRPC to reduce payload size and take advantage of **HTTP/2 multiplexing**, significantly speeding up service-to-service communication.
* **Decoupling with Kafka**: Leveraged the **Bitnami Kafka image** to implement an event-driven architecture, ensuring the Analytics service remains decoupled and doesn't impact the latency of the Patient service.
* **Containerization**: Wrote custom multi-stage Dockerfiles to optimize image size and ensure a consistent "build-once, run-anywhere" workflow.

