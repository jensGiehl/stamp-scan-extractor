# Stamp Extractor

Dieses Projekt extrahiert einzelne Briefmarken aus `.png`-Bilddateien. Es analysiert die Eingangsbilder, identifiziert die Briefmarken und speichert jede gefundene Briefmarke als separate Bilddatei.

## Funktionsweise

Das Programm ist für die Verarbeitung von **PNG-Dateien** optimiert und unterstützt zwei Erkennungs-Strategien:

### 1. Präzisions-Modus (Optimales Ergebnis)

- **Voraussetzung**: Die PNG-Datei hat einen **transparenten Hintergrund**.
- **Methode**: Wenn ein Bild mit Transparenz (Alphakanal) bereitgestellt wird, nutzt das Programm diesen Kanal als perfekte, pixelgenaue Maske. Dies ist die mit Abstand zuverlässigste Methode.
- **Empfehlung**: Für ein optimales Ergebnis sollten die gescannten Bilder so vorbereitet werden, dass die Briefmarken auf einem transparenten Hintergrund liegen.

> **Tipp zur Erstellung transparenter Hintergründe:**
> Gute Erfahrungen wurden mit dem kostenlosen Bildbearbeitungsprogramm [Paint.NET](https://www.getpaint.net/) gemacht. Mit dem **Zauberstab-Werkzeug** und einer **Toleranz von ca. 33%** lässt sich der Hintergrund in der Regel schnell und einfach auswählen und anschließend entfernen.

### 2. Standard-Modus (Fallback)

- **Voraussetzung**: Die PNG-Datei hat einen soliden (nicht-transparenten) Hintergrund.
- **Methode**: Für diese Bilder verwendet das Programm eine robuste Bildanalyse-Pipeline (Graustufen-Konvertierung, adaptive Schwellenwertbildung, etc.), um die Briefmarken vom Hintergrund zu trennen.
- **Ergebnis**: Diese Methode ist robust, kann aber bei komplexen Hintergründen oder schlechten Lichtverhältnissen an ihre Grenzen stoßen.

## Voraussetzungen

Um dieses Projekt auszuführen, wird eine funktionierende Docker-Installation auf dem System benötigt.

## Docker-Benutzung

### 1. Docker-Image erstellen

Führen Sie den folgenden Befehl im Stammverzeichnis des Projekts aus, um das Docker-Image zu erstellen:

```bash
docker build -t stamp-extractor .
```

### 2. Anwendung mit Docker ausführen

Führen Sie den folgenden Befehl aus, um die Anwendung in einem Docker-Container zu starten. Dieser Befehl bindet die lokalen Ordner `input` und `output` in den Container ein, sodass die Anwendung auf Ihre Bilder zugreifen und die Ergebnisse speichern kann.

```bash
docker run --rm -v "$(pwd)/input:/app/input" -v "$(pwd)/output:/app/output" stamp-extractor
```

- **Input-Ordner**: Legen Sie Ihre `.png`-Dateien in den `input`-Ordner im Stammverzeichnis des Projekts.
- **Output-Ordner**: Die extrahierten Briefmarken werden im `output`-Ordner gespeichert.

Sie können auch benutzerdefinierte Ordner angeben, indem Sie die Pfade im Befehl anpassen:

```bash
docker run --rm -v "/absoluter/pfad/zu/ihrem/input:/app/input" -v "/absoluter/pfad/zu/ihrem/output:/app/output" stamp-extractor
```
