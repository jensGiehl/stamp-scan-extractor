package de.agiehl.stamp;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class StampFinder {

    // --- Parameters for Stamp Detection ---
    private static final double MIN_STAMP_AREA = 10000; // Corresponds roughly to 100x100 pixels
    private static final double MIN_ASPECT_RATIO = 0.5;
    private static final double MAX_ASPECT_RATIO = 2.0;
    private static final int ADAPTIVE_THRESHOLD_BLOCK_SIZE = 51;
    private static final int ADAPTIVE_THRESHOLD_C = 10;

    public List<Rect> findStampsIn(Mat image) {
        Mat binaryMask;

        // Check if the image has an alpha channel (4 channels: BGRA)
        if (image.channels() == 4) {
            // --- STRATEGY 1: Use the Alpha Channel as a perfect mask ---
            var channels = new ArrayList<Mat>();
            Core.split(image, channels);
            binaryMask = channels.get(3); // The alpha channel is the 4th channel

            // Binarize the alpha channel to handle semi-transparent edges (anti-aliasing)
            // Any pixel that is not fully transparent (alpha > 0) is considered part of the stamp.
            Imgproc.threshold(binaryMask, binaryMask, 0, 255, Imgproc.THRESH_BINARY);

        } else {
            // --- STRATEGY 2: Fallback for images without alpha (e.g., JPG) ---
            var grayImage = new Mat();
            Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);

            var thresholdImage = new Mat();
            Imgproc.adaptiveThreshold(grayImage,
                    thresholdImage,
                    255,
                    Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                    Imgproc.THRESH_BINARY_INV,
                    ADAPTIVE_THRESHOLD_BLOCK_SIZE,
                    ADAPTIVE_THRESHOLD_C);

            Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
            Imgproc.morphologyEx(thresholdImage, thresholdImage, Imgproc.MORPH_CLOSE, kernel);
            binaryMask = thresholdImage;
        }

        // --- Contour Detection (works on the binary mask from either strategy) ---
        var contours = new ArrayList<MatOfPoint>();
        var hierarchy = new Mat();
        Imgproc.findContours(binaryMask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Filter the contours to find the ones that are likely to be stamps.
        return contours.stream()
                .filter(this::isLikelyAStamp)
                .map(Imgproc::boundingRect)
                .toList();
    }

    private boolean isLikelyAStamp(MatOfPoint contour) {
        double area = Imgproc.contourArea(contour);
        if (area < MIN_STAMP_AREA) {
            return false;
        }

        Rect boundingBox = Imgproc.boundingRect(contour);
        double aspectRatio = (double) boundingBox.width / boundingBox.height;
        return !(aspectRatio < MIN_ASPECT_RATIO) && !(aspectRatio > MAX_ASPECT_RATIO);
    }
}
