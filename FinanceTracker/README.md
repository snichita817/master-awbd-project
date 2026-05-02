# Finance Tracker — Analiză față de Cerințele Proiectului

> **Proiect:** Personal Finance & Subscription Tracker  
> **Framework:** Spring Boot 3.5.8, Java 17, H2 (in-memory/file), JUnit 5 + Mockito  
> **Evaluare raportată la:** Proiect_Aplicatii_Web_Microservicii — Cerințe Obligatorii (60%)

---

## Sumar Estimare Punctaj

| Cerință | Pondere | Estimat | Status |
|---|---|---|---|
| 1. Model de Date | 10% | ~7% | ⚠️ Parțial |
| 2. CRUD Complet | 8% | ~6% | ⚠️ Parțial |
| 3. Multi-Environment | 5% | 0% | ❌ Lipsă |
| 4. Testing | 7% | ~3% | ⚠️ Parțial |
| 5. Views & Validare | 10% | ~3% | ⚠️ Parțial |
| 6. Logging | 4% | ~1% | ⚠️ Parțial |
| 7. Paginare & Sortare | 6% | 0% | ❌ Lipsă |
| 8. Spring Security | 10% | 0% | ❌ Lipsă |
| **TOTAL** | **60%** | **~20%** | |

---

## 1. Model de Date (10%) — estimat ~7/10

### Ce s-a implementat ✅

Există **6 entități** interconectate (`User`, `Subscription`, `Category`, `Budget`, `PaymentMethod`, `Transaction`), la minimul cerut de 6–7.

Relațiile implementate:

- **`@OneToOne`** — `Budget` ↔ `Category`:
  - `src/main/java/com/unibuc/management/entity/Budget.java` (câmpul `category`, `@OneToOne`)
  - `src/main/java/com/unibuc/management/entity/Category.java` (câmpul `budget`, `@OneToOne(mappedBy="category")`)

- **`@ManyToOne` / `@OneToMany`** — multiple exemple:
  - `User` → `Subscription`, `Category`, `PaymentMethod` (`src/main/java/com/unibuc/management/entity/User.java`, câmpurile `subscriptions`, `paymentMethods`, `categories`)
  - `Subscription` → `Transaction` (`src/main/java/com/unibuc/management/entity/Subscription.java`, câmpul `transactions`)
  - `Subscription` → `Category`, `PaymentMethod` (câmpurile `category`, `paymentMethod`)

### Ce lipsește ❌

**Nu există nicio relație `@ManyToMany`**, care este cerință explicită (minimum 1 exemplu). Nicio entitate din aplicație nu modelează o relație many-to-many. De exemplu, s-ar putea implementa `User` ↔ `Category` (categorii partajate în familie), sau `Subscription` ↔ `Tag`.

---

## 2. Operații CRUD Complete (8%) — estimat ~6/8

### Ce s-a implementat ✅

CRUD complet pentru **toate 6 entitățile**, cu:
- Repository pattern via Spring Data JPA (ex. `src/main/java/com/unibuc/management/repository/UserRepository.java`, `SubscriptionRepository.java`)
- Interfețe de serviciu + implementări separate (ex. `UserService` / `UserServiceImpl`, `BudgetService` / `BudgetServiceImpl` etc.)
- Logică de business reală:
  - Validare email unic la creare/actualizare utilizator (`src/main/java/com/unibuc/management/service/UserServiceImpl.java`)
  - Actualizare automată a `currentSpending` în `Budget` la adăugarea/ștergerea/actualizarea unui `Subscription` (`src/main/java/com/unibuc/management/service/BudgetServiceImpl.java`, metodele `addSubscriptionToBudget`, `removeSubscriptionFromBudget`)
  - Normalizare costuri anuale la echivalent lunar (împărțire la 12) (`src/main/java/com/unibuc/management/service/FinanceServiceImpl.java`)
  - Query-uri JPQL custom în repository-uri (ex. `BudgetRepository.findExceededBudgets()`, `TransactionRepository.findByUserIdAndDateRange()`)
- Swagger/OpenAPI documentat complet pe toate endpoint-urile (`src/main/java/com/unibuc/management/config/OpenApiConfig.java`)

### Ce poate fi îmbunătățit ⚠️

**Exception handling folosește doar excepții generice.** În `src/main/java/com/unibuc/management/aop/ErrorControllerAdvice.java` sunt prinse doar `IllegalArgumentException` (→ 404) și `IllegalStateException` (→ 500). Cerința cere excepții **custom specifice** per operație (ex. `UserNotFoundException`, `DuplicateEmailException`, `BudgetAlreadyExistsException`), ceea ce ar face codul mai clar și mai ușor de extins.

De exemplu, în `src/main/java/com/unibuc/management/service/UserServiceImpl.java`:
```java
// Actual — generic
throw new IllegalArgumentException("User not found with id: " + id);

// Recomandat — custom exception
throw new UserNotFoundException(id);
```

---

## 3. Configurare Multi-Environment (5%) — estimat 0/5

### Ce lipsește complet ❌

Există un **singur fișier de configurare** `src/main/resources/application.properties`, fără niciun profil Spring activ. Cerința cere:

- `application-dev.properties` (sau `.yml`) cu o bază de date relațională (PostgreSQL / MySQL)
- `application-test.properties` (sau `.yml`) cu H2 in-memory, separat de baza de date de fișier
- `@ActiveProfiles("test")` pe clasele de test

Momentan, atât aplicația cât și testele rulează pe aceeași configurație H2 file-based, ceea ce înseamnă că testele pot "polua" datele de dezvoltare și invers.

---

## 4. Testing (7%) — estimat ~3/7

### Ce s-a implementat ✅

Există teste **`@WebMvcTest`** cu MockMvc și `@MockitoBean` (JUnit 5 + Mockito) pentru **toate 6 controllerele**:
- `src/test/java/com/unibuc/management/controllers/UserControllerTest.java` — 5 scenarii (creare, 404, validare email invalid, venit negativ, ștergere)
- `src/test/java/com/unibuc/management/controllers/SubscriptionControllerTest.java` — 2 scenarii
- `src/test/java/com/unibuc/management/controllers/BudgetControllerTest.java` — 2 scenarii
- `src/test/java/com/unibuc/management/controllers/CategoryControllerTest.java` — 3 scenarii
- `src/test/java/com/unibuc/management/controllers/TransactionControllerTest.java` — 2 scenarii
- `src/test/java/com/unibuc/management/controllers/PaymentMethodControllerTest.java` — 2 scenarii

### Ce lipsește ❌

**Zero teste pentru service layer.** Cerința cere minimum 70% coverage pe servicii. Nu există nicio clasă de test în afara pachetului `controllers/`. Serviciile (`UserServiceImpl`, `BudgetServiceImpl`, `SubscriptionServiceImpl` etc.) nu sunt testate deloc cu Mockito unit tests.

**Zero integration tests** end-to-end. Clasa `src/test/java/com/unibuc/management/FinanceTrackerApplicationTests.java` conține un singur test `contextLoads()` care verifică că aplicația pornește, dar nu testează niciun flux complet (ex. creare user → creare subscripție → verificare buget actualizat automat).

Cerința cere minimum **3 scenarii de integration test** end-to-end, folosind `@SpringBootTest` + `TestRestTemplate` sau `MockMvc` cu context real.

---

## 5. Views și Validare (10%) — estimat ~3/10

### Ce s-a implementat ✅

**Validare server-side completă** cu Bean Validation pe toate entitățile:
- `src/main/java/com/unibuc/management/entity/User.java`: `@NotBlank`, `@Email`, `@DecimalMin`
- `src/main/java/com/unibuc/management/entity/Subscription.java`: `@NotBlank`, `@DecimalMin`, `@FutureOrPresent`
- `src/main/java/com/unibuc/management/entity/Transaction.java`: `@PastOrPresent`, `@DecimalMin`
- `@Valid` aplicat corect pe toate endpoint-urile de creare/actualizare din controllere

### Ce lipsește complet ❌

**Nu există niciun frontend.** Deși `spring-boot-starter-thymeleaf` este prezent în `pom.xml`, el este dezactivat explicit în `src/main/resources/application.properties`:
```properties
spring.thymeleaf.check-template-location=false
```
Nu există niciun fișier HTML, niciun template Thymeleaf, niciun formular. Aplicația este exclusiv un REST API.

Cerința cere:
- Formulare pentru toate operațiile CRUD
- Validare client-side (JavaScript sau Thymeleaf)
- Pagini de eroare custom pentru 404 și 500

---

## 6. Logging (4%) — estimat ~1/4

### Ce s-a implementat ✅

Configurare parțială de logging SQL în `src/main/resources/application.properties`:
```properties
logging.level.web=debug
logging.level.sql=debug
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

### Ce lipsește ❌

**SLF4J nu este utilizat în niciun loc din cod.** Niciun serviciu, niciun controller nu are `Logger log = LoggerFactory.getLogger(...)`. Cerința cere logging explicit la nivel de business logic (INFO pentru operații, DEBUG pentru detalii, ERROR la excepții).

Nu există fișier `src/main/resources/logback.xml` sau `log4j2.xml` pentru:
- Configurarea nivelelor per pachet
- Scriere într-un fișier separat pentru erori (`logs/error.log`)
- Pattern de format pentru mesaje

De menționat că în `src/main/java/com/unibuc/management/aop/ErrorControllerAdvice.java` există deja un `@ControllerAdvice` care ar fi locul ideal pentru a loga erorile la nivel ERROR.

---

## 7. Paginare și Sortare (6%) — estimat 0/6

### Ce lipsește complet ❌

**Nu există nicio utilizare a `Pageable` sau `Sort`** în întreaga aplicație. Toate metodele din repository-uri și servicii returnează `List<T>` simple:
- `src/main/java/com/unibuc/management/repository/SubscriptionRepository.java`: `List<Subscription> findByUserId(Long userId)`
- `src/main/java/com/unibuc/management/repository/UserRepository.java`: moștenește `findAll()` simplu
- Și toate celelalte repository-uri

Cerința cere `Pageable` pentru minimum 3 entități, sortare după minimum 2 criterii, și UI pentru navigare între pagini.

Exemplu de ce ar trebui adăugat:
```java
// În SubscriptionRepository
Page<Subscription> findByUserId(Long userId, Pageable pageable);

// În controller
@GetMapping("/user/{userId}")
public ResponseEntity<Page<Subscription>> getSubscriptions(
    @PathVariable Long userId,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(defaultValue = "name") String sortBy) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
    ...
}
```

---

## 8. Spring Security (10%) — estimat 0/10

### Ce lipsește complet ❌

**Nu există nicio dependință de Spring Security** în `pom.xml` și nicio configurare de autentificare/autorizare. Toate endpoint-urile sunt publice și accesibile fără autentificare.

Cerința cere (cerințe minime):
- `spring-boot-starter-security` în `pom.xml`
- Autentificare JDBC cu users stocați în baza de date
- Minimum 2 roluri: `ROLE_USER` și `ROLE_ADMIN`
- Protejarea endpoint-urilor bazată pe rol (ex. `DELETE` doar pentru `ADMIN`)
- Pagină de login custom
- Logout funcțional

Cerințe recomandate pentru punctaj maxim:
- Password encoding cu BCrypt (`@Bean PasswordEncoder`)
- CSRF protection (activ by default în Spring Security)
- `Remember Me` functionality

Aceasta este **cea mai mare pierdere de punctaj** din cerințele obligatorii (10%).

---

## Priorități de Implementare (în ordinea impactului)

| Prioritate | Cerință | Punctaj recuperabil |
|---|---|---|
| 1 | **Spring Security** — adaugă dependință, `SecurityFilterChain`, BCrypt, 2 roluri, login page | 10% |
| 2 | **Paginare & Sortare** — `Pageable` în 3 repository-uri + controllere + UI | 6% |
| 3 | **Multi-Environment** — `application-dev.properties` + `application-test.properties` + `@ActiveProfiles` | 5% |
| 4 | **Views Thymeleaf** — template-uri HTML pentru CRUD + pagini eroare (404/500) | ~5-6% |
| 5 | **Integration Tests** — 3 scenarii `@SpringBootTest` end-to-end + service layer unit tests | ~3-4% |
| 6 | **Logging SLF4J** — Logger în servicii + `logback.xml` cu fișier de erori separat | ~2-3% |
| 7 | **Relație @ManyToMany** — ex. `Subscription` ↔ `Tag` sau `User` ↔ `SharedCategory` | ~2% |
| 8 | **Excepții custom** — `UserNotFoundException`, `SubscriptionNotFoundException` etc. | ~1-2% |

---

## Ce a fost bine implementat (puncte forte)

- Logică de business complexă și corectă: actualizare automată buget la modificarea subscripțiilor (`BudgetServiceImpl.addSubscriptionToBudget` / `removeSubscriptionFromBudget`)
- Normalizare costuri anuale → lunare (împărțire la 12 cu `RoundingMode.HALF_UP`) în `FinanceServiceImpl`
- Query-uri JPQL custom relevante în repository-uri (`findExceededBudgets`, `findByUserIdAndDateRange`, `findUpcomingRenewals`)
- Arhitectura în layere este curată și consistentă: Controller → Service (interfață + implementare) → Repository
- Swagger/OpenAPI documentat pe toate endpoint-urile cu `@Operation`, `@ApiResponse`, `@Parameter`
- Bean Validation completă cu mesaje de eroare explicite pe toate entitățile
- `@Transactional(readOnly = true)` aplicat corect pe metodele de citire
- Separarea corectă a `@JsonIgnore` pentru a evita cicluri de serializare
