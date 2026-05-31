# AI agents in development

This document explains how AI tools were used during the development of the Personal Finance and Subscription Tracker project. It covers the optional "AI Agents - Development" requirement from the project specification.

## Tools used

The project used two AI development tools:

- Codex, mainly for codebase analysis, documentation drafts, implementation support, and requirement checks.
- GitHub Copilot, mainly for coding assistance, repetitive code completion, and test scaffolding.

The tools were used as assistants, not as the source of truth. The architecture, domain idea, and first monolithic version were planned and implemented manually first. AI support was added later to speed up migration, documentation, testing, and review work.

## How AI was used

AI was used in two main areas: documentation and coding.

For documentation, AI helped turn implementation details into readable project documents. This included the README, the microservices architecture description, and the pattern documentation for the Strangler Fig and Backend-for-Frontend approaches.

For coding, AI helped with repetitive and structural work after the main project direction was already clear. The initial monolithic application was written manually. During the microservices phase, AI was useful for separating responsibilities, checking the service boundaries, and filling in supporting code around controllers, DTOs, tests, configuration, and documentation.

## Spec driven development style

The project also used a lightweight spec driven development style. This was not a strict external framework, but a practical workflow based on the project statement and the functional requirements.

Before asking AI for help, the expected behavior was usually defined from one of these sources:

- the official project requirements
- the functional requirements document
- the existing monolithic implementation
- the intended responsibility of each microservice
- the expected behavior of a page, endpoint, or service method

AI was then used to compare the current implementation against those expectations. This helped with tasks such as checking whether CRUD flows were complete, whether the microservices split matched the optional requirements, and whether the documentation described real behavior from the codebase.

This approach was useful because it kept AI work tied to a specification. The assistant was not asked to invent the project direction from scratch. It was asked to work from requirements, existing code, and concrete architecture decisions, then the result was manually reviewed.

## Microservices migration support

The project started as a monolithic Spring Boot application in `FinanceTracker/`. After that version was working, the application was split into three services:

- `user-service`, for users, authentication, support tickets, Thymeleaf UI, and the browser-facing backend.
- `finance-core-service`, for categories, budgets, payment methods, subscriptions, sharing, and transactions.
- `reporting-service`, for dashboard calculations, disposable income, upcoming renewals, and category spending reports.

AI helped during this migration by checking whether the responsibilities were separated clearly enough and by helping document the result. The final architecture still came from the project requirements and the existing monolith, but AI made it easier to reason about the split and explain it consistently.

One concrete example is the Strangler Fig documentation. AI first helped describe the migration pattern, but the text had to be adjusted during review. A strict Strangler Fig implementation often includes gradual traffic routing through a gateway. This project does not implement that full routing transition. The final documentation was corrected to explain the pattern at project level: the monolith remains in the repository as the original implementation, while the new services take over its main responsibilities.

## Documentation work

AI assistance was used for the following documentation tasks:

| Document | How AI helped |
|---|---|
| `README.md` | Helped structure the project overview, architecture, setup instructions, monitoring section, CI/CD section, and ER diagram notes. |
| `docs/strangler-fig-pattern.md` | Helped explain how the project moved from the monolith to separate services. |
| `docs/backend-for-frontend-pattern.md` | Helped describe why `user-service` acts as the browser-facing backend for the Thymeleaf UI. |
| Project requirement review | Helped compare the implementation against the mandatory and optional requirements from the project statement. |

The useful part was not that AI produced final text immediately. The useful part was that it produced drafts and checklists quickly. Those drafts were then reviewed against the actual repository.

## Coding and test support

GitHub Copilot and Codex were also used while coding.

The first tests were written manually. After the testing pattern was established, AI helped complete missing tests and cover more service-layer behavior. This was useful for repetitive cases where the structure was already known: arrange repository mocks, call the service method, assert the result, and verify interactions.

AI also helped with code that follows repeated Spring Boot patterns, such as DTO mapping, controller methods, validation annotations, service calls, and configuration snippets. These suggestions still needed manual review, especially because small mistakes in a Spring application can compile but still be wrong at runtime.

## Requirement review

AI was used to review what had been implemented and what was still missing from the project requirements.

This was especially useful for the optional microservices section. The project had several moving parts: Docker Compose, service communication, monitoring, tracing, CI, security, resilience, and documentation. AI helped compare those pieces against the project checklist and separate completed work from future work.

For example, the review confirmed that the project has three microservices, REST communication, Docker Compose deployment, Actuator endpoints, Prometheus, Grafana, Zipkin, CI checks, and documented design patterns. It also helped identify missing or partial optional items such as centralized configuration, service discovery, load balancing, API gateway, distributed JWT security, and NoSQL or caching.

## Human review

All AI-generated output was manually reviewed.

The repository remained the source of truth. Claims in documentation were checked against files such as:

- `docker-compose.yml`
- `README.md`
- service folders like `user-service/`, `finance-core-service/`, and `reporting-service/`
- Spring controllers and service classes
- REST clients used for inter-service communication
- profile-specific configuration files
- test files and JaCoCo reports
- GitHub Actions workflow
- monitoring configuration under `monitoring/`

For code changes, review was not enough by itself. Every AI-assisted change had to be checked by running the relevant unit tests or service tests. This was especially important for generated or completed test cases, service-layer changes, controller changes, and microservice configuration changes. If a suggestion broke tests or did not fit the existing behavior, it was changed or discarded.

During review, some AI suggestions and documentation drafts were corrected because they were too generic, overstated the implementation, or implied features that were not actually present. For example, AI could describe Docker Compose service names as if they were full service discovery, or describe the BFF behavior as if it were a complete API Gateway. Similar overstatements appeared in documentation drafts, where the wording sometimes made optional or partial features sound fully implemented. Those claims were adjusted. Docker Compose DNS is useful for local service-to-service calls, but it is not the same as Eureka or Kubernetes service discovery. The `user-service` works as a browser-facing BFF, but it does not implement gateway features such as centralized routing, rate limiting, or request filtering.

This review step mattered because AI can write confident text even when the details are slightly off. The final documentation keeps the parts that match the project and removes or weakens claims that go beyond the implementation.

## Benefits

Using AI during development helped in practical ways:

- It reduced the time needed to write and polish documentation.
- It helped keep the README and detailed docs consistent with each other.
- It made the microservices migration easier to explain during a presentation.
- It helped complete repetitive test cases after the first examples were written manually.
- It helped audit the project against the original requirements.
- It made it easier to spot gaps in the optional microservices requirements.

The biggest benefit was speed with supervision. AI helped produce drafts, suggest structure, and fill in repetitive work, but the final decisions stayed with the developer.

## Limits

AI was not used as a replacement for understanding the project.

It did not decide the domain, the main architecture, or the original monolithic implementation. It also did not replace manual verification. Every generated suggestion still had to be checked against the codebase and the project requirements.

The most important limitation was accuracy. AI is useful for drafts and patterns, but it can overstate what exists. 

## Conclusion

AI agents were used as development support for this project. Codex and GitHub Copilot helped with documentation, microservices migration support, repeated coding patterns, test completion, and requirement review.

The workflow was simple: plan and implement the core project manually, use AI to speed up repetitive or documentation-heavy work, then verify everything against the repository. This made the project easier to document and review without giving up control over the technical decisions.
