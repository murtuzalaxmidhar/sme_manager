package com.lax.sme_manager.repository;

import com.lax.sme_manager.util.DatabaseManager;
import java.sql.*;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class TrendRepository implements ITrendRepository {

    @Override
    public Map<LocalDate, Integer> getWeeklyBagsTrend() {
        String sql = "SELECT entry_date, SUM(bags) as total_bags " +
                "FROM purchase_entries " +
                "WHERE entry_date >= ? AND is_deleted = 0 " +
                "GROUP BY entry_date " +
                "ORDER BY entry_date ASC";

        Map<LocalDate, Integer> trend = new TreeMap<>(); // Sorted by date
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(6);

        // Pre-fill with zeros
        for (int i = 0; i < 7; i++) {
            trend.put(sevenDaysAgo.plusDays(i), 0);
        }

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, sevenDaysAgo);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    LocalDate date = rs.getObject("entry_date", LocalDate.class);
                    trend.put(date, rs.getInt("total_bags"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return trend;
    }

    @Override
    public Map<String, Integer> getPaymentModeDistribution() {
        String sql = "SELECT payment_mode, COUNT(*) as count " +
                "FROM purchase_entries " +
                "WHERE is_deleted = 0 " +
                "GROUP BY payment_mode";

        Map<String, Integer> distribution = new LinkedHashMap<>();
        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                distribution.put(rs.getString("payment_mode"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return distribution;
    }

    @Override
    public Map<String, Integer> getTopVendors(int limit) {
        String sql = "SELECT v.name, SUM(p.bags) as total_bags " +
                "FROM purchase_entries p " +
                "JOIN vendors v ON p.vendor_id = v.id " +
                "WHERE p.is_deleted = 0 " +
                "GROUP BY v.id " +
                "ORDER BY total_bags DESC " +
                "LIMIT ?";

        Map<String, Integer> topVendors = new LinkedHashMap<>();
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    topVendors.put(rs.getString("name"), rs.getInt("total_bags"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return topVendors;
    }
}
