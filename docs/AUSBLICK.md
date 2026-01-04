# Ausblick: Zukünftige Erweiterungen und Verbesserungen

Dieses Dokument beschreibt potenzielle Erweiterungen und Verbesserungsmöglichkeiten für die WG-Management-Anwendung, die im Rahmen zukünftiger Entwicklungsphasen umgesetzt werden könnten.

---

## 1. E-Mail-Service

Die Integration eines E-Mail-Services würde die Benutzererfahrung erheblich verbessern und folgende Funktionen ermöglichen:

### 1.1 Passwort-Reset
- **Aktueller Zustand:** Benutzer können ihr Passwort nicht selbstständig zurücksetzen
- **Vorgeschlagene Lösung:**
  - Implementierung eines "Passwort vergessen"-Flows mit Token-basierter E-Mail-Verifizierung
  - Zeitlich begrenzte Reset-Links (z.B. 24 Stunden gültig)
  - Sichere Token-Generierung mittels `SecureRandom`

### 1.2 E-Mail-Verifizierung bei Registrierung
- **Aktueller Zustand:** E-Mail-Adressen werden bei der Registrierung nicht verifiziert (`UserService.registerUser`)
- **Vorgeschlagene Lösung:**
  - Bestätigungs-E-Mail nach Registrierung
  - Account-Aktivierung erst nach E-Mail-Bestätigung
  - Erweiterung des `User`-Models um `emailVerified`-Flag und `verificationToken`

### 1.3 Account-Löschung mit Bestätigung
- **Aktueller Zustand:** Keine Self-Service Account-Löschung implementiert
- **Vorgeschlagene Lösung:**
  - Löschungsanfrage mit E-Mail-Bestätigung
  - Optionale Karenzzeit vor endgültiger Löschung
  - Automatische Datenbereinigung (Transaktionen, Putzpläne, Einkaufslisten)

### 1.4 Benachrichtigungen
- Erinnerungen an ausstehende Putzaufgaben
- Benachrichtigungen bei neuen Transaktionen oder Schuldenänderungen
- WG-Einladungen per E-Mail versenden

**Technologie-Empfehlung:** Spring Boot Starter Mail mit SMTP-Integration (z.B. SendGrid, Mailgun)

---

## 2. Push-Benachrichtigungen / In-App-Notifications

### 2.1 Echtzeit-Benachrichtigungen
- **Aktueller Zustand:** Keine Benachrichtigungsfunktion vorhanden
- **Vorgeschlagene Lösung:**
  - Notification-Center in der Anwendung
  - Benachrichtigungen für:
    - Neue Transaktionen, die den Benutzer betreffen
    - Heute fällige Putzaufgaben
    - Änderungen an geteilten Einkaufslisten
    - WG-bezogene Ereignisse (neuer Mitbewohner, Mitbewohner verlässt WG)

---

## 3. Finanzmodul-Erweiterungen

### 3.1 Ausgaben-Export
- **Vorgeschlagene Features:**
  - Export der Transaktionshistorie als CSV/PDF
  - Monatsübersichten und Jahresberichte
  - Filterung nach Zeitraum, Kategorie oder Mitbewohner

### 3.2 Kategorisierung von Ausgaben
- **Aktueller Zustand:** Transaktionen haben nur eine Beschreibung
- **Vorgeschlagene Lösung:**
  - Kategorien-Feld für Transaktionen (Miete, Einkäufe, Haushalt, etc.)
  - Kategorisierte Ausgabenübersichten
  - Statistiken und Diagramme pro Kategorie

### 3.3 Budgetplanung
- Monatliches WG-Budget festlegen
- Warnungen bei Budgetüberschreitung
- Prognosen basierend auf wiederkehrenden Daueraufträgen

### 3.4 Externe Zahlungsintegration
- Integration mit PayPal, Klarna, oder Banking-APIs
- Automatische Begleichung von Schulden
- QR-Code-Generierung für schnelle Überweisungen

---

## 4. Benutzerverwaltung

### 4.1 Selbständige Profilverwaltung
- **Aktueller Zustand:** Begrenzte Optionen zur Profilbearbeitung in `ProfileController`
- **Vorgeschlagene Erweiterungen:**
  - Profilbild-Upload
  - Passwortänderung mit alter Passwort-Verifizierung
  - Account-Deaktivierung (temporär)
  - Vollständige Account-Löschung mit DSGVO-konformer Datenentfernung

### 4.2 Rollenbasierte Berechtigungen
- **Aktueller Zustand:** Nur Admin-Rolle existiert (`WG.admin`)
- **Vorgeschlagene Erweiterungen:**
  - Differenzierte Rollen (Admin, Moderator, Mitglied)
  - Granulare Berechtigungen (z.B. nur Transaktionen erstellen, nicht löschen)

---

## 5. Putzplan-Erweiterungen

### 5.1 Flexible Wiederholungsmuster
- **Aktueller Zustand:** Putzaufgaben basieren auf wöchentlichen Zyklen
- **Vorgeschlagene Erweiterungen:**
  - Alle 2 Wochen, monatliche, saisonale Aufgaben
  - Flexible Datumsauswahl für einmalige Aufgaben
  - Integration mit Kalender-Apps (iCal-Export)

### 5.2 Aufgabenbewertung und Fairness-Metrik
- Erfassung des Zeitaufwands pro Aufgabe
- Automatische Anpassung der Rotation basierend auf Aufgabenschwere
- Statistiken zur Aufgabenverteilung pro Mitbewohner

### 5.3 Foto-Dokumentation
- Vorher-/Nachher-Fotos für erledigte Aufgaben
- Quality-Checks durch andere Mitbewohner

---

## 6. Einkaufslisten-Erweiterungen

### 6.1 Preiserfassung
- **Aktueller Zustand:** Nur Artikelname und "gekauft"-Status (`ShoppingListItem`)
- **Vorgeschlagene Lösung:**
  - Optionales Preisfeld pro Artikel
  - Automatische Transaktionserstellung beim Abhaken
  - Summe pro Einkaufsliste

### 6.2 Rezept-Integration
- Rezepte mit Zutatenlisten
  - Automatisches Hinzufügen von Rezutzutaten zur Einkaufsliste
- Vorrats-Tracking

### 6.3 Barcode-Scanning
- Produkterkennung via Barcode
- Preis- und Produktinformationen automatisch einfügen

---

## 7. Technische Verbesserungen

### 7.1 Performance-Optimierung
- **Aktueller Zustand:** N+1 Query-Problem in `TransactionService.calculateAllBalances()` (siehe To dos.md)
- **Vorgeschlagene Lösung:**
  - Batch-Fetching und Join-Fetching
  - Caching-Layer für häufige Abfragen
  - LAZY-Fetching konsequent durchsetzen

### 7.2 Exception Handling
- **Aktueller Zustand:** Generische `RuntimeException` für Fehler
- **Vorgeschlagene Lösung:**
  - Custom Exception Hierarchy (`EntityNotFoundException`, `UnauthorizedOperationException`, `ValidationException`)
  - Strukturierte Fehlerrückmeldungen an die UI

### 7.3 Sicherheit
- **Vorgeschlagene Verbesserungen:**
  - `SecureRandom` statt `Random` für Invite-Codes
  - Rate-Limiting für Login-Versuche
  - Session-Timeout und automatischer Logout
  - Thread-Safety für `SessionManager`

### 7.4 Testabdeckung
- **Aktueller Zustand:** 17 Test-Dateien für 88 Quellcode-Dateien
- **Vorgeschlagene Erweiterungen:**
  - Erhöhung der Testabdeckung auf >80%
  - Integration-Tests für kritische Workflows
  - End-to-End UI-Tests

### 7.5 API-Schicht
- **Vorgeschlagene Erweiterung:**
  - REST-API für externe Clients (Mobile Apps, Web-Frontend)
  - API-Versionierung und Dokumentation (OpenAPI/Swagger)

---

## 8. Multi-Plattform und Skalierbarkeit

### 8.1 Mobile App
- Native iOS/Android-App oder Cross-Platform (Flutter, React Native)
- Push-Notifications auf mobilen Geräten
- Offline-Synchronisation

### 8.2 Web-Interface
- Responsive Web-Anwendung
- Zugriff von jedem Gerät ohne Installation

### 8.3 Mehrere WGs pro Benutzer
- **Aktueller Zustand:** Ein Benutzer kann nur einer WG angehören (`User.wg`)
- **Vorgeschlagene Lösung:**
  - Many-to-Many-Beziehung zwischen User und WG
  - WG-Wechsel in der Anwendung

---

## 9. Internationalisierung (i18n)

### 9.1 Mehrsprachigkeit
- **Aktueller Zustand:** Deutsche Benutzeroberfläche
- **Vorgeschlagene Lösung:**
  - Resource Bundles für mehrere Sprachen
  - Sprachauswahl in den Einstellungen
  - Automatische Spracherkennung

### 9.2 Währungsauswahl
- **Aktueller Zustand:** Euro (€) hardcoded
- **Vorgeschlagene Lösung:**
  - Konfigurierbare Währung pro WG
  - Währungsformatierung basierend auf Locale

---

## Zusammenfassung

Die Anwendung bietet bereits eine solide Grundlage für die Verwaltung von Wohngemeinschaften mit Funktionen für Finanzverwaltung, Putzpläne und Einkaufslisten. Die hier vorgeschlagenen Erweiterungen würden die Benutzerfreundlichkeit, Sicherheit und Funktionalität erheblich erweitern:

| Priorität | Bereich | Verbesserung |
|-----------|---------|--------------|
| **Hoch** | E-Mail-Service | Passwort-Reset, E-Mail-Verifizierung |
| **Hoch** | Sicherheit | SecureRandom, Exception Handling |
| **Hoch** | Performance | N+1 Query-Fixes |
| **Mittel** | Benutzerverwaltung | Account-Löschung, Profilerweiterungen |
| **Mittel** | Benachrichtigungen | In-App-Notifications |
| **Mittel** | Finanzmodul | Export, Kategorien |
| **Niedrig** | Multi-Plattform | Mobile App, Web-Interface |
| **Niedrig** | i18n | Mehrsprachigkeit, Währungsauswahl |

Diese Erweiterungen könnten schrittweise in zukünftigen Entwicklungszyklen implementiert werden, wobei der E-Mail-Service und die Sicherheitsverbesserungen die höchste Priorität haben sollten.
