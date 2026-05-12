# Personal Finance & Subscription Tracker

A Spring Boot web application that helps individuals and families manage recurring subscriptions, track expenses by category, and monitor their financial health. Users centralize subscriptions across services (Netflix, Spotify, cloud storage, etc.), set spending budgets per category, share subscription costs with other users, and gain insight into their disposable income and upcoming payment obligations.

---

## Project Description

The platform turns the messy reality of household subscriptions into a single auditable view. The main capabilities are:

- **User profile & income management**: register, log in, maintain monthly income as the baseline for financial calculations.
- **Subscription lifecycle**: full CRUD over recurring subscriptions, with monthly/yearly billing frequency, renewal date tracking, and automatic monthly-equivalent normalization.
- **Subscription sharing**: many-to-many cost split between users, by either percentage or fixed amount, governed by a request/accept/decline workflow.
- **Categories & budgets**: custom categories with per-category spending limits; current spending is recomputed automatically when subscriptions are added, edited, or deleted.
- **Payment methods & transactions**: register payment methods, link each subscription to one, and keep an immutable transaction history.
- **Financial dashboard**: disposable income, upcoming renewals (next 7 days), and per-category breakdowns.
- **Support tickets**: per-user ticket submission with admin triage (`OPEN -> IN_PROGRESS -> RESOLVED`), threaded replies, and a dedicated admin console.
- **Authentication & authorization**: Spring Security with `ROLE_USER` / `ROLE_ADMIN`, BCrypt password hashing, remember-me, CSRF protection, and a custom login page.

Full functional requirements are documented in [Functional_Requirements.md](Functional_Requirements.md).

---

## ER Diagram

The diagram below was generated from [docs/database/schema.dbml](docs/database/schema.dbml) using [dbdiagram.io](https://dbdiagram.io).

![ER Diagram](ERD.png)

**12 entities, all required relationship types covered:**

- **`@OneToOne`**: `Budget` <-> `Category`
- **`@OneToMany` / `@ManyToOne`**: `User` -> `Subscription`, `User` -> `Category`, `User` -> `PaymentMethod`, `Subscription` -> `Transaction`, `Subscription` -> `Category`, `Subscription` -> `PaymentMethod`, `Ticket` -> `TicketReply`, etc.
- **`@ManyToMany`** — `User` <<->> `Role` (via `user_roles`); `User` <<->> `Subscription` (via `subscription_shares` with extra columns)

---

## Setup Instructions

### Prerequisites

- JDK 25 (or any JDK 21+; adjust `JAVA_HOME` accordingly)
- MySQL 8.x running on `localhost:3306` with database `financetracker`, user `root`, password `root` (override via env vars or `application-dev.yml`)
- Maven wrapper (bundled and no global Maven needed)

### Run locally (dev profile, MySQL)

PowerShell:
```powershell
$env:JAVA_HOME = "C:\Users\<you>\.jdks\openjdk-25.0.2"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
Set-Location "FinanceTracker"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

The app listens on `http://localhost:8080`. Default seeded admin credentials (created by `DataInitializer`):

- **Email:** `admin@financetracker.com`
- **Password:** `admin123`

### Run tests (test profile, H2 in-memory)

```powershell
.\mvnw.cmd test "-Dspring.profiles.active=test"
```

To produce the JaCoCo coverage report:

```powershell
.\mvnw.cmd verify
```

Open `FinanceTracker/target/site/jacoco/index.html` for the HTML coverage report.

### Run with Docker

```powershell
Set-Location "FinanceTracker"
docker compose up --build
```

This starts MySQL and the application using `application-docker.yml`.

---

## Screenshots

> _To be added_
