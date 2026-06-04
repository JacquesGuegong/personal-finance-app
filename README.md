# Personal Finance Tracker — REST API

A REST API for tracking personal finances. Built with Java 21, Spring Boot 3, PostgreSQL, and JWT authentication.

---

## Architecture

The application is organized into four horizontal layers. Each layer has a single responsibility and only talks to the layer directly below it.

```
HTTP Request
     │
     ▼
┌─────────────┐
│  Controller │  ← Handles HTTP. Validates input. Calls service. Returns response.
└──────┬──────┘
       │  calls
       ▼
┌─────────────┐
│   Service   │  ← All business logic lives here. Enforces user ownership rules.
└──────┬──────┘
       │  calls
       ▼
┌─────────────┐
│ Repository  │  ← Database access only. Spring Data JPA auto-generates SQL.
└──────┬──────┘
       │  reads/writes
       ▼
┌─────────────┐
│  PostgreSQL │
└─────────────┘
```

### Why layers?

**Single Responsibility** — each class does one thing. A controller doesn't know how to calculate a budget; a repository doesn't know about HTTP status codes.

**Testability** — you can test the service layer with plain JUnit (no web server needed). You test controllers with MockMvc (no database needed). Isolated units are fast and reliable.

**Replaceability** — if you switch from PostgreSQL to MongoDB tomorrow, only the repository layer changes. The controller and service don't care.

---

## Package Guide

| Package | Contents | Rule |
|---|---|---|
| `controller` | `@RestController` classes | HTTP only — no business logic |
| `service` | `@Service` classes | All decisions happen here |
| `repository` | `@Repository` interfaces | Database access only |
| `entity` | `@Entity` classes | One class = one database table |
| `dto` | Request/Response objects | API contract — never expose entities |
| `config` | `@Configuration` classes | Security, JWT, beans |
| `exception` | Custom exceptions + `@ControllerAdvice` | Consistent error responses |

---

## Tech Stack

| Technology | Why |
|---|---|
| **Java 21** | Latest LTS. Virtual threads available for high concurrency. |
| **Spring Boot 3** | Auto-configuration eliminates boilerplate setup. |
| **Spring Data JPA** | Generates SQL from method names — no hand-written queries for common cases. |
| **Spring Security** | Battle-tested authentication and authorization framework. |
| **PostgreSQL** | Reliable, ACID-compliant relational database. |
| **Flyway** | Version-controls the database schema — every schema change is a tracked SQL file. |
| **Lombok** | Generates getters, setters, constructors at compile time — less noise in entity/DTO classes. |
| **JWT** | Stateless authentication — the server doesn't store sessions. |
| **Anthropic Claude API** | Powers the AI features — spending summaries, auto-categorization, budget advice, and anomaly detection. |

---

## Running Locally

### Prerequisites
- Java 21
- PostgreSQL running on port 5432

### 1. Create the database

```sql
CREATE DATABASE finance_tracker;
```

### 2. Set environment variables

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/finance_tracker
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=your_password
export JWT_SECRET=your-long-random-secret-string

# Optional — enables the AI features (see "AI Features" below). If unset, the app
# still starts and runs normally; the AI endpoints just return a graceful fallback
# message instead of failing.
export ANTHROPIC_API_KEY=your-anthropic-api-key
```

### 3. Run

```bash
./gradlew bootRun
```

Flyway will automatically create the tables on first startup.

### 4. Run tests

```bash
./gradlew test
```

---

## Database Schema

Flyway migrations live in `src/main/resources/db/migration/`.

Naming convention: `V{version}__{description}.sql`

| Migration | Description |
|---|---|
| `V1__init_schema.sql` | Creates `users`, `transactions`, `budgets` tables |

---

## Security Model

- Passwords are hashed with **BCrypt** before storage — never stored as plain text.
- Every API request (except login/register) requires a **JWT Bearer token**.
- Every database query filters by `userId` — users can only see their own data.
- Secrets (`JWT_SECRET`, database password) are read from **environment variables** — never hardcoded.

---

## API Overview

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/auth/register` | Create account |
| `POST` | `/api/auth/login` | Get JWT token |
| `GET` | `/api/transactions` | List my transactions |
| `POST` | `/api/transactions` | Create transaction |
| `PUT` | `/api/transactions/{id}` | Update transaction |
| `DELETE` | `/api/transactions/{id}` | Delete transaction |
| `GET` | `/api/budgets` | List my budgets |
| `POST` | `/api/budgets` | Create budget |
| `GET` | `/api/ai/spending-summary` | Plain-English summary of this month's spending |
| `POST` | `/api/ai/categorize` | Suggest a category for a transaction description |
| `GET` | `/api/ai/budget-advice` | Personalized saving recommendations |

---

## AI Features

The app integrates the **Anthropic Claude API** to add four AI-powered features. The
code lives in `service/AiService.java` (what we ask Claude) and `ai/AnthropicClient.java`
(how we call Claude over HTTP) — the same one-responsibility-per-class rule as the rest
of the project.

| Feature | Trigger | What it does |
|---|---|---|
| **Spending summary** | `GET /api/ai/spending-summary` | Summarizes this month's spending vs last month in plain English |
| **Auto-categorize** | `POST /api/ai/categorize` | Maps a transaction description (e.g. `"Netflix 15.99"`) to a category |
| **Budget advice** | `GET /api/ai/budget-advice` | Gives encouraging, specific saving tips based on your budgets |
| **Anomaly detection** | Automatic on new transactions | Flags an unusually large charge and raises an alert |

Two design rules shape every AI call:

**1. Summarize before sending.** We never ship raw transaction rows (which can contain
merchant names or notes) to a third party. We aggregate to category totals first — enough
for a useful answer, far less data leaked.

**2. Degrade gracefully.** Every Claude call is wrapped so that if the API is down — or the
`ANTHROPIC_API_KEY` is not set — the user gets a helpful fallback string, never a `500`.
Anomaly detection also gates on cost: it only calls Claude when a charge exceeds **2×** the
category's recent average, so normal spending never triggers a paid API call.

The model and limits are configured in `application.properties` (`app.anthropic.*`) and
default to `claude-haiku-4-5`.
