**Proiect: Aplicații Web cu Arhitectură de Microservicii**

# INFORMAȚII GENERALE

**Echipe:** 2-3 studenți

**Durată:** 1 semestru

**Punctaj total:** 100% (60% cerințe obligatorii + 40% cerințe opționale)

# PARTEA I: CERINȚE OBLIGATORII (60%)

## 1\. Model de Date (10%)

**Cerințe: Lab2**

*   Minimum 6-7 entități interconectate
*   Relații de toate tipurile:

\- @OneToOne (min. 1 exemplu)

\- @OneToMany / @ManyToOne (min. 2 exemple)

\- @ManyToMany (min. 1 exemplu)

*   Diagrama ER documentată în README

**Criterii evaluare:**

*   Complexitate model de date
*   Relevanța relațiilor pentru domeniul ales
*   Documentație

## 2\. Operații CRUD Complete (8%)

**Cerințe: Lab2**

*   **Create, Read, Update, Delete** pentru toate entitățile
*   Repository pattern cu Spring Data JPA
*   Service layer cu logică de business
*   Exception handling specific pentru fiecare operație

**Criterii evaluare:**

*   Implementare completă CRUD
*   Calitatea codului și separarea responsabilităților
*   Tratarea excepțiilor

## 3\. Configurare Multi-Environment (5%)

**Cerințe: Lab2**

*   Minimum 2 profiluri Spring (dev, test)
*   Configurare pentru minimum 2 baze de date diferite:

\- Una pentru dezvoltare (PostgreSQL/MySQL)

\- Una pentru testare (H2 in-memory sau separată)

*   Fișiere de configurare separate (application-dev.yml, application-test.yml)

**Criterii evaluare:**

*   Configurare profiles (3p)
*   Separarea corectă a mediilor (3p)

## 4\. Testing (7%)

**Cerințe: Lab 4**

*   **Unit tests:** minimum 70% coverage pentru service layer
*   **Integration tests:** minimum 3 scenarii end-to-end
*   Utilizare JUnit 5 + Mockito
*   Test database configuration

**Criterii evaluare:**

*   Unit tests
*   Integration tests
*   Code coverage

## 5\. Views și Validare (10%)

**Cerințe: Lab 4**

*   **Frontend:** Thymeleaf/JSP sau framework modern (React/Vue/Angular)
*   **Formulare:** pentru toate operațiile CRUD
*   **Validare:**

\- Server-side cu Bean Validation (@Valid, @NotNull, etc.)

\- Client-side validation

\- Mesaje de eroare user-friendly

*   **Exception handling:** pagini de eroare custom (404, 500, etc.)

**Criterii evaluare:**

*   Interfață funcțională și intuitivă
*   Validare completă
*   Tratarea erorilor

## 6\. Logging (4%)

**Cerințe:**

*   Framework: SLF4J + Logback/Log4j2
*   Nivele de logging configurate corect (INFO, DEBUG, ERROR)
*   Logging în fișiere separate pentru erori
*   **\[Opțional\]** Aspecte pentru logging automat

**Criterii evaluare:**

*   Configurare logging
*   Utilizare adecvată în cod

## 7\. Paginare și Sortare (6%)

**Cerințe:**

*   Implementare Pageable pentru minimum 3 entități
*   Opțiuni de sortare după minim 2 criterii per entitate
*   UI pentru navigare între pagini
*   Configurare dimensiune pagină

**Criterii evaluare:**

*   Implementare backend
*   Integrare frontend

## 8\. Spring Security (10%)

**Cerințe minime: Lab5 (saptamana 6)**

*   Autentificare JDBC
*   Minimum 2 roluri (USER, ADMIN)
*   Protejarea endpoint-urilor bazată pe rol
*   Pagină de login custom
*   Logout funcțional

**Cerințe recomandate pentru punctaj maxim:**

*   Password encoding (BCrypt)
*   Remember me functionality
*   CSRF protection activă

**Criterii evaluare:**

*   Autentificare funcțională
*   Autorizare bazată pe roluri
*   Best practices securitate

# PARTEA II: CERINȚE OPȚIONALE - Microservicii (40%)

## Arhitectura Generală

**Obligatoriu pentru cerințe opționale:** Migrarea aplicației monolitice la minimum **3 microservicii independente**.

**Exemple de decompoziție:**

*   User Service (autentificare, gestionare utilizatori)
*   Business Logic Service (entitățile principale)
*   Notification Service / Reporting Service

### 1\. Configurare Centralizată (4%) productapi demo (sapatamana 5,6)

**Tehnologii:** Spring Cloud Config / Kubernetes ConfigMaps

**Cerințe:**

*   Config Server pentru toate microserviciile
*   Externalizarea configurațiilor sensibile
*   Refresh dinamic fără restart+-+

### 2\. Service Discovery și Comunicare (6%)

**Tehnologii:** Eureka / Kubernetes Service Discovery producapi demo (sapatamana 5,6)

**Cerințe:**

*   Service registry funcțional
*   Comunicare inter-servicii prin:

\- REST (Feign Client / RestTemplate)

\- SAU Message Broker (RabbitMQ/Kafka)

*   Demonstrare că serviciile se descoperă automat

### 3\. Load Balancing și Scalabilitate (5%)

**Cerințe: producapi demo (sapatamana 5,6)**

*   Client-side load balancing (Spring Cloud LoadBalancer)
*   Demonstrare rulare multiplă instanță pentru un serviciu
*   Testing cu minim 2 instanțe per serviciu

### 4\. API Gateway (4%)

**Tehnologii:** Spring Cloud Gateway / Kubernetes Ingress

**Cerințe:**

*   Routing centralizat
*   Rate limiting
*   Request/Response filtering

### 5\. Monitorizare și Metrici (5%)

**Tehnologii:** Spring Boot Actuator + Prometheus + Grafana

**Cerințe: producapi demo (sapatamana 6)**

*   Actuator endpoints expuse
*   Dashboard cu metrici (CPU, memory, requests)
*   Health checks pentru toate serviciile
*   Distributed tracing (Zipkin/Jaeger - bonus)

### 6\. Securitate Distribuită (4%)

**Cerințe:**

*   JWT authentication între microservicii
*   SAU OAuth2 / Keycloak
*   Secure communication (HTTPS - bonus)

### 7\. Resilience și Fault Tolerance (5%)

**Tehnologii:** Resilience4j / Hystrix

**Cerințe:**

*   Circuit Breaker pentru minimum 2 servicii
*   Retry mechanism
*   Fallback methods
*   Demonstrare comportament în caz de eroare

### 8\. Design Patterns (3%)

**Exemple:**

*   Saga Pattern pentru tranzacții distribuite
*   CQRS pentru separarea read/write
*   Event Sourcing
*   Strangler Fig pentru migrare treptată

**Cerințe:** Implementare și documentare minimum 1 pattern

### 9\. NoSQL și Caching (4%)

**Cerințe: Lab3**

*   Integrare minimum 1 bază NoSQL (MongoDB/Redis/Cassandra)
*   Caching layer (Redis/Hazelcast) pentru date accesate frecvent
*   Demonstrare beneficii de performanță

### 10\. Micro-frontends (bonus 2-..%)

**Cerințe:**

*   Separarea frontend-ului în module independente
*   Tehnologii: Module Federation / Single-SPA

### 11\. CI/CD Pipeline (2-..%)

**Tehnologii:** Jenkins / GitLab CI / GitHub Actions

**Cerințe:**

*   Build automatizat
*   Rulare teste automate
*   Deployment automat (staging)
*   Docker containerization

### 12\. AI Agents - Dezvoltare (bonus 2-..%)

**Exemple:**

*   GitHub Copilot pentru pair programming
*   Code review automatizat
*   Documentație generată automat

**Cerințe:** Documentare utilizare și beneficii

### 13\. AI Agents - Runtime (2-..%)

**Exemple aplicație:**

*   Recomandări personalizate
*   Chatbot pentru suport
*   Analiza semantică a conținutului
*   Search enhancement

**Cerințe:** Integrare funcțională în aplicație

# LIVRABILE ȘI EVALUARE

## 1\. Repository Git

**Cerințe:**

*   Repository public pe GitHub/GitLab
*   Commit-uri regulate
*   Branch strategy (main + dev minimum)
*   README complet cu:

\- Descrierea proiectului

\- Arhitectură

\- Setup instructions

\- API documentation

\- Screenshots

\- Contribuții membrii echipei

**Penalizări:**

*   Commit-uri neregulate
*   Documentație incompletă
*   Un singur commit final

## 2\. Deployment

**Opțiuni:**

*   Heroku / Railway / Render (free tier)
*   AWS / Azure / GCP (free tier student)
*   Kubernetes cluster (Minikube / Kind local)
*   Docker Compose deployment

**Cerințe:**

*   Aplicație accesibilă online
*   Link funcțional în README
*   Environment variables configurate corect

## 3\. Prezentare Finală (obligatoriu)

**Format:** 15-20 minute / 5-10 minute întrebări

**Conținut:**

*   Demo live al aplicației
*   Arhitectură și decizii tehnice
*   Walkthrough prin cod (părți cheie)
*   Provocări și soluții
*   Lessons learned

**Deadline**

**Etapa**

**Deliverable**

Săptămâna 4

**22 martie**

Planificare

Tema proiect. Document specificații + diagrame ER

Săptămâna 10

**10 mai**

Cerințe obligatorii

Model + CRUD + teste basic + Security + views + validare

Săptămâna 14

Finalizare, Cerințe opționale

Microservicii (minim 3 servicii), Documentație + deployment

Sesiune

Prezentare

Demo + Q&A

# CRITERII DE PUNCTARE - REZUMAT

## Cerințe Obligatorii (60%)

*   Model de date (10%)
*   CRUD operations (8%)
*   Multi-environment (5%)
*   Testing (7%)
*   Views & validation (10%)
*   Logging (4%)
*   Paginare & sortare (6%)
*   Spring Security (10%)

## Cerințe Opționale (40%)

*   Config centralizată (4%)
*   Service discovery (6%)
*   Load balancing (5%)
*   API Gateway (4%)
*   Monitorizare (5%)
*   Securitate (4%)
*   Resilience (5%)
*   Design patterns (3%)
*   NoSQL & Caching (4%)

## Bonus (până la 10%)

*   Micro-frontends (2%)
*   AI in development (2%)
*   AI integration (2%)
*   Deployment live (2%)

# RECOMANDĂRI

### Pentru Nota 6-7 (Cerințe Obligatorii)

*   Focus pe implementare solidă a tuturor cerințelor obligatorii
*   Testing adecvat
*   Documentație clară

### Pentru Nota 8-9 (+ Microservicii Basic)

*   Toate cerințele obligatorii
*   Minim 3 microservicii
*   Service discovery + load balancing
*   Monitoring basic

### Pentru Nota 10 (Microservicii Complet)

*   Implementare completă și solidă
*   Minimum 5-6 cerințe opționale
*   Best practices peste tot
*   Deployment live