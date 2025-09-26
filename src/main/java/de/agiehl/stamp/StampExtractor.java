package de.agiehl.stamp;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class StampExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(StampExtractor.class);

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        var inputPath = (args.length > 0) ? Paths.get(args[0]) : Paths.get("input");
        var outputPath = (args.length > 1) ? Paths.get(args[1]) : Paths.get("output");

        new StampExtractor().run(inputPath, outputPath);
    }

    private void run(Path inputDirectory, Path outputDirectory) {
        LOGGER.info("Input directory: {}", inputDirectory.toAbsolutePath());
        LOGGER.info("Output directory: {}", outputDirectory.toAbsolutePath());

        try {
            Files.createDirectories(outputDirectory);
            try (Stream<Path> fileStream = Files.list(inputDirectory)) {
                fileStream
                        .filter(Files::isRegularFile)
                        .filter(this::isImageFile)
                        .forEach(inputFile -> processImage(inputFile, outputDirectory));
            }
        } catch (IOException e) {
            LOGGER.error("Error enumerating files: {}", e.getMessage(), e);
        }
    }

    private void processImage(Path inputFile, Path outputDirectory) {
        LOGGER.info("Processing: {}", inputFile.getFileName());
        try {
            byte[] fileBytes = Files.readAllBytes(inputFile);
            MatOfByte matOfByte = new MatOfByte(fileBytes);

            // Load the image as-is, preserving the alpha channel if it exists (e.g., for PNGs)
            Mat originalImage = Imgcodecs.imdecode(matOfByte, Imgcodecs.IMREAD_UNCHANGED);

            if (originalImage.empty()) {
                LOGGER.warn("OpenCV could not decode image: {}", inputFile.getFileName());
                return;
            }

            var stampFinder = new StampFinder();
            var stampBoxes = stampFinder.findStampsIn(originalImage);

            if (stampBoxes.isEmpty()) {
                LOGGER.info("-> No stamps found in {}.", inputFile.getFileName());
                return;
            }

            LOGGER.info("-> Found {} potential stamps. Saving...", stampBoxes.size());
            saveCroppedImages(stampBoxes, originalImage, inputFile, outputDirectory);

        } catch (IOException e) {
            LOGGER.error("Failed to read file into memory: {}", inputFile.getFileName(), e);
        }
    }

    private void saveCroppedImages(java.util.List<Rect> stampBoxes, Mat originalImage, Path inputFile, Path outputDirectory) {
        var baseFileName = getBaseFileName(inputFile);
        var counter = 1;

        for (var box : stampBoxes) {
            var croppedImage = new Mat(originalImage, box);
            var outputFileName = String.format("%s_%03d.png", baseFileName, counter++);
            var outputFilePath = outputDirectory.resolve(outputFileName);

            Imgcodecs.imwrite(outputFilePath.toString(), croppedImage);
        }
    }

    private String getBaseFileName(Path path) {
        String fileName = path.getFileName().toString();
        // Sanitize for the output filename, just to be safe.
        String sanitizedFileName = fileName.replaceAll("[^a-zA-Z0-9.\\-]", "");
        int dotIndex = sanitizedFileName.lastIndexOf('.');
        return (dotIndex == -1) ? sanitizedFileName : sanitizedFileName.substring(0, dotIndex);
    }

    private boolean isImageFile(Path path) {
        var fileName = path.toString().toLowerCase();
        return fileName.endsWith(".png");
    }
}
