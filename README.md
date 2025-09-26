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

Um dieses Projekt auszuführen, wird eine funktionierende OpenCV-Installation auf dem System benötigt. Die native Bibliothek muss für die JVM verfügbar sein.

- **OpenCV-Version**: Es muss die korrekte Version von OpenCV installiert sein, die in der [pom.xml](pom.xml) dieses Projekts definiert ist. 
- **Download**: Sie können die für Ihr Betriebssystem passende Version von der offiziellen Releases-Seite herunterladen: [https://opencv.org/releases/](https://opencv.org/releases/)


## Benutzung

1.  **Kompilieren des Projekts:**
    ```bash
    mvn clean package
    ```

2.  **Ausführen des Programms:**
    ```bash
    java -jar target/stamp-extractor-1.0.0.jar [INPUT_ORDNER] [OUTPUT_ORDNER]
    ```
    - `[INPUT_ORDNER]` (optional): Der Ordner, der die zu verarbeitenden `.png`-Bilder enthält. Standardmäßig wird der Ordner `input` verwendet.
    - `[OUTPUT_ORDNER]` (optional): Der Ordner, in dem die ausgeschnittenen Briefmarken gespeichert werden sollen. Standardmäßig wird ein Ordner namens `output` erstellt.
