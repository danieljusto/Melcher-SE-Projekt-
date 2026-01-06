# WG Manager - Shared Living Management Application

Eine JavaFX-Desktop-Anwendung zur Verwaltung von Wohngemeinschaften (WGs), entwickelt mit Spring Boot 3 und Java 17.

## Features

### WG-Verwaltung
- Erstellen und Verwalten von Wohngemeinschaften
- Mitgliederverwaltung mit Raumzuteilung
- Benutzerregistrierung und Login mit BCrypt-Passwort-Verschlüsselung

### Putzplan
- Erstellung von Putzaufgaben und wiederholenden Vorlagen
- Flexible Wiederholungsintervalle (täglich, wöchentlich, monatlich)
- Automatische Raum- und Personenzuteilung über Warteschlangen
- Kalenderansicht der Aufgaben
- 
### Einkaufsliste
- Gemeinsame Einkaufslisten
- Hinzufügen und Abhaken von Artikeln

### Finanz
- Transaktionsverwaltung für WG-Ausgaben
- Aufteilung von Kosten zwischen Mitbewohnern (Split-Transaktionen)
- Daueraufträge mit verschiedenen Frequenzen (wöchentlich, monatlich, jährlich)
- Übersicht der Kontostände


## Technologie-Stack

| Komponente              | Technologie                      |
| ----------------------- | -------------------------------- |
| **Frontend**            | JavaFX 21.0.2 mit FXML           |
| **Backend**             | Spring Boot 3.2.0                |
| **Datenbank**           | H2 (embedded, AES-verschlüsselt) |
| **Build-Tool**          | Maven                            |
| **Java-Version**        | Java 17+                         |
| **Passwort-Sicherheit** | Spring Security Crypto (BCrypt)  |
| **Test-Coverage**       | JaCoCo                           |

## Projekt-Struktur

```
src/main/java/com/group_2/
├── Main.java                  # Spring Boot Einstiegspunkt
├── JavaFxApplication.java     # JavaFX Application-Integration
├── dto/                       # Data Transfer Objects
├── model/                     # JPA Entities
│   ├── User.java
│   ├── WG.java
│   ├── cleaning/              # CleaningTask, CleaningTaskTemplate, Room, etc.
│   ├── finance/               # Transaction, StandingOrder, TransactionSplit
│   └── shopping/              # ShoppingList, ShoppingListItem
├── repository/                # Spring Data JPA Repositories
├── service/                   # Business-Logik Services
├── ui/                        # JavaFX Controller
│   ├── core/                  # Login, Signup, Dashboard, Profile
│   ├── cleaning/              # Putzplan-Controller
│   ├── finance/               # Finanz-Controller
│   └── shopping/              # Einkaufs-Controller
└── util/                      # Hilfsfunktionen
```

## Voraussetzungen

- **Java 17** oder höher
- **Maven 3.6+**

## Installation & Ausführung

### 1. Repository klonen

```bash
git clone <repository-url>
cd Melcher-SE-Projekt-
```

### 2. Anwendung starten

```bash
./mvnw javafx:run
```

Alternativ unter Windows:
```bash
mvnw.cmd javafx:run
```

### 3. Demo-Anmeldedaten

| E-Mail           | Passwort  |
| ---------------- | --------- |
| felix@email.com  | felix123  |
| daniel@email.com | daniel123 |
... usw für alle gruppenmitglieder

### 4. Datenbank-Konsole (optional)

Während die Anwendung läuft, ist die H2-Konsole verfügbar unter:
- **URL:** http://localhost:8080/h2-console
- **JDBC URL:** `jdbc:h2:file:./data/wgdb;CIPHER=AES`
- **Username:** `sa`
- **Password:** `password `
--> Leerzeichen am Ende wichtig!

## Tests ausführen

```bash
# Alle Tests ausführen
./mvnw test

# Mit Coverage-Report
./mvnw test jacoco:report
```

Der Coverage-Report befindet sich nach Ausführung unter: `target/site/jacoco/index.html`

## Datenbank-Konfiguration

Die Anwendung nutzt standardmäßig eine lokale H2-Datenbank mit AES-Verschlüsselung. Die Datenbankdateien liegen im `./data/` Verzeichnis.

Für die Nutzung einer Server-Datenbank (H2 Server-Modus oder PostgreSQL) siehe: [docs/Anleitung_DB_Server.md](docs/Anleitung_DB_Server.md)

## Plattform-Unterstützung

Die Anwendung unterstützt automatisch:
- Windows (x64)
- macOS (Intel & Apple Silicon)
- Linux (x64)

Die JavaFX-Dependencies werden automatisch für die entsprechende Plattform heruntergeladen.

## Entwicklung

### Code-Stil
- Englische Klassen- und Methodennamen
- Deutsche Kommentare und Dokumentation erlaubt

### Build-Artefakt erstellen

```bash
./mvnw package
```

Das JAR befindet sich dann unter `target/Melcher_SE_Projekt-1.0-SNAPSHOT.jar`.

## Lizenz

Dieses Projekt ist Teil eines Software-Engineering-Praktikums (Gruppe 2).

---

*Erstellt für das SE-Projekt - Melcher*
