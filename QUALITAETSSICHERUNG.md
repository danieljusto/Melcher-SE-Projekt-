# Qualitätssicherung - WG-Management Anwendung

**Stand:** Januar 2026

---

## 1. Übersicht

Die Qualitätssicherung unseres WG-Management-Projekts basiert auf drei Säulen:

| Säule                        | Beschreibung                                                                |
| ---------------------------- | --------------------------------------------------------------------------- |
| **Automatisierte Tests**     | Unit- und Integrationstests für Repository-, Service- und Utility-Schichten |
| **Benutzer-Tests**           | Echte Anwender testen die Applikation und liefern Feedback                  |
| **Architektur & Sicherheit** | Schichtenarchitektur, Datenbankintegrität und Zugriffskontrolle             |

---

## 2. Benutzer-Tests

### 2.1 Durchführung

Wir haben die Anwendung von echten Benutzern testen lassen, um reale Nutzungsszenarien abzudecken:

- **Testgruppe**: Mehrere Personen aus dem Umfeld (potenzielle WG-Bewohner)
- **Testumfang**: Alle Kernfunktionen (Login, WG-Verwaltung, Finanzen, Putzplan, Einkaufsliste)
- **Feedback-Sammlung**: Fehlermeldungen, Usability-Probleme, Verbesserungsvorschläge

### 2.2 Ergebnisse und Umsetzung

| Kategorie                    | Beispiele                                        | Status                |
| ---------------------------- | ------------------------------------------------ | --------------------- |
| **Fehler gefunden**          | Validierungsfehler, Edge-Cases bei Transaktionen | ✅ Behoben             |
| **Usability-Verbesserungen** | Dialog-Layouts, Navigationsfluss                 | ✅ Umgesetzt           |
| **Feature-Vorschläge**       | Erweiterte Sortierung, bessere Fehlermeldungen   | ✅ Teilweise umgesetzt |

**Vorteile der Benutzer-Tests:**
- Realistische Testszenarien, die automatisierte Tests nicht abdecken
- Frühe Erkennung von UX-Problemen
- Validierung der Benutzerfreundlichkeit

---

## 3. Automatisierte Tests

### 3.1 Technologie-Stack

| Komponente            | Technologie  | Zweck                       |
| --------------------- | ------------ | --------------------------- |
| **Test-Framework**    | JUnit 5      | Unit- und Integrationstests |
| **Assertion-Library** | AssertJ      | Lesbare, fluent Assertions  |
| **Test-Datenbank**    | H2 In-Memory | Schnelle, isolierte Tests   |
| **Coverage-Tool**     | JaCoCo       | Testabdeckungsanalyse       |

### 3.2 Test-Kategorien

#### Repository-Tests (Datenschicht)
- `UserRepository`, `WGRepository`, `TransactionRepository`
- `ShoppingListRepository`, `CleaningTaskRepository`, `RoomRepository`

**Geprüfte Aspekte:** CRUD-Operationen, Custom Queries, Entity-Beziehungen

#### Service-Tests (Geschäftslogik)
- `UserService` - Registrierung, Authentifizierung
- `TransactionService` - Finanzverwaltung, Saldenberechnung
- `CleaningScheduleService` - Putzplan-Generierung
- `ShoppingListService` - Einkaufslisten-Verwaltung

**Geprüfte Aspekte:** Passwort-Hashing, Autorisierungsprüfungen, Geschäftsregeln

#### Utility-Tests
- `FormatUtils` - Währungs- und Datumsformatierung
- `MonthlyScheduleUtil` - Terminplanungs-Hilfsfunktionen

### 3.3 Test-Übersicht

| Kategorie        | Testklassen | Status          |
| ---------------- | ----------- | --------------- |
| Repository-Tests | 6           | ✅ Implementiert |
| Service-Tests    | 5           | ✅ Implementiert |
| Utility-Tests    | 2           | ✅ Implementiert |
| Context-Tests    | 1           | ✅ Implementiert |

### 3.4 Entscheidung gegen UI-Tests

Automatisierte UI-Tests (z.B. mit TestFX) wurden bewusst **nicht** implementiert:

| Grund                       | Erklärung                                                                                                        |
| --------------------------- | ---------------------------------------------------------------------------------------------------------------- |
| **Thin Controllers**        | Unsere JavaFX-Controller enthalten keine Geschäftslogik – diese liegt in den Services, die bereits getestet sind |
| **Hoher Wartungsaufwand**   | UI-Tests sind fragil und brechen bei Layout-Änderungen                                                           |
| **Benutzer-Tests ersetzen** | Echte Benutzer testen die UI effektiver als automatisierte Skripte                                               |
| **Kosten-Nutzen**           | Der Implementierungsaufwand übersteigt den Mehrwert                                                              |

**Stattdessen:** Die UI wird durch manuelle Benutzer-Tests validiert.

---

## 4. Architektur-Qualität

### 4.1 Schichtenarchitektur

```
┌─────────────────────────────────────────┐
│           JavaFX UI Layer               │
│    (Controller, FXML, Dialoge)          │
├─────────────────────────────────────────┤
│           Service Layer                 │
│    (Geschäftslogik, Validierung)        │
├─────────────────────────────────────────┤
│         Repository Layer                │
│    (Datenzugriff, JPA Queries)          │
├─────────────────────────────────────────┤
│           Database (H2)                 │
└─────────────────────────────────────────┘
```

**Warum sichert dies Qualität?**
- **Klare Trennung der Verantwortlichkeiten**: Jede Schicht hat eine definierte Aufgabe. Änderungen in einer Schicht beeinflussen andere Schichten minimal.
- **Testbarkeit**: Services können isoliert von der UI getestet werden, Repositories isoliert von der Geschäftslogik.
- **Wartbarkeit**: Fehler lassen sich schneller lokalisieren, da klar ist, in welcher Schicht sie auftreten.
- **Wiederverwendbarkeit**: Services können von verschiedenen Controllern genutzt werden.

### 4.2 DTO-basierte API

Data Transfer Objects (DTOs) entkoppeln die Schichten:

| Domain       | DTOs                                                           |
| ------------ | -------------------------------------------------------------- |
| **Core**     | `UserSummaryDTO`, `WgSummaryDTO`, `UserSessionDTO`             |
| **Finance**  | `TransactionViewDTO`, `BalanceViewDTO`, `StandingOrderViewDTO` |
| **Cleaning** | `CleaningTaskDTO`, `CleaningTaskTemplateDTO`, `RoomDTO`        |

**Qualitätsvorteil:** Die UI arbeitet nur mit DTOs, nicht mit JPA-Entities. Dies verhindert Lazy-Loading-Probleme und ungewollte Datenbankzugriffe aus der UI-Schicht.

---

## 5. Datenintegrität & Sicherheit

### 5.1 Datenbank-Sperren (Locking)

Um Konflikte bei gleichzeitigen Zugriffen zu vermeiden, nutzen wir Datenbank-Locks:

- **Problem**: Zwei Benutzer bearbeiten gleichzeitig denselben Datensatz
- **Lösung**: Pessimistisches/Optimistisches Locking auf Datenbankebene
- **Effekt**: Konflikte werden erkannt und verhindert – keine inkonsistenten Daten

**Beispielszenarien:**
- Parallele Updates am Putzplan → Konflikt wird erkannt

### 5.2 Passwort-Sicherheit

Alle Passwörter werden mit BCrypt gehasht:

```java
@Service
public class PasswordEncryptionService {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    
    public String hashPassword(String password) {
        return encoder.encode(password);
    }
}
```

### 5.3 Autorisierungsprüfungen

Kritische Operationen prüfen die Berechtigung des Benutzers:

- Nur der Ersteller einer Transaktion kann diese löschen/bearbeiten
- WG-Einstellungen können nur von WG-Mitgliedern geändert werden
- Verifiziert durch automatisierte Tests

---

## 6. Test-Ausführung

### 6.1 Befehle

```bash
# Alle Tests ausführen
mvn test

# Mit Coverage-Report
mvn clean test
# Report in: target/site/jacoco/index.html

# Einzelne Testklasse
mvn -Dtest=UserServiceTest test
```

### 6.2 Test-Isolation

| Maßnahme                  | Effekt                                   |
| ------------------------- | ---------------------------------------- |
| `@Transactional`          | Automatischer Rollback nach jedem Test   |
| `@ActiveProfiles("test")` | Isolierte In-Memory-Testdatenbank        |
| `TestDataFactory`         | Konsistente, wiederverwendbare Testdaten |

---

## 7. Logging

### 7.1 Technologie-Stack

| Komponente                  | Technologie               | Zweck                        |
| --------------------------- | ------------------------- | ---------------------------- |
| **Logging-Fassade**         | SLF4J                     | Einheitliche API für Logging |
| **Logging-Implementierung** | Logback (via Spring Boot) | Konfigurierbare Ausgabe      |
| **Pattern**                 | Strukturiertes Logging    | Lesbarkeit und Parsing       |

### 7.2 Logging-Muster

Jede Klasse, die Logging benötigt, verwendet folgendes Standard-Pattern:

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleService {
    private static final Logger log = LoggerFactory.getLogger(ExampleService.class);
    
    public void doSomething() {
        log.info("Operation started for entity {}", entityId);
        // ...
        log.error("Operation failed: {}", e.getMessage(), e);
    }
}
```

**Wichtig:** Wir verwenden Platzhalter `{}` statt String-Konkatenation für bessere Performance und Lesbarkeit.

### 7.3 Log-Level-Konventionen

| Level     | Verwendung                           | Beispiel                                                         |
| --------- | ------------------------------------ | ---------------------------------------------------------------- |
| **ERROR** | Fehler, die zur Laufzeit auftreten   | Datenbankfehler, Validierungsfehler, fehlgeschlagene Operationen |
| **WARN**  | Potenzielle Probleme                 | Veraltete API-Nutzung, unerwartete aber behandelbare Zustände    |
| **INFO**  | Wichtige Geschäftsereignisse         | Erfolgreiche Operationen, Scheduled Tasks, Benutzeraktionen      |
| **DEBUG** | Detaillierte Debugging-Informationen | Methodenaufrufe, Zwischenzustände (nur in Entwicklung)           |

### 7.4 Logging-Bereiche

| Bereich             | Klassen                                              | Geloggte Ereignisse                                  |
| ------------------- | ---------------------------------------------------- | ---------------------------------------------------- |
| **Services**        | `StandingOrderService`                               | Ausführung von Daueraufträgen, Scheduler-Aktivitäten |
| **Controller (UI)** | `TransactionsController`, `SettingsController`, etc. | Fehler bei Benutzerinteraktionen                     |
| **Utilities**       | `StageInitializer`, `FinanceMapper`                  | Initialisierungsfehler, Mapping-Probleme             |

### 7.5 Best Practices

| Praxis                        | Beschreibung                                                                         |
| ----------------------------- | ------------------------------------------------------------------------------------ |
| **Kontextinformationen**      | Log-Nachrichten enthalten relevante IDs (z.B. `userId`, `orderId`)                   |
| **Exception-Logging**         | Bei Fehlern wird die Exception als letztes Argument übergeben: `log.error("msg", e)` |
| **Keine `printStackTrace()`** | Stack Traces werden nur über den Logger ausgegeben                                   |
| **Strukturierte Nachrichten** | Einheitliches Format für einfaches Parsing und Monitoring                            |

### 7.6 Beispiel aus dem Projekt

```java
// StandingOrderService.java - Strukturiertes Error-Logging mit Kontext
log.error("Failed to execute standing order {}: {}", order.getId(), e.getMessage());

// Erfolgreiche Operation mit Kontext-IDs
log.info("Deactivated standing order {} for departing user {}", order.getId(), userId);
```

**Qualitätsvorteil:** 
- Fehler sind nachvollziehbar und enthalten alle relevanten Informationen
- Logs können maschinell ausgewertet werden (z.B. für Alerting)
- Konsistentes Format erleichtert Debugging im Team

---

## 8. Fazit

Die Qualitätssicherung unserer WG-Management-Anwendung kombiniert:

| Maßnahme                 | Abdeckung                                  |
| ------------------------ | ------------------------------------------ |
| **Automatisierte Tests** | Geschäftslogik, Datenzugriff, Utilities    |
| **Benutzer-Tests**       | UI, Usability, reale Nutzungsszenarien     |
| **Architektur**          | Wartbarkeit, Testbarkeit, Entkopplung      |
| **Datenbank-Sperren**    | Datenintegrität bei Mehrbenutzerbetrieb    |
| **Sicherheitsmaßnahmen** | Passwort-Hashing, Autorisierung            |
| **Logging**              | Nachvollziehbarkeit, Debugging, Monitoring |

Diese mehrstufige Qualitätssicherung stellt sicher, dass die Anwendung zuverlässig, sicher und benutzerfreundlich ist.
