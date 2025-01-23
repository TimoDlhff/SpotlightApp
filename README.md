# SpotlightApp🎤

Spotlight ist eine Android-App für bessere Präsentationen. Mit Echtzeit-Spracherkennung und dezenten Vibrationen hilft die App dabei, professioneller und überzeugender zu präsentieren.

## Features ✨

### 1. Smarte Füllwort-Erkennung
- Erkennt Füllwörter wie "ähm", "halt" oder "sozusagen" in Echtzeit
- Gibt dezentes Vibrations-Feedback
- Macht unbewusste Sprechgewohnheiten bewusst
- Eigene Füllwörter können einfach hinzugefügt werden

### 2. Sprechgeschwindigkeits-Check
- Behält das Sprechtempo im Auge
- Warnt dezent bei zu schnellem oder zu langsamem Sprechen
- Hilft dabei, das perfekte Tempo zu finden

### 3. Flexibles Feedback
- Frei wählbar: Smartphone oder Smartwatch für Vibrationen
- Dezente Benachrichtigungen, die nicht aus dem Flow bringen
- Komplett anpassbar an persönliche Vorlieben

### 4. Detaillierte Stats
- Analysiert die Präsentation
- Zeigt an, welche Füllwörter wie oft vorkommen
- Visualisiert das Sprechtempo
- Macht Fortschritte über Zeit sichtbar

### 5. Zwei Modi
- Präsentationsmodus: Dezentes Feedback für den Ernstfall - vibriert nicht bei jedem Fehler, um nicht zu stören
- Trainingsmodus: Intensives Feedback zum Üben - meldet jeden Fehler sofort

### 6. Smartwatch Support
- Wear OS App muss auf der Smartwatch installiert und geöffnet sein
- In VocalWave auf dem Handy "Smartwatch" als Feedback-Methode auswählen
- Perfekt für dezentes Feedback während der Präsentation

## Installation 🚀

### 1. Code holen
```bash
git clone https://github.com/TimoDlhff/VocalWaveApp.git
```

### 2. In Android Studio öffnen
1. Android Studio starten
2. "Open an existing Android Studio project" wählen
3. Zum VocalWaveApp-Ordner navigieren und öffnen

### 3. Projekt einrichten
1. Kurz warten bis Android Studio alles geladen hat
2. Auf das Elefanten-Symbol klicken ("Sync Project with Gradle Files")
3. Synchronisierung abwarten

### 4. App installieren
1. Android-Gerät per USB anschließen
2. Entwicklermodus und USB-Debugging auf dem Gerät aktivieren
3. Gerät in Android Studio auswählen
4. Auf den grünen Play-Button klicken

## Technik-Details 🔧

Die App nutzt VOSK für die Spracherkennung. Das deutsche Sprachmodell (ca. 88 MB) ist direkt dabei und wird automatisch mit installiert.

### Andere Sprachen nutzen

Die App kann auch andere Sprachen lernen! So geht's:

1. VOSK-Modell besorgen:
   - Ab zur [VOSK-Modelle-Seite](https://alphacephei.com/vosk/models)
   - Passendes Modell aussuchen (z.B. `vosk-model-small-en-us-0.15` für Englisch)
   - ZIP-Datei runterladen

2. Modell einbauen:
   - ZIP entpacken
   - Ordner nach `models/src/main/assets/` kopieren
   - Ordner sollte so heißen: `vosk-model-small-XX-YY-0.15`
     - XX = Sprache (z.B. 'en' für Englisch)
     - YY = Region (z.B. 'us' für USA)

3. Code anpassen:
   - `app/src/main/java/org/vosk/demo/VoskActivity.java` öffnen
   - Nach `model-de-DE` suchen
   - Neue Sprache hinzufügen oder die alte ersetzen

Beispiel für Englisch:
```java
String modelPath = new File(assetDir, "vosk-model-small-en-us-0.15").getAbsolutePath();
```

## System-Voraussetzungen 📱

- Android 6.0 oder neuer
- Optional: Wear OS Smartwatch für noch besseres Feedback
- Mind. 500MB freier Speicher für die Sprachmodelle

## Mehr Infos 📚

Mehr technische Details und Docs zur Vosk-Integration gibt's auf der [Vosk Website](https://alphacephei.com/vosk/android).
