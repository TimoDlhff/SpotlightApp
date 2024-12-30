# VocalWave

VocalWave ist eine innovative Android-App, die Ihnen hilft, Ihre Präsentationsfähigkeiten zu verbessern. Durch Echtzeit-Spracherkennung und subtiles haptisches Feedback unterstützt Sie die App dabei, professioneller und überzeugender zu präsentieren.

## Hauptfunktionen

### 1. Intelligente Füllwort-Erkennung
- Erkennt typische Füllwörter in Echtzeit (z.B. "ähm", "halt", "sozusagen")
- Benachrichtigt Sie diskret durch Vibration, wenn Füllwörter verwendet werden
- Hilft Ihnen, sich dieser Gewohnheit bewusst zu werden und sie zu reduzieren

### 2. Sprechgeschwindigkeits-Monitoring
- Überwacht Ihre Sprechgeschwindigkeit während der Präsentation
- Warnt Sie subtil, wenn Sie zu schnell oder zu langsam sprechen
- Hilft Ihnen, ein optimales Sprechtempo zu halten

### 3. Flexibles Feedback-System
- Wählen Sie zwischen Smartphone- oder Smartwatch-Vibration
- Diskrete Benachrichtigungen stören nicht den Präsentationsfluss
- Individuell anpassbare Einstellungen

### 4. Detaillierte Statistiken
- Analysiert Ihre Präsentationsgewohnheiten
- Zeigt Häufigkeit von Füllwörtern
- Visualisiert Sprechgeschwindigkeits-Muster
- Ermöglicht langfristige Fortschrittsverfolgung

## Installation

### 1. Projekt klonen
```bash
git clone https://github.com/TimoDlhff/VocalWaveApp.git
```

### 2. Projekt in Android Studio öffnen
1. Starten Sie Android Studio
2. Wählen Sie "Open an existing Android Studio project"
3. Navigieren Sie zum geklonten VocalWaveApp-Verzeichnis und öffnen Sie es

### 3. Projekt einrichten
1. Warten Sie, bis Android Studio das Projekt vollständig geladen hat
2. Klicken Sie auf "Sync Project with Gradle Files" (das Elephant-Symbol in der Toolbar)
3. Warten Sie, bis die Synchronisierung abgeschlossen ist

### 4. App auf Ihrem Gerät installieren
1. Verbinden Sie Ihr Android-Gerät via USB mit dem Computer
2. Aktivieren Sie auf Ihrem Gerät den "Entwicklermodus" und "USB-Debugging"
3. Wählen Sie in Android Studio Ihr Gerät aus der Geräteliste
4. Klicken Sie auf den "Run"-Button (grüner Play-Button)

## Technische Details

Die App verwendet das VOSK-Spracherkennungsmodell für die deutsche Sprache. Das Modell ist bereits im Repository enthalten und wird automatisch mit dem Projekt heruntergeladen (ca. 88 MB).

## Systemanforderungen

- Android 6.0 oder höher
- Optional: Wear OS Smartwatch für erweitertes Feedback
- Mindestens 500MB freier Speicherplatz für Sprachmodelle

## Dokumentation

Für weitere technische Dokumentation und Anweisungen zur Vosk-Integration besuchen Sie die [Vosk Website](https://alphacephei.com/vosk/android).
