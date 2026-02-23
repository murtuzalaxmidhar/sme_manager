package com.lax.sme_manager.repository;

import com.lax.sme_manager.util.DatabaseManager;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class VendorAnalyticsRepository implements IVendorAnalyticsRepository {

    @Override
    public Map<String, Integer> getMonthlySupplyTrend(int vendorId) {
        String sql = "SELECT strftime('%Y-%m', entry_date) as month, SUM(bags) as total_bags " +
                "FROM purchase_entries " +
                "WHERE vendor_id = ? AND is_deleted = 0 " +
                "GROUP BY month " +
                "ORDER BY month DESC " +
                "LIMIT 12";

        Map<String, Integer> trend = new LinkedHashMap<>();
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, vendorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    trend.put(rs.getString("month"), rs.getInt("total_bags"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return trend;
    }

    @Override
    public Map<LocalDate, BigDecimal> getPriceHistory(int vendorId) {
        String sql = "SELECT entry_date, rate " +
                "FROM purchase_entries " +
                "WHERE vendor_id = ? AND is_deleted = 0 " +
                "ORDER BY entry_date ASC";

        Map<LocalDate, BigDecimal> history = new TreeMap<>();
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, vendorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    history.put(rs.getObject("entry_date", LocalDate.class),
                            rs.getBigDecimal("rate"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    @Override
    public Map<String, Object> getVendorSummary(int vendorId) {
        String sql = "SELECT COUNT(*) as trans_count, SUM(bags) as total_bags, " +
                "AVG(rate) as avg_rate, MAX(entry_date) as last_date " +
                "FROM purchase_entries " +
                "WHERE vendor_id = ? AND is_deleted = 0";

        Map<String, Object> summary = new LinkedHashMap<>();
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, vendorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    summary.put("Transaction Count", rs.getInt("trans_count"));
                    summary.put("Total Bags", rs.getInt("total_bags"));
                    summary.put("Average Rate", rs.getBigDecimal("avg_rate"));
                    summary.put("Last Transaction", rs.getObject("last_date", LocalDate.class));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return summary;
    }
}
