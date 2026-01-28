package com.lax.sme_manager.service;

import com.lax.sme_manager.repository.model.PurchaseEntity;
import com.lax.sme_manager.repository.VendorRepository;
import com.lax.sme_manager.domain.Vendor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service to export purchase data to professional Excel (.xlsx).
 */
public class ExportService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportService.class);
    private final VendorRepository vendorRepository;

    public ExportService(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    public void exportToCsv(List<PurchaseEntity> purchases, File file) throws Exception {
        // We will default to calling formatting logic, but keep method signature for
        // compatibility
        // if file extension is .xlsx, use POI.
        if (file.getName().toLowerCase().endsWith(".xlsx")) {
            exportToExcel(purchases, file);
        } else {
            // Fallback to legacy or just use same POI but save as .xlsx?
            // Phase 3 goal is Excel. Let's assume user picks .xlsx via FileChooser.
            exportToExcel(purchases, file);
        }
    }

    public void exportToExcel(List<PurchaseEntity> purchases, File file) throws Exception {
        exportToExcel(purchases, file, null); // Delegate to the main method
    }

    public void exportToExcel(List<PurchaseEntity> purchases, File file, List<String> selectedColumns)
            throws Exception {
        Map<Integer, String> vendorMap = vendorRepository.findAllVendors()
                .stream()
                .collect(Collectors.toMap(Vendor::getId, Vendor::getName));

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Purchase History");

            // Print Setup: A4 Landscape
            sheet.getPrintSetup().setPaperSize(PrintSetup.A4_PAPERSIZE);
            sheet.getPrintSetup().setLandscape(true);
            sheet.setFitToPage(true);
            sheet.setHorizontallyCenter(true);

            // Styles
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle normalStyle = createBorderedStyle(workbook);
            CellStyle totalLabelStyle = createTotalLabelStyle(workbook);
            CellStyle totalValueStyle = createTotalValueStyle(workbook);

            // Row 0: Title "Laxmidhar Enterprise"
            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(30);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Laxmidhar Enterprise - Purchase Report");
            titleCell.setCellStyle(titleStyle);

            // Determine active headers
            String[] allHeaders = { "Date", "Vendor", "Bags", "Rate", "Weight (kg)", "Total Amount", "Status",
                    "Payment", "Cheque No", "Notes" };
            java.util.List<String> activeHeaders = new java.util.ArrayList<>();
            if (selectedColumns == null || selectedColumns.isEmpty()) {
                activeHeaders = java.util.Arrays.asList(allHeaders);
            } else {
                activeHeaders = selectedColumns;
            }

            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, activeHeaders.size() - 1));

            // Row 1: Headers
            Row headerRow = sheet.createRow(1);
            headerRow.setHeightInPoints(20);
            for (int i = 0; i < activeHeaders.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(activeHeaders.get(i));
                cell.setCellStyle(headerStyle);
            }

            // Data Rows
            int rowIdx = 2;
            int totalBags = 0;
            double totalAmount = 0;

            for (PurchaseEntity p : purchases) {
                Row row = sheet.createRow(rowIdx++);
                for (int i = 0; i < activeHeaders.size(); i++) {
                    String h = activeHeaders.get(i);
                    Cell cell = row.createCell(i);
                    cell.setCellStyle(normalStyle);

                    if (h.equals("Date")) {
                        cell.setCellValue(p.getEntryDate());
                        cell.setCellStyle(dateStyle);
                    } else if (h.equals("Vendor")) {
                        cell.setCellValue(vendorMap.getOrDefault(p.getVendorId(), "Unknown"));
                    } else if (h.equals("Bags")) {
                        cell.setCellValue(p.getBags());
                        totalBags += p.getBags();
                    } else if (h.equals("Rate")) {
                        cell.setCellValue(p.getRate() != null ? p.getRate().doubleValue() : 0.0);
                        cell.setCellStyle(currencyStyle);
                    } else if (h.equals("Weight (kg)")) {
                        cell.setCellValue(p.getWeightKg() != null ? p.getWeightKg().doubleValue() : 0.0);
                        cell.setCellStyle(currencyStyle);
                    } else if (h.equals("Total Amount")) {
                        double val = p.getGrandTotal() != null ? p.getGrandTotal().doubleValue() : 0.0;
                        cell.setCellValue(val);
                        cell.setCellStyle(currencyStyle);
                        totalAmount += val;
                    } else if (h.equals("Status")) {
                        cell.setCellValue(p.getStatus());
                    } else if (h.equals("Payment")) {
                        cell.setCellValue(p.getPaymentMode());
                    } else if (h.equals("Cheque No")) {
                        cell.setCellValue(p.getChequeNumber() != null ? p.getChequeNumber() : "-");
                    } else if (h.equals("Notes")) {
                        cell.setCellValue(p.getNotes() != null ? p.getNotes() : "");
                    }
                }
            }

            // Totals Row
            Row totalRow = sheet.createRow(rowIdx);
            totalRow.setHeightInPoints(24);

            for (int i = 0; i < activeHeaders.size(); i++) {
                String h = activeHeaders.get(i);
                if (h.equals("Vendor")) {
                    Cell c = totalRow.createCell(i);
                    c.setCellValue("TOTALS:");
                    c.setCellStyle(totalLabelStyle);
                } else if (h.equals("Bags")) {
                    Cell c = totalRow.createCell(i);
                    c.setCellValue(totalBags);
                    c.setCellStyle(totalValueStyle);
                } else if (h.equals("Total Amount")) {
                    Cell c = totalRow.createCell(i);
                    c.setCellValue(totalAmount);
                    c.setCellStyle(totalValueStyle);
                }
            }

            // Auto Size Columns
            for (int i = 0; i < activeHeaders.size(); i++) {
                sheet.autoSizeColumn(i);
                if (activeHeaders.get(i).equals("Notes")) {
                    sheet.setColumnWidth(i, 8000);
                }
            }

            // Write File
            try (FileOutputStream out = new FileOutputStream(file)) {
                workbook.write(out);
            }

            LOGGER.info("Exported EXCEL report to {} with {} columns", file.getAbsolutePath(), activeHeaders.size());
        }
    }

    // --- Style Helpers ---

    private CellStyle createTitleStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 16);
        font.setBold(true);
        font.setColor(IndexedColors.DARK_TEAL.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = createBorderedStyle(wb);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createBorderedStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDateStyle(Workbook wb) {
        CellStyle style = createBorderedStyle(wb);
        style.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("dd/mm/yyyy"));
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook wb) {
        CellStyle style = createBorderedStyle(wb);
        style.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("#,##0.00")); // Currency format
        return style;
    }

    private CellStyle createTotalLabelStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private CellStyle createTotalValueStyle(Workbook wb) {
        CellStyle style = createBorderedStyle(wb);
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("#,##0.00"));
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}
