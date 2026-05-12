# Finance & Subscription Tracker: Functional Requirements

## Platform Overview

The **Personal Finance & Subscription Tracker** is a Spring Boot application designed to help individuals and families manage recurring subscriptions, track expenses by category, and monitor their financial health. The platform enables users to centralize subscription management across multiple services (e.g., Netflix, Spotify, cloud storage), set spending budgets per category, share subscription costs with other users, and gain insights into their disposable income and upcoming payment obligations. Through an intuitive API-driven architecture, users can maintain a complete record of their payment methods, transaction history, and financial metrics to achieve better spending awareness and control.

---

**1. User Profile & Income Management**
*   **FR 1.1:** The system shall allow users to register and manage their profile (name, universally unique email, and monthly income).
*   **FR 1.2:** The system must use the user's monthly income as the baseline to calculate their financial health and available disposable income.

**2. Subscription Lifecycle Management**
*   **FR 2.1:** Users shall be able to create, read, update, and delete (CRUD) recurring subscriptions (e.g., Netflix, Internet).
*   **FR 2.2:** Each subscription must track core pricing details, billing frequency (Monthly/Yearly), and the exact upcoming renewal date.
*   **FR 2.3:** The system must normalize yearly subscription costs into monthly equivalents for accurate budget calculation.

**3. Subscription Sharing (Many-to-Many Cost Split)**
*   **FR 3.1:** The system shall allow a single subscription to be shared among multiple users (e.g., family members or flatmates).
*   **FR 3.2:** Users must be able to specify their exact contribution to a shared subscription using either a **percentage share** (e.g., 50%) or a **fixed monetary amount**.
*   **FR 3.3:** The system must accurately distribute the overall subscription cost to the respective users' personal budgets based on their defined share.
*   **FR 3.4:** Sharing a subscription with another user will create a pending share request that the recipient can either accept or decline before the share becomes active.
*   **FR 3.5:** The owner of a subscription should be able to revoke a pending share request before the recipient accepts or declines it.
*   **FR 3.6:** A recipient who has accepted a shared subscription should be able to leave the share, removing their participation from that subscription.
*   **FR 3.7:** The system shall prevent duplicate active shares and duplicate pending share requests for the same subscription and recipient.
*   **FR 3.8:** Percentage share and fixed monetary amount are mutually exclusive for a single share or share request. If both values are submitted, the system stores the fixed monetary amount and clears the percentage value.

**4. Category & Budget Tracking**
*   **FR 4.1:** Users shall be able to create custom categories (e.g., "Entertainment", "Utilities") to logically group their subscriptions.
*   **FR 4.2:** The system shall allow users to set a maximum spending limit (Budget) for any specific category.
*   **FR 4.3:** The system must automatically update the "current spending" within a budget whenever a subscription in that category is added, removed, or has its cost modified.
*   **FR 4.4:** When a user accepts a shared subscription request, the system shall copy the subscription's category name and description into the recipient's profile if the recipient does not already have a category with that name.

**5. Payment Methods & Transactions**
*   **FR 5.1:** Users will be able to register various specific payment methods (e.g., Credit Card, PayPal, Custom Bank Transfer) linked securely to their profile.
*   **FR 5.2:** Each subscription shall be linked to a single primary payment method to track where the money is physically withdrawn from.
*   **FR 5.3:** The system shall maintain an immutable transaction registry that logs raw historical payments (amount and transaction date) tied to specific subscriptions.

**6. Financial Dashboard & Analytics**
*   **FR 6.1:** The system can calculate and display the user's **Disposable Income** (Monthly Income - Total Monthly Subscription Liabilities).
*   **FR 6.2:** The system should identify and display "Upcoming Renewals" (subscriptions due for payment within the next 7 days).
*   **FR 6.3:** The system should provide an aggregated summary of expected costs broken down by category.

---

**7. Contact & Support Ticket System**
*   **FR 7.1:** Any authenticated user should be able to submit a support ticket consisting of a subject and a free-form message.
*   **FR 7.2:** Ticket visibility is strictly per-user: a regular user may only view tickets they personally submitted. Other users' tickets are inaccessible to them regardless of authentication status.
*   **FR 7.3:** A submitted ticket starts in status `OPEN`. Admins may transition it to `IN_PROGRESS` and then to `RESOLVED` (or back to `IN_PROGRESS` to reopen). Status changes require `ROLE_ADMIN`.
*   **FR 7.4:** When resolving (or updating) a ticket, an admin may optionally attach a **resolution note** visible to the ticket owner.
*   **FR 7.5:** Both the ticket owner and any admin may post **replies** to a ticket, forming a threaded conversation. The owner sees the full reply thread on their own ticket; admins see all reply threads on all tickets.
*   **FR 7.6:** Admins can have access to a dedicated `/admin/tickets` page listing all tickets across all users, with the ability to filter by status (`OPEN`, `IN_PROGRESS`, `RESOLVED`).
*   **FR 7.7:** All ticket and reply read/write endpoints are protected by role:
    *   `/contact/**` — requires `ROLE_USER` (any authenticated user)
    *   `/admin/tickets/**` — requires `ROLE_ADMIN`
