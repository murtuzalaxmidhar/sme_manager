package com.lax.sme_manager.service;

import com.lax.sme_manager.repository.model.PurchaseEntity;
import com.lax.sme_manager.repository.VendorRepository;
import com.lax.sme_manager.domain.Vendor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service to export purchase data to CSV (Excel compatible).
 */
public class ExportService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportService.class);
    private final VendorRepository vendorRepository;

    public ExportService(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    public void exportToCsv(List<PurchaseEntity> purchases, File file) throws Exception {
        // Pre-fetch vendors for names
        Map<Integer, String> vendorMap = vendorRepository.findAllVendors()
                .stream()
                .collect(Collectors.toMap(Vendor::getId, Vendor::getName));

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try (PrintWriter writer = new PrintWriter(file)) {
            // Write Header (BOM for Excel UTF-8)
            writer.write('\ufeff');
            writer.println(
                    "Date,Vendor,Bags,Rate,Weight,Lumpsum,Base,Market Fee,Commission,Total,Status,Payment Mode,Cheque No,Notes");

            for (PurchaseEntity p : purchases) {
                StringBuilder sb = new StringBuilder();
                sb.append(p.getEntryDate().format(dtf)).append(",");
                sb.append(escape(vendorMap.getOrDefault(p.getVendorId(), "Unknown"))).append(",");
                sb.append(p.getBags()).append(",");
                sb.append(p.getRate()).append(",");
                sb.append(p.getWeightKg()).append(",");
                sb.append(p.getIsLumpsum() ? "Yes" : "No").append(",");
                sb.append(p.getBaseAmount()).append(",");
                sb.append(p.getMarketFeeAmount()).append(",");
                sb.append(p.getCommissionFeeAmount()).append(",");
                sb.append(p.getGrandTotal()).append(",");
                sb.append(escape(p.getStatus())).append(",");
                sb.append(escape(p.getPaymentMode())).append(",");
                sb.append(escape(p.getChequeNumber() != null ? p.getChequeNumber() : "-")).append(",");
                sb.append(escape(p.getNotes() != null ? p.getNotes() : "-"));
                writer.println(sb.toString());
            }
            LOGGER.info("Exported {} records to {}", purchases.size(), file.getAbsolutePath());
        }
    }

    private String escape(String data) {
        if (data == null)
            return "";
        String escapedData = data.replaceAll("\"", "\"\"");
        if (data.contains(",") || data.contains("\n") || data.contains("\"")) {
            escapedData = "\"" + escapedData + "\"";
        }
        return escapedData;
    }
}
