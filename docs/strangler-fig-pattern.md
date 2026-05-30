# Strangler Fig pattern

This document explains how the Personal Finance and Subscription Tracker used the Strangler Fig pattern to move from a monolithic Spring Boot application to a microservices architecture.

## Pattern overview

The Strangler Fig pattern is used when an existing monolith is replaced by new services over time. Instead of rewriting the whole system at once, the team keeps the old application available and extracts one responsibility at a time into a separate service.

In this project, the original monolith remains in `FinanceTracker/`. The microservices version was built around it by separating the application into services with clearer ownership: users and UI, finance operations, and reporting. The old implementation was not deleted, which made it possible to compare the new services with the original behavior during the migration.

The BFF pattern supports this migration. `user-service` replaces the old user and authentication code and becomes the new browser-facing entry point. The web interface can use the extracted backend services without exposing them directly to the browser.

## Why this pattern was chosen

The original application had several responsibilities inside one Spring Boot project:

- authentication and users
- roles and support tickets
- categories and budgets
- payment methods
- subscriptions, sharing, and transactions
- dashboard and reporting calculations
- Thymeleaf pages for the web interface

Moving all of this into microservices without a migration strategy would make the change harder to test and maintain. The work to ensure everything still works while the migration is in progress is enormous.

The Strangler Fig pattern fits the project because the application has natural domain boundaries. Each part could be separated into a service without changing the whole system design at the same time.

The reason for using the pattern was risk reduction. The monolith stayed available as a working reference, while the new services took ownership of specific features. This gave the project a safer migration path, rather than replacing every layer blindly.

## How the pattern was applied

The migration started from the monolithic application in `FinanceTracker/`. Its main responsibilities were then extracted into three independent Spring Boot services:

| Extracted service | Responsibility taken from the monolith |
|---|---|
| `user-service` | Authentication, users, roles, support tickets, Thymeleaf UI, browser-facing BFF behavior, and internal user lookup endpoints |
| `finance-core-service` | Categories, budgets, payment methods, subscriptions, sharing, and transactions |
| `reporting-service` | Dashboard calculations, disposable income, renewal summaries, and category spending reports |

This is the Strangler Fig idea applied at project level. The monolith represents the original system. The new services grow around it and take over its responsibilities. By the end of the migration phase, the microservices stack becomes the main implementation, while the monolith remains in the repository as the old version and fallback reference.

`user-service` is especially important in this setup because it replaces the monolith as the web entry point. The browser no longer needs to interact with finance and reporting features through one large application. It talks to `user-service`, and `user-service` coordinates the extracted services behind the scenes.

## Migration result

The current architecture is split by business responsibility:

* `user-service` is the browser-facing service and BFF. It serves the Thymeleaf UI, handles login and session security, and calls backend services when a page needs finance or reporting data.

* `finance-core-service` owns the core finance domain. It exposes REST APIs for subscriptions, categories, budgets, payment methods, transactions, and sharing. It calls `user-service` internal endpoints when it needs to validate users or resolve share recipients.

* `reporting-service` owns dashboard and reporting calculations. It is stateless and assembles reports by calling both `user-service` and `finance-core-service`.

## Service communication

The extracted services communicate through REST with Spring `RestClient`:

| Caller | Target | Purpose |
|---|---|---|
| `user-service` | `finance-core-service` | Finance CRUD pages |
| `user-service` | `reporting-service` | Dashboard page |
| `finance-core-service` | `user-service` | User validation and share recipient lookup |
| `reporting-service` | `user-service` | User profile and income data |
| `reporting-service` | `finance-core-service` | Subscription, category, and spending data |

In local development, these calls use `localhost` ports. In Docker Compose, they use service names such as `http://finance-core-service:8081`.

The BFF relationship is visible here: browser requests enter through `user-service`, while backend calls to finance and reporting APIs stay inside the server-side application flow.

## Data ownership

The migration also separated data ownership:

| Service | Database state |
|---|---|
| `user-service` | Own MySQL database for users, roles, and support tickets |
| `finance-core-service` | Own MySQL database for finance entities |
| `reporting-service` | Stateless, reads from the other services through REST |

This supports the Strangler Fig migration because each extracted service owns the data for the responsibility it took from the monolith. The services no longer depend on one shared monolithic database.

## What was strangled from the monolith

The following responsibilities were moved out of the monolith and into the microservices stack:

- User authentication and role management moved to `user-service`.
- Support ticket workflows moved to `user-service`.
- Thymeleaf UI coordination and BFF behavior moved to `user-service`.
- Category and budget management moved to `finance-core-service`.
- Payment method management moved to `finance-core-service`.
- Subscription and transaction management moved to `finance-core-service`.
- Subscription sharing workflows moved to `finance-core-service`.
- Dashboard and reporting calculations moved to `reporting-service`.

After these extractions, the microservices stack can run the main application flow through Docker Compose. The monolith is still present, but the new architecture has taken over the main responsibilities.

## Repository evidence

The pattern can be seen in the repository structure:

| Path | Meaning |
|---|---|
| `FinanceTracker/` | Original monolithic application kept as the old implementation |
| `user-service/` | Extracted user, authentication, support, and UI service |
| `finance-core-service/` | Extracted finance domain service |
| `reporting-service/` | Extracted reporting service |
| `docker-compose.yml` | Runs the microservices stack |
| `.github/workflows/ci.yml` | Validates tests and Docker build |
| `MICROSERVICES_PHASE_II.md` | Tracks implemented and future microservices requirements |

Together, these files show the Strangler Fig migration at repository level: the original application remains available, while the extracted services now implement the same main business areas as independent deployable units.
