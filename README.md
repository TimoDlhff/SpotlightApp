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

### 1. Git LFS Setup (nur einmalig pro Computer)
Wenn Sie Git LFS noch nicht installiert haben:

1. Installieren Sie Git LFS:
   - **Mac**: `brew install git-lfs`
   - **Windows**: Laden Sie den Installer von https://git-lfs.github.com herunter
   - **Linux**: `sudo apt-get install git-lfs`

2. Aktivieren Sie Git LFS (einmalig):
   ```bash
   git lfs install
   ```

### 2. Projekt klonen
```bash
git clone https://github.com/TimoDlhff/VocalWaveApp.git
```
Die Sprachmodelle werden automatisch über Git LFS heruntergeladen.

### 3. Projekt einrichten
1. Öffnen Sie das Projekt in Android Studio
2. Synchronisieren Sie das Projekt mit Gradle

## Technische Details

- Offline-Spracherkennung basierend auf Kaldi und Vosk
- Die Sprachmodelle (`.mdl` und `.fst` Dateien) werden über Git LFS verwaltet
- Lokaler Pfad der Modelle: `models/src/main/assets/`
- Unterstützte Sprachen: Deutsch und Englisch
- Wear OS Integration für Smartwatch-Feedback

## Systemanforderungen

- Android 6.0 oder höher
- Optional: Wear OS Smartwatch für erweitertes Feedback
- Mindestens 500MB freier Speicherplatz für Sprachmodelle

## Dokumentation

Für weitere technische Dokumentation und Anweisungen zur Vosk-Integration besuchen Sie die [Vosk Website](https://alphacephei.com/vosk/android).
