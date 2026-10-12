# LangChain4j Playground

A Spring Boot application for experimenting with AI chat and Java tool calling using LangChain4j and OpenAI. It exposes a chat endpoint and an order support assistant that can retrieve order details and cancel orders stored in MySQL.

## Stack

| Component | Version / detail |
| --- | --- |
| Java | 21 or newer |
| Spring Boot | 4.1.1 |
| LangChain4j | 1.21.0-beta31 |
| Persistence | Spring Data JPA and MySQL |
| Build | Maven |
| Base package | `com.gyaneswar` |

## Prerequisites

- A JDK compatible with Java 21 and Maven installed, with `java` and `mvn` on your `PATH`.
- A running MySQL instance and a database user with permission to create and update tables in the application database.
- An OpenAI API key and access to a chat model that supports tool calling.

## Run locally

### 1. Clone the repository

```sh
git clone https://github.com/Gyaneswar/Langchain4j-playground.git
cd Langchain4j-playground
```

### 2. Create the database

Run this in your MySQL client:

```sql
CREATE DATABASE IF NOT EXISTS langchain4j_playground;
```

Hibernate creates or updates the application tables at startup using `spring.jpa.hibernate.ddl-auto=update`. The database itself must already exist.

### 3. Configure the application

Set the following environment variables in the terminal where you will start the application. Replace the credential and model placeholders with your own values. These settings override the defaults in [application.yaml](src/main/resources/application.yaml).

**PowerShell**

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:mysql://localhost:3306/langchain4j_playground"
$env:SPRING_DATASOURCE_USERNAME = "your-mysql-user"
$env:SPRING_DATASOURCE_PASSWORD = "your-mysql-password"
$env:LANGCHAIN4J_OPENAI_CHATMODEL_APIKEY = "your-openai-api-key"
$env:LANGCHAIN4J_OPENAI_CHATMODEL_MODELNAME = "your-tool-capable-model"
```

**Bash / Zsh**

```sh
export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/langchain4j_playground"
export SPRING_DATASOURCE_USERNAME="your-mysql-user"
export SPRING_DATASOURCE_PASSWORD="your-mysql-password"
export LANGCHAIN4J_OPENAI_CHATMODEL_APIKEY="your-openai-api-key"
export LANGCHAIN4J_OPENAI_CHATMODEL_MODELNAME="your-tool-capable-model"
```

The LangChain4j environment variable names correspond to `langchain4j.open-ai.chat-model.api-key` and `langchain4j.open-ai.chat-model.model-name`. Keep real credentials out of committed files.

### 4. Start the application

```sh
mvn spring-boot:run
```

The server listens on `http://localhost:9292`. Set `SERVER_PORT` to use a different port.

### 5. Add a sample order

After the application starts and creates the `orders` table, run this once in MySQL to populate an order for the examples below:

```sql
USE langchain4j_playground;

INSERT INTO orders (
    order_id, customerName, productName, status,
    currentLocation, expectedDelivery, totalAmount, paymentStatus
) VALUES (
    'ORD-1001', 'Alex', 'Wireless Headphones', 'PROCESSING',
    'Warehouse', DATE_ADD(CURRENT_DATE(), INTERVAL 3 DAY), 2499.00, 'PAID'
);
```

The column names match the entity and the configured Hibernate physical naming strategy. No sample orders are inserted automatically.

## API examples

Both endpoints accept a required `message` query parameter and return the assistant's answer as a string.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| GET | `/api/ai-support` | Chat with the AI assistant |
| GET | `/api/ai-support/order` | Ask about an order or request its cancellation |

Use the commands below in a shell with cURL. In Windows PowerShell, use `curl.exe` in place of `curl`.

**Chat**

```sh
curl --get "http://localhost:9292/api/ai-support" --data-urlencode "message=Explain Java interfaces in two sentences."
```

**Check an order**

```sh
curl --get "http://localhost:9292/api/ai-support/order" --data-urlencode "message=What is the status and expected delivery date of order ORD-1001?"
```

**Cancel an order**

```sh
curl --get "http://localhost:9292/api/ai-support/order" --data-urlencode "message=Cancel order ORD-1001."
```

Cancellation updates the order in MySQL: its status becomes `CANCELLED` and its expected delivery date is cleared. The service rejects cancellation when the current status is `Shipped`, ignoring case. Requests for an unknown order ID raise an error in the order service.

The order assistant is instructed to use the available Java tools for real order information. Tool selection and response wording depend on the configured model.

## Project structure

```text
src/main/java/com/gyaneswar/
|-- Langchain4jExampleApplication.java
|-- controller/AiSupportController.java
|-- model/OrderEntity.java
|-- repository/OrderRepository.java
|-- service/
|   |-- ChatAIAgent.java
|   |-- OrderSupportAIAgent.java
|   `-- OrderService.java
`-- tool/OrderTools.java

src/main/resources/application.yaml
src/test/java/com/gyaneswar/Langchain4JExampleApplicationTests.java
```

`AiSupportController` delegates to the AI service interfaces. `OrderTools` exposes `getOrderStatus` and `cancelOrder` to the model, and `OrderService` uses `OrderRepository` to read and update MySQL records.

## Build and validation

Compile the application and test sources without starting Spring or connecting to external services:

```sh
mvn -DskipTests test-compile
```

Run the existing Spring context test with the database and model configuration from the setup steps available:

```sh
mvn test
```

Build an executable JAR without running tests, then start it in the configured terminal:

```sh
mvn -DskipTests package
java -jar target/langchain4j-example-0.0.1-SNAPSHOT.jar
```

## Current scope

This is a learning project. It has no authentication or per-customer order authorization, and cancellation is currently exposed through the same GET endpoint as order queries. Model request and response logging is enabled in `application.yaml`.
