package com.lax.sme_manager.service;

import com.lax.sme_manager.dto.ChequeData;
import com.lax.sme_manager.repository.model.ChequeConfig;
import com.lax.sme_manager.util.IndianNumberToWords;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.printing.PDFPageable;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class ChequePrintService {

    // 1 mm = 2.83465 points
    private static final float MM_TO_POINTS = 2.83465f;

    // CTS-2010 Standard Cheque Size: 202mm x 92mm
    private static final float CHEQUE_WIDTH_POINTS = 202 * MM_TO_POINTS;
    private static final float CHEQUE_HEIGHT_POINTS = 92 * MM_TO_POINTS;

    private static final boolean FIX_ROTATION = true;

    public void printSilent(ChequeConfig config, ChequeData data) throws Exception {
        try (PDDocument document = new PDDocument()) {
            PDRectangle pageSize = new PDRectangle(CHEQUE_WIDTH_POINTS, CHEQUE_HEIGHT_POINTS);
            if (FIX_ROTATION) {
                // Swap dimensions for rotation
                pageSize = new PDRectangle(CHEQUE_HEIGHT_POINTS, CHEQUE_WIDTH_POINTS);
            }

            PDPage page = new PDPage(pageSize);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                if (FIX_ROTATION) {
                    // Rotate the content 90 degrees and shift origin so (0,0) is correct for the
                    // new orientation
                    // Matrix.getRotateInstance(Angle, X, Y)
                    contentStream.transform(org.apache.pdfbox.util.Matrix.getRotateInstance(Math.toRadians(90),
                            CHEQUE_HEIGHT_POINTS, 0));
                }

                contentStream.setFont(PDType1Font.HELVETICA_BOLD, config.getFontSize());

                // 1. Date (dd-MM-yyyy) - Expert Alignment
                String dateStr = data.date() != null ? data.date().format(DateTimeFormatter.ofPattern("ddMMyyyy")) : "";
                String[] posArray = config.getDatePositions() != null ? config.getDatePositions().split(";") : null;

                for (int i = 0; i < dateStr.length(); i++) {
                    float x, y;
                    if (posArray != null && i < posArray.length) {
                        String[] xy = posArray[i].split(",");
                        x = Float.parseFloat(xy[0]);
                        y = Float.parseFloat(xy[1]);
                    } else {
                        x = (float) (config.getDateX() + (i * 5.5));
                        y = (float) config.getDateY();
                    }
                    drawText(contentStream, String.valueOf(dateStr.charAt(i)), x, y);
                }

                // 2. Payee
                if (data.payeeName() != null) {
                    drawText(contentStream, data.payeeName(), config.getPayeeX(), config.getPayeeY());
                }

                // 3. Amount (Words)
                String amountWords = IndianNumberToWords.convert(data.amount());
                drawText(contentStream, amountWords, config.getAmountWordsX(), config.getAmountWordsY());

                // 4. Amount (Digits)
                String amountDigits = String.format("%.2f/-", data.amount());
                drawText(contentStream, amountDigits, config.getAmountDigitsX(), config.getAmountDigitsY());

                // 5. AC Payee - Expert Diagonal Ribbon
                if (data.isAcPayee()) {
                    drawAcPayee(contentStream, 15, 10);
                }

                // 6. Signature - Expert "Pen-Authentic" Integration
                var sigRepo = new com.lax.sme_manager.repository.SignatureRepository();
                var sigCfg = config.getActiveSignatureId() > 0 ? sigRepo.getSignatureById(config.getActiveSignatureId())
                        : null;
                String sigPath = (sigCfg != null) ? sigCfg.getPath() : config.getSignaturePath();

                if (sigPath != null && !sigPath.isEmpty()) {
                    try {
                        PDImageXObject pdImage = PDImageXObject.createFromFile(sigPath, document);

                        float scale = (sigCfg != null) ? (float) sigCfg.getScale() : 1.0f;
                        float baseWidthMm = 40f;
                        float sigWidth = baseWidthMm * scale * MM_TO_POINTS;
                        float sigHeight = (pdImage.getHeight() / (float) pdImage.getWidth()) * sigWidth;

                        // Apply Pen-Authentic Effects
                        org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState gs = new org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState();
                        gs.setBlendMode(org.apache.pdfbox.pdmodel.graphics.blend.BlendMode.MULTIPLY);
                        if (sigCfg != null)
                            gs.setNonStrokingAlphaConstant((float) sigCfg.getOpacity());
                        contentStream.setGraphicsStateParameters(gs);

                        drawImage(contentStream, pdImage, config.getSignatureX(), config.getSignatureY(), sigWidth,
                                sigHeight);

                        // Reset graphics state for anything after
                        contentStream.setGraphicsStateParameters(
                                new org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // Send to Printer
            PrinterJob job = PrinterJob.getPrinterJob();

            // Expert fix: Force Landscape PageFormat for the PrinterJob
            java.awt.print.PageFormat pf = job.defaultPage();
            pf.setOrientation(java.awt.print.PageFormat.LANDSCAPE);

            job.setPageable(new PDFPageable(document));
            PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
            if (defaultService != null) {
                job.setPrintService(defaultService);
                job.print();
            } else {
                throw new RuntimeException("No default printer found.");
            }
        }
    }

    /**
     * EXPERT FIX: Draws "A/C PAYEE ONLY" diagonally with parallel lines
     */
    private void drawAcPayee(PDPageContentStream stream, double xMm, double yMm) throws IOException {
        float x = (float) (xMm * MM_TO_POINTS);
        float y = CHEQUE_HEIGHT_POINTS - (float) (yMm * MM_TO_POINTS);

        stream.saveGraphicsState();

        // 1. Position and Rotate
        stream.transform(org.apache.pdfbox.util.Matrix.getTranslateInstance(x, y));
        stream.transform(org.apache.pdfbox.util.Matrix.getRotateInstance(Math.toRadians(15), 0, 0));

        // 2. Draw Parallel Lines
        stream.setLineWidth(1.2f);
        stream.setStrokingColor(0, 0, 0);

        // Top line (Extended for 'Y' clearance)
        stream.moveTo(-12, 12);
        stream.lineTo(120, 12);
        stream.stroke();

        // Bottom line (Extended for 'Y' clearance)
        stream.moveTo(-12, -4);
        stream.lineTo(120, -4);
        stream.stroke();

        // 3. Draw Text
        stream.beginText();
        stream.setFont(PDType1Font.HELVETICA_BOLD, 11);
        stream.newLineAtOffset(0, 0);
        stream.showText("A/C PAYEE");
        stream.endText();

        stream.restoreGraphicsState();
    }

    private void drawText(PDPageContentStream stream, String text, double xMm, double yMm) throws IOException {
        if (text == null || text.isEmpty())
            return;

        float x = (float) (xMm * MM_TO_POINTS);
        float y = CHEQUE_HEIGHT_POINTS - (float) (yMm * MM_TO_POINTS); // Invert Y

        stream.beginText();
        stream.newLineAtOffset(x, y);
        stream.showText(text);
        stream.endText();
    }

    private void drawImage(PDPageContentStream stream, PDImageXObject image, double xMm, double yMm, float widthPoints,
            float heightPoints) throws IOException {
        float x = (float) (xMm * MM_TO_POINTS);
        float y = CHEQUE_HEIGHT_POINTS - (float) (yMm * MM_TO_POINTS) - heightPoints; // Invert Y (Bottom-Left anchor)

        stream.drawImage(image, x, y, widthPoints, heightPoints);
    }
}
