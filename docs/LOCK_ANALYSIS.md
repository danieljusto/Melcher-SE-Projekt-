# Lock Analysis Implementation Plan

Umfassende Analyse der Codebase zur Identifizierung fehlender Locks und Bewertung bestehender Locks für das WG-Management-System.

---

## Zusammenfassung

Die Codebase hat bereits **grundlegende Transaktionssicherheit** durch `@Transactional`-Annotationen, aber es fehlen **pessimistische Locks** an mehreren kritischen Stellen, die Race Conditions unter konkurrierendem Zugriff ermöglichen.

### Bestehende Locks

| Datei                                                                                                                                                                                | Lock-Typ            | Bewertung |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------------------- | --------- |
| [StandingOrderRepository.java](file:///Users/felixraffel/Melcher_Current/Melcher-SE-Projekt-/src/main/java/com/group_2/repository/finance/StandingOrderRepository.java)              | `PESSIMISTIC_WRITE` | ✅ Korrekt |
| [RoomAssignmentQueueRepository.java](file:///Users/felixraffel/Melcher_Current/Melcher-SE-Projekt-/src/main/java/com/group_2/repository/cleaning/RoomAssignmentQueueRepository.java) | `PESSIMISTIC_WRITE` | ✅ Korrekt |
| [UserRepository.java](file:///Users/felixraffel/Melcher_Current/Melcher-SE-Projekt-/src/main/java/com/group_2/repository/UserRepository.java)                                        | `PESSIMISTIC_WRITE` | ✅ **NEU** |

### Kritische Lücken

1. **Invite-Code-Generierung** → Mögliche Duplikate
2. ~~**Benutzerregistrierung** → Mögliche doppelte E-Mail-Adressen~~ ✅ **IMPLEMENTIERT**
3. **Balance-Berechnung bei WG-Austritt** → Inkonsistente Salden
4. **Queue-Rotation** → Bereits gelockt, aber Verwendung inkonsistent

---

## Bestehende Locks - Detailanalyse

### 1. StandingOrderRepository (✅ Korrekt)

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT s FROM StandingOrder s WHERE s.nextExecution <= :date AND s.isActive = true")
List<StandingOrder> findDueOrdersForUpdate(@Param("date") LocalDate date);
```

**Zweck**: Verhindert doppelte Ausführung von Daueraufträgen, wenn der Scheduler gleichzeitig auf mehreren Instanzen läuft.

**Bewertung**: ✅ **Korrekt implementiert**
- Lock-Level: **Row-Level** (nur fällige Orders werden gelockt)
- Verwendung: Wird korrekt in `processDueStandingOrders()` verwendet

---

### 2. RoomAssignmentQueueRepository (✅ Korrekt)

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT q FROM RoomAssignmentQueue q WHERE q.wg = :wg AND q.room = :room")
List<RoomAssignmentQueue> findByWgAndRoomForUpdate(@Param("wg") WG wg, @Param("room") Room room);
```

**Zweck**: Verhindert Race Conditions bei der Queue-Rotation.

**Bewertung**: ✅ **Korrekt implementiert**
- Lock-Level: **Room-Level** (je Raum-WG-Kombination)
- Verwendung: Wird in `getOrCreateQueueForRoom()` verwendet

> [!WARNING]
> **Inkonsistente Verwendung**: `syncAllQueuesWithMembers()` verwendet `findByWg()` statt der gelockte Version, was Race Conditions bei gleichzeitigen Mitgliedschaftsänderungen ermöglicht.

---

## Fehlende Locks - Kritische Bereiche

### 1. 🔴 Invite-Code-Generierung (WG-Level Lock erforderlich)

**Datei**: [WGService.java](file:///Users/felixraffel/Melcher_Current/Melcher-SE-Projekt-/src/main/java/com/group_2/service/core/WGService.java#L52-L78)

**Problem**: Die Schleife zur Generierung eines einzigartigen Invite-Codes ist anfällig für Race Conditions:

```java
while (wgRepository.existsByInviteCode(wg.getInviteCode())) {
    wg.regenerateInviteCode();
    // ...
}
wg = wgRepository.save(wg);
```

**Race Condition Szenario**:
1. Thread A prüft: Code "ABC123" existiert nicht
2. Thread B prüft: Code "ABC123" existiert nicht
3. Beide speichern → **Duplikat oder Constraint-Violation**

**Empfohlene Lösung**: 

```java
// WGRepository.java - NEU
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT w FROM WG w WHERE w.inviteCode = :code")
Optional<WG> findByInviteCodeForUpdate(@Param("code") String code);
```

**Lock-Level**: **Global für Invite-Codes** (da über alle WGs hinweg einzigartig sein muss)

> [!IMPORTANT]
> Alternative: Unique Index in der Datenbank mit Try-Catch für Constraint-Violations wäre ebenfalls valide.

---

### 2. ✅ Benutzerregistrierung - E-Mail-Prüfung (IMPLEMENTIERT)

**Datei**: [UserService.java](file:///Users/felixraffel/Melcher_Current/Melcher-SE-Projekt-/src/main/java/com/group_2/service/core/UserService.java#L38-L49)

> [!TIP]
> **Status**: ✅ Implementiert am 2026-01-04

**Implementierte Lösung**:

```java
// UserRepository.java - IMPLEMENTIERT
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT u FROM User u WHERE u.email = :email")
Optional<User> findByEmailForUpdate(@Param("email") String email);

// Effiziente findByEmail ohne Lock für authenticate()
Optional<User> findByEmail(String email);
```

```java
// UserService.registerUser() - AKTUALISIERT
@Transactional
public User registerUser(String name, String surname, String email, String password) {
    // Pessimistischer Lock verhindert Race Conditions
    if (userRepository.findByEmailForUpdate(email).isPresent()) {
        throw new RuntimeException("Email already exists");
    }
    // ...
}
```

**Zusätzlich**: `authenticate()` verwendet jetzt `findByEmail()` statt `findAll().stream()` für bessere Performance.

**Lock-Level**: **Global für E-Mails** + Unique Constraint auf DB-Ebene (bereits vorhanden: `@Column(unique = true)`)

---

### 3. 🟡 Balance-Berechnung bei WG-Austritt (WG-Level Lock empfohlen)

**Datei**: [WGService.java](file:///Users/felixraffel/Melcher_Current/Melcher-SE-Projekt-/src/main/java/com/group_2/service/core/WGService.java#L206-L250)

**Problem**: Mehrstufige Operation beim Mitgliederaustritt:

```java
// Step 1: Balance berechnen
transactionService.settleCreditsForDepartingUser(userId);

// Step 2: Mitglied entfernen
wg.removeMitbewohner(userToRemove);

// ... weitere Schritte
```

**Race Condition Szenario**:
1. User A verlässt WG, Balance wird berechnet (User A schuldet User B 10€)
2. Gleichzeitig erstellt User B eine neue Transaktion
3. Balance-Settlement ist unvollständig

**Empfohlene Lösung**: 

```java
// WGRepository.java - NEU
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT w FROM WG w WHERE w.id = :id")
Optional<WG> findByIdForUpdate(@Param("id") Long id);
```

**Lock-Level**: **WG-Level** (sperrt die gesamte WG während Mitgliedschaftsänderungen)

---

### 4. 🟡 Mitgliederbeitritt per Invite-Code (WG-Level Lock erforderlich)

**Datei**: [WGService.java](file:///Users/felixraffel/Melcher_Current/Melcher-SE-Projekt-/src/main/java/com/group_2/service/core/WGService.java#L133-L152)

**Problem**: Check-Then-Act Pattern bei WG-Beitritt:

```java
if (userRepository.existsByIdAndWgId(user.getId(), wg.getId())) {
    throw new RuntimeException("User is already a member...");
}
wg.addMitbewohner(user);
```

**Race Condition Szenario**:
1. Zwei Threads verarbeiten denselben Invite-Code für denselben User
2. Beide prüfen → User ist nicht Mitglied
3. Beide fügen hinzu → Inkonsistenter Zustand

**Empfohlene Lösung**: Verwende WG-Level Lock wie oben beschrieben.

---

### 5. 🟡 Queue-Synchronisation - Inkonsistente Verwendung

**Datei**: [QueueManagementService.java](file:///Users/felixraffel/Melcher_Current/Melcher-SE-Projekt-/src/main/java/com/group_2/service/cleaning/QueueManagementService.java#L92-L102)

**Problem**: `syncAllQueuesWithMembers()` verwendet ungelockte Query:

```java
public void syncAllQueuesWithMembers(WG wg) {
    List<RoomAssignmentQueue> queues = queueRepository.findByWg(wg); // ⚠️ Keine Lock!
    for (RoomAssignmentQueue queue : queues) {
        // ...
    }
}
```

Aber `getOrCreateQueueForRoom()` verwendet die gelockte Version:

```java
public RoomAssignmentQueue getOrCreateQueueForRoom(WG wg, Room room, List<User> members) {
    List<RoomAssignmentQueue> queues = queueRepository.findByWgAndRoomForUpdate(wg, room); // ✅ Mit Lock
    // ...
}
```

**Empfohlene Lösung**:

```java
// RoomAssignmentQueueRepository.java - NEU
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT q FROM RoomAssignmentQueue q WHERE q.wg = :wg")
List<RoomAssignmentQueue> findByWgForUpdate(@Param("wg") WG wg);
```

**Lock-Level**: **WG-Level** (für alle Queues einer WG)

---

### 6. 🟢 Transaktionen (Aktuell ausreichend)

**Datei**: [TransactionService.java](file:///Users/felixraffel/Melcher_Current/Melcher-SE-Projekt-/src/main/java/com/group_2/service/finance/TransactionService.java)

**Bewertung**: `@Transactional` bietet ausreichenden Schutz für:
- `createTransaction()` → Atomare Erstellung von Transaction + Splits
- `updateTransaction()` → Atomares Update
- `deleteTransaction()` → Atomare Löschung

**Empfehlung**: Kein zusätzlicher Lock erforderlich, solange keine Balance-abhängigen Checks durchgeführt werden.

---

### 7. 🟢 Shopping Lists (Aktuell ausreichend)

**Datei**: [ShoppingListService.java](file:///Users/felixraffel/Melcher_Current/Melcher-SE-Projekt-/src/main/java/com/group_2/service/shopping/ShoppingListService.java)

**Bewertung**: `@Transactional` auf Klassenebene bietet ausreichenden Schutz.

**Empfehlung**: Kein zusätzlicher Lock erforderlich.

---

## Implementierungsübersicht

### Repositories - Neue Methoden

| Repository                      | Neue Methode                             | Lock-Level | Lock-Typ            |
| ------------------------------- | ---------------------------------------- | ---------- | ------------------- |
| `WGRepository`                  | `findByIdForUpdate(Long id)`             | WG-Level   | `PESSIMISTIC_WRITE` |
| `WGRepository`                  | `findByInviteCodeForUpdate(String code)` | Global     | `PESSIMISTIC_WRITE` |
| `UserRepository`                | `findByEmailForUpdate(String email)`     | Global     | `PESSIMISTIC_WRITE` |
| `RoomAssignmentQueueRepository` | `findByWgForUpdate(WG wg)`               | WG-Level   | `PESSIMISTIC_WRITE` |

### Services - Änderungen

| Service                  | Methode                        | Änderung                                 |
| ------------------------ | ------------------------------ | ---------------------------------------- |
| `WGService`              | `createWG()`                   | Invite-Code mit Lock prüfen              |
| `WGService`              | `addMitbewohnerByInviteCode()` | WG-Level Lock verwenden                  |
| `WGService`              | `removeMitbewohner()`          | WG-Level Lock verwenden                  |
| `UserService`            | `registerUser()`               | E-Mail mit Lock prüfen ODER Unique Index |
| `QueueManagementService` | `syncAllQueuesWithMembers()`   | Gelockte Query verwenden                 |

---

## Proposed Changes

### Repository Layer

---

#### [MODIFY] [WGRepository.java](file:///Users/felixraffel/Melcher_Current/Melcher-SE-Projekt-/src/main/java/com/group_2/repository/WGRepository.java)

Hinzufügen von pessimistischen Lock-Methoden:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT w FROM WG w WHERE w.id = :id")
Optional<WG> findByIdForUpdate(@Param("id") Long id);

@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT w FROM WG w WHERE w.inviteCode = :code")
Optional<WG> findByInviteCodeForUpdate(@Param("code") String code);
```

---

#### ✅ [DONE] [UserRepository.java](file:///Users/felixraffel/Melcher_Current/Melcher-SE-Projekt-/src/main/java/com/group_2/repository/UserRepository.java)

Implementiert:

render_diffs(file:///Users/felixraffel/Melcher_Current/Melcher-SE-Projekt-/src/main/java/com/group_2/repository/UserRepository.java)

---

#### [MODIFY] [RoomAssignmentQueueRepository.java](file:///Users/felixraffel/Melcher_Current/Melcher-SE-Projekt-/src/main/java/com/group_2/repository/cleaning/RoomAssignmentQueueRepository.java)

Hinzufügen von WG-Level Lock:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT q FROM RoomAssignmentQueue q WHERE q.wg = :wg")
List<RoomAssignmentQueue> findByWgForUpdate(@Param("wg") WG wg);
```

---

### Service Layer

---

#### [MODIFY] [WGService.java](file:///Users/felixraffel/Melcher_Current/Melcher-SE-Projekt-/src/main/java/com/group_2/service/core/WGService.java)

1. `createWG()`: Atomic Check mit Lock für Invite-Code
2. `addMitbewohnerByInviteCode()`: WG mit Lock laden
3. `removeMitbewohner()`: WG mit Lock laden

---

#### ✅ [DONE] [UserService.java](file:///Users/felixraffel/Melcher_Current/Melcher-SE-Projekt-/src/main/java/com/group_2/service/core/UserService.java)

`registerUser()`: Lock-basierte Prüfung implementiert mit `findByEmailForUpdate()`

---

#### [MODIFY] [QueueManagementService.java](file:///Users/felixraffel/Melcher_Current/Melcher-SE-Projekt-/src/main/java/com/group_2/service/cleaning/QueueManagementService.java)

`syncAllQueuesWithMembers()`: `findByWgForUpdate()` statt `findByWg()` verwenden

---

## Verification Plan

### Automatisierte Tests

1. **Unit-Tests** für Lock-Methoden:
   - Verify Lock-Annotation mit Reflection
   - Test Constraint Violations bei Duplikaten

2. **Integration-Tests** für Race Conditions:
   ```java
   // Paralleler WG-Beitritt Test
   @Test
   void testConcurrentWGJoin() {
       ExecutorService executor = Executors.newFixedThreadPool(10);
       // Submit 10 concurrent join requests
       // Assert: Nur ein Erfolg, 9 Exceptions
   }
   ```

### Manuelle Verification

1. Starten der Applikation mit mehreren parallelen Requests
2. Prüfen der Datenbank-Konsistenz nach Stresstests
3. Log-Analyse auf Deadlock-Warnings

---

## Prioritäten

| Priorität  | Bereich                      | Begründung                | Status             |
| ---------- | ---------------------------- | ------------------------- | ------------------ |
| ✅ Erledigt | E-Mail-Eindeutigkeit         | Sicherheitsrelevant       | **IMPLEMENTIERT**  |
| 🔴 Hoch     | Invite-Code-Eindeutigkeit    | Funktionskritisch         | Offen              |
| 🟡 Mittel   | WG-Mitgliedschaftsänderungen | Datenintegrität           | Offen              |
| 🟡 Mittel   | Queue-Synchronisation        | Konsistenz                | Offen              |
| 🟢 Niedrig  | Transactions                 | Bereits adequat geschützt | Nicht erforderlich |
