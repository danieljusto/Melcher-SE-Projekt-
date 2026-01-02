# Controller Audit: Auslagerbare Logik

**Datum:** 2026-01-02  
**Analysierte Controller:** 15

---

## 🔴 Kritische Fälle (sofort auslagern empfohlen)

### 1. CleaningScheduleController (776 Zeilen)

| Zeilen  | Methode               | Problem                                                             | Empfehlung                          |
| ------- | --------------------- | ------------------------------------------------------------------- | ----------------------------------- |
| 83-96   | `updateWeekDisplay()` | Wochennummer-Berechnung, Datumsformatierung                         | `DateFormatterService` oder Utility |
| 117-171 | `createDayCell()`     | Logik zum Bestimmen welche Tasks an welchem Tag fällig (Z. 159-168) | In `CleaningScheduleService`        |
| 173-216 | `createTaskPill()`    | "myTask"-Bestimmung (Z. 179)                                        | In DTO oder Service                 |
| 436-443 | `updateStats()`       | Stream-Berechnung für Statistiken                                   | In Service                          |

---

### 2. TemplateEditorController (661 Zeilen)

| Zeilen  | Methode                                         | Problem                                     | Empfehlung                         |
| ------- | ----------------------------------------------- | ------------------------------------------- | ---------------------------------- |
| 82-105  | `WorkingTemplate` (innere Klasse)               | Datums-Berechnungen mit `TemporalAdjusters` | In Service                         |
| 426-444 | `resolveBaseDate()`, `resolveLastDayBaseDate()` | Komplexe "letzter Tag des Monats"-Logik     | `MonthlyScheduleUtil` oder Service |
| 446-449 | `getBaseDateForTemplate()`                      | Basiswoche-Berechnung                       | In Service                         |

---

### 3. TransactionsController (614 Zeilen)

| Zeilen  | Methode                  | Problem                                          | Empfehlung          |
| ------- | ------------------------ | ------------------------------------------------ | ------------------- |
| 163-187 | `updateBalanceDisplay()` | Card-Farbe basierend auf Balance-Klassifizierung | In Service oder DTO |
| 313-338 | `showSettlementDialog()` | Filtern verfügbarer Credits für Transfer         | In Service          |

---

### 4. TransactionHistoryController (756 Zeilen)

| Zeilen  | Methode                       | Problem                                               | Empfehlung                 |
| ------- | ----------------------------- | ----------------------------------------------------- | -------------------------- |
| 264-322 | `populateFilters()`           | Jahreserstellung, Sortierung, Member-Lookup           | In Service                 |
| 324-383 | `applyFilters()`              | Komplexe Filterlogik (Year/Month/Payer/Debtor/Search) | `TransactionFilterService` |
| 493-601 | `showEditTransactionDialog()` | ~110 Zeilen Split-Berechnung im Dialog                | `SplitCalculationService`  |

---

### 5. TransactionDialogController (981 Zeilen) ⚠️ **Größter Controller**

| Zeilen  | Methode                                                                                 | Problem                                       | Empfehlung                                               |
| ------- | --------------------------------------------------------------------------------------- | --------------------------------------------- | -------------------------------------------------------- |
| 400-452 | `buildStandingOrderDescription()`                                                       | Komplexe String-Aufbau mit Datumsberechnungen | `StandingOrderService` oder `FormatUtils`                |
| 887-962 | `updateEqualSplitSummary()`, `updatePercentageSummary()`, `updateCustomAmountSummary()` | Drei ähnliche Validierungsmethoden            | `SplitValidationHelper` oder in `TransactionDialogState` |

---

### 6. StandingOrdersDialogController (730 Zeilen)

| Zeilen  | Methode                       | Problem                                                          | Empfehlung                 |
| ------- | ----------------------------- | ---------------------------------------------------------------- | -------------------------- |
| 169-187 | `formatFrequency()`           | Switch-Statement für Frequency-Formatierung                      | In Enum oder `FormatUtils` |
| 189-201 | `parseDebtorNames()`          | Stream-basierter String-Aufbau                                   | In Mapper                  |
| 399-573 | `rebuildSplitFields` Runnable | ~175 Zeilen Split-Validierung (identisch zu TransactionHistory!) | `SplitValidationService`   |

---

## 🟡 Mittlere Fälle (sollten ausgelagert werden)

### 7. SettingsController (456 Zeilen)

| Zeilen  | Methode                | Problem                                | Empfehlung                   |
| ------- | ---------------------- | -------------------------------------- | ---------------------------- |
| 451-454 | `isCurrentUserAdmin()` | Permission-Check Logik                 | `PermissionService` oder DTO |
| 126-139 | `loadRooms()`          | Pluralisierung ("1 room" vs "2 rooms") | `FormatUtils.pluralize()`    |
| 141-152 | `loadMembers()`        | Dieselbe Pluralisierung                | `FormatUtils.pluralize()`    |

---

### 8. ProfileController (267 Zeilen)

| Zeilen  | Methode             | Problem                                                               | Empfehlung                 |
| ------- | ------------------- | --------------------------------------------------------------------- | -------------------------- |
| 89-91   | Initial-Berechnung  | `name.substring(0,1).toUpperCase()` - kommt in **5 Controllern** vor! | `StringUtils.getInitial()` |
| 201-203 | `handleEditEmail()` | Email-Validierung (`email.contains("@")`)                             | `ValidationService`        |

---

### 9. NoWgController (224 Zeilen)

| Zeilen  | Methode            | Problem                                     | Empfehlung                               |
| ------- | ------------------ | ------------------------------------------- | ---------------------------------------- |
| 155-164 | `handleCreateWg()` | Schleife die Räume erstellt und IDs sammelt | Transaction-Handling komplett in Service |

---

## 🟢 Saubere Controller (kein sofortiger Handlungsbedarf)

| Controller               | Zeilen | Bemerkung                                |
| ------------------------ | ------ | ---------------------------------------- |
| `LoginController`        | 77     | ✓ Sauber                                 |
| `SignUpController`       | 79     | ✓ Sauber                                 |
| `MainScreenController`   | 115    | ✓ Sauber                                 |
| `NavbarController`       | 71     | ✓ Sauber                                 |
| `ShoppingListController` | 525    | Relativ sauber, delegiert gut an Service |

---

## 📊 Wiederkehrende Muster

| Muster                                               | Häufigkeit | Betroffene Controller                                                   |
| ---------------------------------------------------- | ---------- | ----------------------------------------------------------------------- |
| Initiale-Berechnung (`substring(0,1).toUpperCase()`) | 5x         | MainScreen, Profile, Settings, Cleaning, Template                       |
| Pluralisierung ("1 item" vs "2 items")               | 4x         | Settings, Shopping, Cleaning, Transactions                              |
| Split-Validierung (Percentage/Amount Summe prüfen)   | 3x         | TransactionDialog, TransactionHistory, StandingOrders                   |
| Datumsformatierung/Berechnung                        | 4x         | CleaningSchedule, TemplateEditor, TransactionHistory, TransactionDialog |
| Permission-Prüfung im Controller                     | 3x         | Settings, Profile, ShoppingList                                         |

---

## 🎯 Empfohlene Refactoring-Priorität

### Priorität 1: `SplitValidationService` erstellen
- ~175 Zeilen Code-Duplikation in 3 Controllern entfernen
- Betroffen: `TransactionDialogController`, `TransactionHistoryController`, `StandingOrdersDialogController`

### Priorität 2: `StringUtils.getInitial()` hinzufügen
- 5x verwendet in verschiedenen Controllern
- Einfache Utility-Methode

### Priorität 3: `FormatUtils.pluralize(count, singular, plural)` hinzufügen
- 4x verwendet
- Vereinheitlicht Textausgabe

### Priorität 4: `CleaningScheduleController` aufspalten
- Separate `CleaningCalendarBuilder`-Klasse für UI-Aufbau erstellen
- Controller auf reine Koordination reduzieren

### Priorität 5: `TransactionDialogState` erweitern
- Split-Validierung dorthin verlagern
- State-Klasse bereits vorhanden, nur erweitern

---

## Nächste Schritte

- [ ] `SplitValidationService` implementieren
- [ ] `StringUtils.getInitial()` erstellen
- [ ] `FormatUtils.pluralize()` erstellen
- [ ] `CleaningCalendarBuilder` extrahieren
- [ ] `TransactionDialogState` um Validierung erweitern
