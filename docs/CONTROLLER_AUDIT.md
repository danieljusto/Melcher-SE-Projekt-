# Controller Audit: Offene Verbesserungen

**Letzte Aktualisierung:** 2026-01-03  

---

## 🟡 Offene Fälle (Optional)

### 1. TransactionsController (614 Zeilen)

| Zeilen  | Methode                  | Problem                                          | Empfehlung          |
| ------- | ------------------------ | ------------------------------------------------ | ------------------- |
| 163-187 | `updateBalanceDisplay()` | Card-Farbe basierend auf Balance-Klassifizierung | In Service oder DTO |
| 313-338 | `showSettlementDialog()` | Filtern verfügbarer Credits für Transfer         | In Service          |

### 2. TransactionHistoryController (756 Zeilen)

| Zeilen  | Methode             | Problem                                               | Empfehlung                 |
| ------- | ------------------- | ----------------------------------------------------- | -------------------------- |
| 264-322 | `populateFilters()` | Jahreserstellung, Sortierung, Member-Lookup           | In Service                 |
| 324-383 | `applyFilters()`    | Komplexe Filterlogik (Year/Month/Payer/Debtor/Search) | `TransactionFilterService` |

### 3. TransactionDialogController (981 Zeilen) ⚠️ **Größter Controller**

| Zeilen  | Methode                           | Problem                                       | Empfehlung                                |
| ------- | --------------------------------- | --------------------------------------------- | ----------------------------------------- |
| 400-452 | `buildStandingOrderDescription()` | Komplexe String-Aufbau mit Datumsberechnungen | `StandingOrderService` oder `FormatUtils` |

### 4. StandingOrdersDialogController (730 Zeilen)

| Zeilen  | Methode              | Problem                                     | Empfehlung                 |
| ------- | -------------------- | ------------------------------------------- | -------------------------- |
| 169-187 | `formatFrequency()`  | Switch-Statement für Frequency-Formatierung | In Enum oder `FormatUtils` |
| 189-201 | `parseDebtorNames()` | Stream-basierter String-Aufbau              | In Mapper                  |

### 5. NoWgController (224 Zeilen)

| Zeilen  | Methode            | Problem                                     | Empfehlung                               |
| ------- | ------------------ | ------------------------------------------- | ---------------------------------------- |
| 155-164 | `handleCreateWg()` | Schleife die Räume erstellt und IDs sammelt | Transaction-Handling komplett in Service |

### 6. SettingsController / ProfileController

| Controller         | Problem                                   | Empfehlung                   |
| ------------------ | ----------------------------------------- | ---------------------------- |
| SettingsController | `isCurrentUserAdmin()` Permission-Check   | `PermissionService` oder DTO |
| ProfileController  | Email-Validierung (`email.contains("@")`) | `ValidationService`          |

---

## 🟢 Saubere Controller (kein Handlungsbedarf)

| Controller                   | Zeilen |
| ---------------------------- | ------ |
| `LoginController`            | 77     |
| `SignUpController`           | 79     |
| `MainScreenController`       | 115    |
| `NavbarController`           | 71     |
| `ShoppingListController`     | 525    |
| `CleaningScheduleController` | 747    |
| `TemplateEditorController`   | 602    |

---

## 🎯 Optionale Verbesserungen

1. **`PermissionService`** - Einheitliche Berechtigungsprüfungen
2. **`ValidationService`** - Email-Validierung und Input-Prüfungen
3. **`TransactionFilterService`** - Filterlogik aus TransactionHistoryController
4. **`TransactionDialogState`** erweitern - Business-Validierung

---

*Alle kritischen Refactorings wurden abgeschlossen. Die verbleibenden Punkte sind optional.*
