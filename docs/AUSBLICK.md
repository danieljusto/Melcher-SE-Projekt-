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
- **Aktueller Zustand:** Grundlegende Format-Validierung ist implementiert (`StringUtils.isValidEmail()` prüft auf `xxx@xxx.xxx`-Format). Eine echte Verifizierung der E-Mail-Adresse (ob sie existiert und dem Benutzer gehört) erfolgt nicht.
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

### 3.2 Externe Zahlungsintegration
- Integration mit PayPal, Klarna, oder Banking-APIs
- Automatische Begleichung von Schulden
- QR-Code-Generierung für schnelle Überweisungen

---

## 4. Benutzerverwaltung (Basic stuff was jede app hat)

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
  - Genauere Berechtigungsverwaltung

---

## 5. Putzplan-Erweiterungen

- Integration mit Kalender-Apps (iCal-Export)

---

## 6. Einkaufslisten-Erweiterungen

- Rezepte mit Zutatenlisten
  - Automatisches Hinzufügen von Rezutzutaten zur Einkaufsliste
- Vorrats-Tracking

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

### 7.6 Datenbank-Locking
- **Aktueller Zustand:** Erste Locking-Mechanismen für kritische Operationen (z.B. Benutzerregistrierung) implementiert
- **Vorgeschlagene Erweiterungen:**
  - Erweiterung der Locking-Strategie auf weitere konkurrierende Zugriffe
  - Vermeidung von Race Conditions bei gleichzeitigen Benutzeraktionen
  - Konsistente Datenintegrität bei Mehrbenutzer-Szenarien

### 7.7 Zentrales Server-Deployment
- **Aktueller Zustand:** Lokale Anwendung mit eingebetteter H2-Datenbank
- **Vorgeschlagene Erweiterungen:**
  - Deployment der Spring-Anwendung auf einem zentralen Server
  - H2-Datenbank im Server-Modus für gemeinsamen Zugriff aller WG-Mitglieder
  - Synchronisation der Daten in Echtzeit zwischen allen Clients
  - Zentrale Datensicherung und Backup-Strategien

---

## 8. Multi-Plattform und Skalierbarkeit

### 8.1 Mobile App
- Native iOS/Android-App oder Cross-Platform (Flutter, React Native)
- Push-Notifications auf mobilen Geräten
- Offline-Synchronisation


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

Die Anwendung bietet eine solide Grundlage für die Verwaltung von Wohngemeinschaften. Die identifizierten Verbesserungspotenziale lassen sich in drei Kategorien einteilen:

**Kernfunktionalität:**
- E-Mail-Service für Passwort-Reset, Account-Verifizierung und Benachrichtigungen
- Push-Benachrichtigungen für zeitkritische Ereignisse
- Erweiterte Benutzerverwaltung mit Account-Löschung und Rollenberechtigungen

**Funktionale Erweiterungen:**
- Finanzmodul: Export-Funktionen
- Putzplan: Kalender-Integration
- Einkaufslisten: Rezept-Verwaltung und Vorrats-Tracking

**Technische Qualität:**
- Performance-Optimierung (N+1 Queries, Caching)
- Verbessertes Exception Handling und Sicherheit
- Erhöhung der Testabdeckung und API-Entwicklung

Diese Erweiterungen würden die Benutzerfreundlichkeit und technische Qualität der Anwendung signifikant steigern. Prioritär sollten der E-Mail-Service und die Sicherheitsverbesserungen umgesetzt werden, da diese grundlegende Funktionen für eine produktionsreife Anwendung darstellen.
