# Controller Audit: Auslagerbare Logik

**Datum:** 2026-01-02  
**Letzte Aktualisierung:** 2026-01-03  
**Analysierte Controller:** 15

---

## ✅ Abgeschlossene Refactorings

### CleaningScheduleController (776 → 753 Zeilen)

| Ursprüngliche Zeilen | Methode               | Änderung                                                                                                     |
| -------------------- | --------------------- | ------------------------------------------------------------------------------------------------------------ |
| 83-96                | `updateWeekDisplay()` | ✅ Ausgelagert nach `FormatUtils.formatWeekTitle()` und `FormatUtils.formatWeekDateRange()`                   |
| 436-443              | `updateStats()`       | ✅ Statistik-Berechnung ausgelagert nach `CleaningScheduleService.calculateWeekStats()`, nutzt `WeekStatsDTO` |

**Neue Komponenten:**
- `FormatUtils.formatWeekTitle(LocalDate)` - Formatiert Wochennummer und Jahr
- `FormatUtils.formatWeekDateRange(LocalDate)` - Formatiert Datumsbereich
- `FormatUtils.truncate(String, int)` - Kürzt Strings mit Ellipsen
- `FormatUtils.formatDayNameWithNumber(LocalDate)` - Formatiert Tag mit Nummer
- `WeekStatsDTO` - Record für Wochen-Statistiken (totalTasks, completedTasks, myTasks)
- `CleaningScheduleService.calculateWeekStats()` - Berechnet Statistiken im Service

---

### TemplateEditorController (661 → 602 Zeilen)

| Ursprüngliche Zeilen | Methode                                         | Änderung                                                                    |
| -------------------- | ----------------------------------------------- | --------------------------------------------------------------------------- |
| 82-105               | `WorkingTemplate` (innere Klasse)               | ✅ Ersetzt durch `WorkingTemplateDTO` mit Datums-Berechnungslogik            |
| 426-444              | `resolveBaseDate()`, `resolveLastDayBaseDate()` | ✅ Ausgelagert nach `MonthlyScheduleUtil`                                    |
| 446-449              | `getBaseDateForTemplate()`                      | ✅ Ausgelagert nach `WorkingTemplateDTO.calculateBaseDate()` und Util-Klasse |

**Neue Komponenten:**
- `WorkingTemplateDTO` - Mutable DTO für Template-Editor mit:
  - `calculateBaseDate(LocalDate)` - Berechnet Basisdatum aus Wochenstart
  - `updateFromBaseDate(LocalDate)` - Aktualisiert dayOfWeek und baseWeekStart
- `MonthlyScheduleUtil` - Utility-Klasse mit:
  - `resolveMonthlyDate()` - Löst monatliche Termine auf
  - `getEffectiveDay()` - Behandelt Februar und kurze Monate
  - `calculateBaseWeekStart()` - Berechnet Montag der Woche
  - `resolveBaseDate()` - Löst Basisdatum basierend auf Interval
  - `resolveLastDayBaseDate()` - Findet Tag 31 eines Monats
  - `isDateRequired()` - Prüft ob DatePicker benötigt wird

---

### CleaningTemplateService (erweitert auf 304 Zeilen)

Zentrale Template-Verwaltung hinzugefügt:
- `shouldGenerateTaskThisWeek()` - Recurrence-Berechnung für alle Intervalle
- `resolveDueDateForWeek()` - Datums-Auflösung inkl. monatlicher Logik
- `syncCurrentWeekWithTemplate()` - Template-Synchronisation mit Aufgaben
- Unterstützung für `manualOverride`-Flag zur Erhaltung manueller Tasks

---

## 🔴 Kritische Fälle (noch offen)

### ~~1. CleaningScheduleController (753 → 747 Zeilen)~~ ✅ ABGESCHLOSSEN

| Zeilen  | Methode            | Problem                                                             | Änderung                                                      |
| ------- | ------------------ | ------------------------------------------------------------------- | ------------------------------------------------------------- |
| 106-160 | `createDayCell()`  | Logik zum Bestimmen welche Tasks an welchem Tag fällig (Z. 150-158) | ✅ Ausgelagert nach `CleaningScheduleService.getTasksForDay()` |
| 162-205 | `createTaskPill()` | "myTask"-Bestimmung (Z. 168)                                        | ✅ Bereits über `CleaningTaskDTO.isAssignedTo()` gelöst        |

**Neue Komponenten:**
- `CleaningScheduleService.getTasksForDay(weekTasks, day)` - Filtert Tasks nach Tag
- `StringUtils.getInitial(String)` - Zentralisierte Initial-Extraktion
- `StringUtils.pluralize(count, singular, plural)` - Pluralisierung
- `CleaningTaskDTO.getAssigneeInitial()` nutzt jetzt `StringUtils.getInitial()`

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

### ~~7. SettingsController (456 → 452 Zeilen)~~ ✅ TEILWEISE

| Zeilen  | Methode                | Problem                                | Änderung                                 |
| ------- | ---------------------- | -------------------------------------- | ---------------------------------------- |
| 451-454 | `isCurrentUserAdmin()` | Permission-Check Logik                 | 🟡 Offen - `PermissionService` oder DTO   |
| 126-139 | `loadRooms()`          | Pluralisierung ("1 room" vs "2 rooms") | ✅ Nutzt jetzt `StringUtils.pluralize()`  |
| 141-152 | `loadMembers()`        | Dieselbe Pluralisierung                | ✅ Nutzt jetzt `StringUtils.pluralize()`  |
| 204-206 | Avatar-Initial         | `substring(0,1).toUpperCase()`         | ✅ Nutzt jetzt `StringUtils.getInitial()` |

---

### ~~8. ProfileController (277 → 275 Zeilen)~~ ✅ TEILWEISE

| Zeilen  | Methode             | Problem                                                               | Änderung                                 |
| ------- | ------------------- | --------------------------------------------------------------------- | ---------------------------------------- |
| 89-91   | Initial-Berechnung  | `name.substring(0,1).toUpperCase()` - kommt in **5 Controllern** vor! | ✅ Nutzt jetzt `StringUtils.getInitial()` |
| 201-203 | `handleEditEmail()` | Email-Validierung (`email.contains("@")`)                             | 🟡 Offen - `ValidationService`            |

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

| Muster                                               | Häufigkeit | Status                                             | Betroffene Controller                                                       |
| ---------------------------------------------------- | ---------- | -------------------------------------------------- | --------------------------------------------------------------------------- |
| Initiale-Berechnung (`substring(0,1).toUpperCase()`) | ~~5x~~ 0x  | ✅ Erledigt (`StringUtils.getInitial()`)            | ~~MainScreen, Profile, Settings, Cleaning, Shopping~~                       |
| Pluralisierung ("1 item" vs "2 items")               | ~~4x~~ 2x  | ✅ Teilweise (`StringUtils.pluralize()`)            | ~~Settings, Transactions~~, Shopping (?), Cleaning (?)                      |
| Split-Validierung (Percentage/Amount Summe prüfen)   | ~~3x~~ 0x  | ✅ Erledigt (`SplitValidationHelper`)               | ~~TransactionDialog, TransactionHistory, StandingOrders~~                   |
| Datumsformatierung/Berechnung                        | ~~4x~~ 2x  | ✅ Teilweise (`FormatUtils`, `MonthlyScheduleUtil`) | ~~CleaningSchedule, TemplateEditor~~, TransactionHistory, TransactionDialog |
| Permission-Prüfung im Controller                     | 3x         | 🟡 Offen                                            | Settings, Profile, ShoppingList                                             |

---

## 🎯 Empfohlene Refactoring-Priorität

### ~~Priorität 1: `DateFormatterService`/`MonthlyScheduleUtil` erstellen~~ ✅ ERLEDIGT
- ✅ `FormatUtils` mit Datums- und Währungsformatierung erstellt
- ✅ `MonthlyScheduleUtil` mit monatlicher Terminauflösung erstellt
- ✅ `WeekStatsDTO` für Statistik-Daten erstellt
- ✅ `WorkingTemplateDTO` für Template-Editor erstellt

### ~~Priorität 1: `SplitValidationHelper` erstellen~~ ✅ ERLEDIGT
- ✅ `SplitValidationHelper` Utility-Klasse erstellt mit:
  - `validatePercentageSplit()` - Validiert ob Prozentsumme = 100%
  - `validateAmountSplit()` - Validiert ob Betragssumme = Gesamtbetrag
  - `applyValidationStyling()` - Wendet CSS-Klassen basierend auf Status an
  - `applySuccessStyling()`, `applyErrorStyling()`, `applyMutedStyling()` - Direkte Styling-Methoden
  - `parseAmount()` - Einheitliches Parsing mit Komma-Unterstützung
  - `calculateEqualSplit()`, `formatEqualSplitMessage()` - Helper für Gleichverteilung
- ✅ Refactored: `TransactionDialogController`, `TransactionHistoryController`, `StandingOrdersDialogController`
- **~120 Zeilen duplizierten Code entfernt**

### ~~Priorität 2: `StringUtils.getInitial()` hinzufügen~~ ✅ ERLEDIGT
- ✅ `StringUtils` Utility-Klasse erstellt
- ✅ `getInitial(String)` und `getInitial(String, String fallback)` implementiert
- ✅ `pluralize(count, singular, plural)` und `pluralizeWord()` implementiert
- ✅ `CleaningTaskDTO.getAssigneeInitial()` nutzt jetzt `StringUtils.getInitial()`

### ~~Priorität 3: StringUtils.pluralize() in Controllern anwenden~~ ✅ TEILWEISE ERLEDIGT
- ✅ `SettingsController` - `loadRooms()` und `loadMembers()` nutzen `StringUtils.pluralize()`
- ✅ `TransactionDialogController` - `updateStep2Summary()` nutzt `StringUtils.pluralize()`
- 2x verbleibend: Shopping, Cleaning (falls noch vorhanden)

### ~~Priorität 4: `CleaningScheduleController` weiter aufspalten~~ ✅ ERLEDIGT
- ✅ `getTasksForDay()` in Service ausgelagert
- ✅ `isAssignedTo()` bereits im DTO
- ✅ Stats-Berechnung und Formatierung ausgelagert

### ~~Priorität 5: `TransactionDialogState` erweitern~~ 🟡 OPTIONAL
- Split-Validierung jetzt in `SplitValidationHelper` (UI-fokussiert)
- State-Klasse könnte für Business-Validierung erweitert werden

---

## Nächste Schritte

- [x] `FormatUtils` mit Datums- und Währungsformatierung ✅
- [x] `MonthlyScheduleUtil` für monatliche Terminberechnung ✅
- [x] `WorkingTemplateDTO` für Template-Editor ✅
- [x] `WeekStatsDTO` und Service-Methode für Statistiken ✅
- [x] `CleaningTemplateService` erweitern (manualOverride-Support) ✅
- [x] `StringUtils` mit `getInitial()` und `pluralize()` ✅
- [x] `CleaningScheduleController` vollständig refactored ✅
- [x] `CleaningScheduleService.getTasksForDay()` für Tages-Filterung ✅
- [x] `StringUtils.getInitial()` in allen Controllern angewendet ✅
  - MainScreenController, ProfileController, SettingsController, ShoppingListController, CleaningTaskDTO
- [x] `StringUtils.pluralize()` in SettingsController und TransactionDialogController angewendet ✅
- [x] `SplitValidationHelper` implementiert ✅
  - TransactionDialogController, TransactionHistoryController, StandingOrdersDialogController
- [ ] (Optional) `TransactionDialogState` um Business-Validierung erweitern
- [ ] (Optional) `PermissionService` für einheitliche Berechtigungsprüfungen
