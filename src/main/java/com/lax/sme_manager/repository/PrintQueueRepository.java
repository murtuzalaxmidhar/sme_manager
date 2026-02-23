package com.lax.sme_manager.repository;

import com.lax.sme_manager.repository.model.PrintQueueItem;
import com.lax.sme_manager.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PrintQueueRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrintQueueRepository.class);

    public void addItem(PrintQueueItem item) {
        String sql = "INSERT INTO cheque_print_queue (purchase_id, payee_name, amount, cheque_date, is_ac_payee) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (item.getPurchaseId() != null) {
                pstmt.setInt(1, item.getPurchaseId());
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }
            pstmt.setString(2, item.getPayeeName());
            pstmt.setDouble(3, item.getAmount());
            pstmt.setObject(4, item.getChequeDate());
            pstmt.setBoolean(5, item.isAcPayee());

            pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    item.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to add item to print queue", e);
        }
    }

    public List<PrintQueueItem> getAllItems() {
        List<PrintQueueItem> items = new ArrayList<>();
        String sql = "SELECT * FROM cheque_print_queue ORDER BY created_at ASC";
        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to fetch print queue items", e);
        }
        return items;
    }

    public void removeItem(int id) {
        String sql = "DELETE FROM cheque_print_queue WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Failed to remove item from print queue", e);
        }
    }

    public void clearQueue() {
        String sql = "DELETE FROM cheque_print_queue";
        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            LOGGER.error("Failed to clear print queue", e);
        }
    }

    public void updateItem(PrintQueueItem item) {
        String sql = "UPDATE cheque_print_queue SET payee_name = ?, amount = ?, cheque_date = ?, is_ac_payee = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, item.getPayeeName());
            pstmt.setDouble(2, item.getAmount());
            pstmt.setObject(3, item.getChequeDate());
            pstmt.setBoolean(4, item.isAcPayee());
            pstmt.setInt(5, item.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Failed to update print queue item", e);
        }
    }

    public int countItems() {
        String sql = "SELECT COUNT(*) FROM cheque_print_queue";
        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to count print queue items", e);
        }
        return 0;
    }

    private PrintQueueItem mapResultSet(ResultSet rs) throws SQLException {
        return PrintQueueItem.builder()
                .id(rs.getInt("id"))
                .purchaseId(rs.getObject("purchase_id") != null ? rs.getInt("purchase_id") : null)
                .payeeName(rs.getString("payee_name"))
                .amount(rs.getDouble("amount"))
                .chequeDate(rs.getObject("cheque_date", LocalDate.class))
                .isAcPayee(rs.getBoolean("is_ac_payee"))
                .createdAt(rs.getObject("created_at", LocalDateTime.class))
                .build();
    }
}
