package com.lax.sme_manager.repository;

import com.lax.sme_manager.repository.model.PrintLedgerEntry;
import com.lax.sme_manager.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PrintLedgerRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrintLedgerRepository.class);

    public void logPrint(PrintLedgerEntry entry) {
        String sql = "INSERT INTO cheque_print_ledger (user_id, payee_name, amount, cheque_number, print_status, remarks, printed_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (entry.getUserId() != null)
                pstmt.setInt(1, entry.getUserId());
            else
                pstmt.setNull(1, Types.INTEGER);

            pstmt.setString(2, entry.getPayeeName());
            pstmt.setDouble(3, entry.getAmount());
            pstmt.setString(4, entry.getChequeNumber());
            pstmt.setString(5, entry.getPrintStatus());
            pstmt.setString(6, entry.getRemarks());
            pstmt.setTimestamp(7, Timestamp.valueOf(java.time.LocalDateTime.now()));

            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Failed to log print event", e);
        }
    }

    public List<PrintLedgerEntry> getAllLogs() {
        List<PrintLedgerEntry> logs = new ArrayList<>();
        String sql = """
                    SELECT l.*, u.username
                    FROM cheque_print_ledger l
                    LEFT JOIN users u ON l.user_id = u.id
                    ORDER BY l.printed_at DESC
                """;
        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                logs.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to fetch print ledger logs", e);
        }
        return logs;
    }

    private PrintLedgerEntry mapResultSet(ResultSet rs) throws SQLException {
        return PrintLedgerEntry.builder()
                .id(rs.getInt("id"))
                .userId(rs.getObject("user_id") != null ? rs.getInt("user_id") : null)
                .username(rs.getString("username"))
                .payeeName(rs.getString("payee_name"))
                .amount(rs.getDouble("amount"))
                .chequeNumber(rs.getString("cheque_number"))
                .printStatus(rs.getString("print_status"))
                .remarks(rs.getString("remarks"))
                .printedAt(rs.getTimestamp("printed_at").toLocalDateTime())
                .build();
    }
}
