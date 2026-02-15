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

    public void printSilent(ChequeConfig config, ChequeData data) throws Exception {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(new PDRectangle(CHEQUE_WIDTH_POINTS, CHEQUE_HEIGHT_POINTS));
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, config.getFontSize());

                // 1. Date (dd-MM-yyyy)
                String dateStr = data.date() != null ? data.date().format(DateTimeFormatter.ofPattern("ddMMyyyy")) : "";
                drawText(contentStream, dateStr, config.getDateX(), config.getDateY());

                // 2. Payee
                if (data.payeeName() != null) {
                    drawText(contentStream, data.payeeName(), config.getPayeeX(), config.getPayeeY());
                }

                // 3. Amount (Words)
                String amountWords = IndianNumberToWords.convert(data.amount());
                drawText(contentStream, amountWords, config.getAmountWordsX(), config.getAmountWordsY());

                // 4. Amount (Digits)
                String amountDigits = String.format("**%.2f/-", data.amount());
                drawText(contentStream, amountDigits, config.getAmountDigitsX(), config.getAmountDigitsY());

                // 5. AC Payee
                if (data.isAcPayee()) {
                    drawText(contentStream, "A/C PAYEE ONLY", 10, 10);
                }

                // 6. Signature
                if (config.getSignaturePath() != null && !config.getSignaturePath().isEmpty()) {
                    try {
                        PDImageXObject pdImage = PDImageXObject.createFromFile(config.getSignaturePath(), document);
                        // Default height 15mm, preserve aspect ratio
                        float sigHeight = 15 * MM_TO_POINTS;
                        float sigWidth = (pdImage.getWidth() / (float) pdImage.getHeight()) * sigHeight;

                        drawImage(contentStream, pdImage, config.getSignatureX(), config.getSignatureY(), sigWidth,
                                sigHeight);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // Send to Printer
            PrinterJob job = PrinterJob.getPrinterJob();
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
