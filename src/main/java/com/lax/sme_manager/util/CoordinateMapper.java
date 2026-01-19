package com.lax.sme_manager.util;

import javafx.geometry.Point2D;

/**
 * Coordinate Mapping Utility for Cheque Designer
 * 
 * Converts between screen pixels (JavaFX UI) and physical millimeters (print
 * space).
 * 
 * MATHEMATICS:
 * - Standard cheque size: 152.4mm × 69.85mm (6" × 2.75")
 * - Screen DPI: 96 (JavaFX default)
 * - Scale factor: actualChequeMm / uploadedImagePx
 * 
 * USAGE:
 * 1. User uploads 800px wide cheque image
 * 2. CoordinateMapper mapper = new CoordinateMapper(152.4, 800);
 * 3. User drags field to (400px, 100px)
 * 4. Point2D physical = mapper.screenToPhysical(400, 100);
 * 5. Store: dateX = 76.2mm, dateY = 19.05mm in database
 * 6. During print: convert mm → printer pixels based on printer DPI
 */
public class CoordinateMapper {

    // Standard cheque dimensions in millimeters
    public static final double STANDARD_CHEQUE_WIDTH_MM = 152.4; // 6 inches
    public static final double STANDARD_CHEQUE_HEIGHT_MM = 69.85; // 2.75 inches

    private final double scaleFactor; // mm per pixel
    private final double chequeWidthMm;
    private final double chequeHeightMm;
    private final double imageWidthPx;
    private final double imageHeightPx;

    /**
     * Constructor for standard cheque size
     * 
     * @param imageWidthPx  Width of uploaded cheque image in pixels
     * @param imageHeightPx Height of uploaded cheque image in pixels
     */
    public CoordinateMapper(double imageWidthPx, double imageHeightPx) {
        this(STANDARD_CHEQUE_WIDTH_MM, STANDARD_CHEQUE_HEIGHT_MM, imageWidthPx, imageHeightPx);
    }

    /**
     * Constructor for custom cheque size
     * 
     * @param chequeWidthMm  Actual physical width of cheque in millimeters
     * @param chequeHeightMm Actual physical height of cheque in millimeters
     * @param imageWidthPx   Width of uploaded image in pixels
     * @param imageHeightPx  Height of uploaded image in pixels
     */
    public CoordinateMapper(double chequeWidthMm, double chequeHeightMm,
            double imageWidthPx, double imageHeightPx) {
        this.chequeWidthMm = chequeWidthMm;
        this.chequeHeightMm = chequeHeightMm;
        this.imageWidthPx = imageWidthPx;
        this.imageHeightPx = imageHeightPx;

        // Calculate scale factor (mm per pixel)
        this.scaleFactor = chequeWidthMm / imageWidthPx;
    }

    /**
     * Convert screen pixels to physical millimeters
     * 
     * @param pixels Value in pixels
     * @return Value in millimeters
     */
    public double pxToMm(double pixels) {
        return pixels * scaleFactor;
    }

    /**
     * Convert physical millimeters to screen pixels
     * 
     * @param mm Value in millimeters
     * @return Value in pixels
     */
    public double mmToPx(double mm) {
        return mm / scaleFactor;
    }

    /**
     * Convert screen coordinates (x, y) to physical coordinates
     * 
     * @param x X coordinate in pixels
     * @param y Y coordinate in pixels
     * @return Point2D with coordinates in millimeters
     */
    public Point2D screenToPhysical(double x, double y) {
        return new Point2D(pxToMm(x), pxToMm(y));
    }

    /**
     * Convert physical coordinates (x, y) to screen coordinates
     * 
     * @param x X coordinate in millimeters
     * @param y Y coordinate in millimeters
     * @return Point2D with coordinates in pixels
     */
    public Point2D physicalToScreen(double x, double y) {
        return new Point2D(mmToPx(x), mmToPx(y));
    }

    /**
     * Convert millimeters to printer pixels for a specific DPI
     * 
     * @param mm         Value in millimeters
     * @param printerDPI Printer resolution (e.g., 300, 600)
     * @return Value in printer pixels
     */
    public static double mmToPrinterPixels(double mm, int printerDPI) {
        // 1 inch = 25.4mm
        // pixels = (mm / 25.4) * DPI
        return (mm / 25.4) * printerDPI;
    }

    /**
     * Convert printer pixels to millimeters for a specific DPI
     * 
     * @param pixels     Value in printer pixels
     * @param printerDPI Printer resolution (e.g., 300, 600)
     * @return Value in millimeters
     */
    public static double printerPixelsToMm(double pixels, int printerDPI) {
        // mm = (pixels / DPI) * 25.4
        return (pixels / printerDPI) * 25.4;
    }

    // Getters
    public double getScaleFactor() {
        return scaleFactor;
    }

    public double getChequeWidthMm() {
        return chequeWidthMm;
    }

    public double getChequeHeightMm() {
        return chequeHeightMm;
    }

    public double getImageWidthPx() {
        return imageWidthPx;
    }

    public double getImageHeightPx() {
        return imageHeightPx;
    }
}
