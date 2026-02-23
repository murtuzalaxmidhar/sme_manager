package com.lax.sme_manager.service;

import com.lax.sme_manager.domain.Vendor;
import com.lax.sme_manager.repository.model.PurchaseEntity;
import com.lax.sme_manager.util.VendorCache;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportService {
    private final VendorCache vendorCache;

    public ReportService(VendorCache vendorCache) {
        this.vendorCache = vendorCache;
    }

    public void exportToExcel(List<PurchaseEntity> data, File file) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Monthly Purchase Report");

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            // Columns
            String[] columns = { "Date", "Vendor", "Bags", "Rate", "Base Amount", "Market Fee", "Commission",
                    "Grand Total", "Status" };
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data
            int rowIdx = 1;
            BigDecimal totalBags = BigDecimal.ZERO;
            BigDecimal totalGrand = BigDecimal.ZERO;
            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (PurchaseEntity p : data) {
                Row row = sheet.createRow(rowIdx++);
                Vendor v = vendorCache.findById(p.getVendorId());
                String vendorName = (v != null) ? v.getName() : "Unknown";

                row.createCell(0).setCellValue(p.getEntryDate().format(df));
                row.createCell(1).setCellValue(vendorName);
                row.createCell(2).setCellValue(p.getBags());
                row.createCell(3).setCellValue(p.getRate().doubleValue());
                row.createCell(4).setCellValue(p.getBaseAmount().doubleValue());
                row.createCell(5).setCellValue(p.getMarketFeeAmount().doubleValue());
                row.createCell(6).setCellValue(p.getCommissionFeeAmount().doubleValue());
                row.createCell(7).setCellValue(p.getGrandTotal().doubleValue());
                row.createCell(8).setCellValue(p.getStatus());

                totalBags = totalBags.add(BigDecimal.valueOf(p.getBags()));
                totalGrand = totalGrand.add(p.getGrandTotal());
            }

            // Total Row
            Row totalRow = sheet.createRow(rowIdx);
            totalRow.createCell(1).setCellValue("TOTALS");
            totalRow.createCell(2).setCellValue(totalBags.doubleValue());
            totalRow.createCell(7).setCellValue(totalGrand.doubleValue());

            CellStyle totalStyle = workbook.createCellStyle();
            Font totalFont = workbook.createFont();
            totalFont.setBold(true);
            totalStyle.setFont(totalFont);
            totalRow.getCell(1).setCellStyle(totalStyle);

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
            }
        }
    }

    public void exportToPDF(List<PurchaseEntity> data, File file) throws Exception {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("Monthly Accountant Report");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                contentStream.newLineAtOffset(50, 730);
                contentStream.showText("Total Transactions: " + data.size());
                contentStream.endText();

                // Simple Table-like view (Minimalist for now)
                float y = 700;
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                contentStream.newLineAtOffset(50, y);
                contentStream.showText("Date         Vendor               Bags    Total Amount");
                contentStream.endText();
                y -= 20;

                contentStream.setFont(PDType1Font.HELVETICA, 9);
                DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                for (PurchaseEntity p : data) {
                    if (y < 50) {
                        // Support for multiple pages could be added here, but keep it simple for now
                        break;
                    }
                    Vendor v = vendorCache.findById(p.getVendorId());
                    String vName = (v != null)
                            ? (v.getName().length() > 20 ? v.getName().substring(0, 17) + "..." : v.getName())
                            : "Unknown";

                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, y);
                    String line = String.format("%-12s %-20s %-7d %-15.2f",
                            p.getEntryDate().format(df), vName, p.getBags(), p.getGrandTotal().doubleValue());
                    contentStream.showText(line);
                    contentStream.endText();
                    y -= 15;
                }
            }

            document.save(file);
        }
    }
}
