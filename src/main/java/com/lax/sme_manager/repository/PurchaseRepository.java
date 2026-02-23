package com.lax.sme_manager.repository;

import com.lax.sme_manager.repository.model.PurchaseEntity;
import com.lax.sme_manager.util.DatabaseManager;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PurchaseRepository - Implementation of IPurchaseRepository
 */
public class PurchaseRepository implements IPurchaseRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(PurchaseRepository.class);

    @Override
    public PurchaseEntity save(PurchaseEntity entity) {
        if (entity.getId() == null) {
            return insert(entity);
        } else {
            return update(entity);
        }
    }

    private PurchaseEntity insert(PurchaseEntity entity) {
        String sql = "INSERT INTO purchase_entries (" +
                "entry_date, vendor_id, bags, rate, weight_kg, is_lumpsum, " +
                "market_fee_percent, commission_percent, market_fee_amount, commission_amount, " +
                "base_amount, grand_total, notes, payment_mode, " +
                "advance_paid, status, created_at, updated_at, " +
                "cheque_number, cheque_date, created_by_user) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int idx = 1;
            idx = setCommonFields(pstmt, entity, idx);
            pstmt.setObject(idx++, entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
            pstmt.setObject(idx++, LocalDateTime.now()); // updated_at
            pstmt.setString(idx++, entity.getChequeNumber());
            pstmt.setObject(idx++, entity.getChequeDate());
            pstmt.setString(idx++, entity.getCreatedByUser() != null ? entity.getCreatedByUser() : "admin");

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating purchase failed, no rows affected.");
            }
            try (Statement s = conn.createStatement();
                    ResultSet rs = s.executeQuery("SELECT last_insert_rowid()")) {
                rs.next();
                entity.setId(rs.getInt(1));
            }
            return entity;

        } catch (SQLException e) {
            System.err.println("Error inserting purchase: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save purchase", e);
        }
    }

    @Override
    public void updateStatus(Integer id, String status) {
        String sql = "UPDATE purchase_entries SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating status: " + e.getMessage());
            throw new RuntimeException("Failed to update status", e);
        }
    }

    private PurchaseEntity update(PurchaseEntity entity) {
        String sql = "UPDATE purchase_entries SET " +
                "entry_date = ?, vendor_id = ?, bags = ?, rate = ?, weight_kg = ?, is_lumpsum = ?, " +
                "market_fee_percent = ?, commission_percent = ?, market_fee_amount = ?, commission_amount = ?, " +
                "base_amount = ?, grand_total = ?, notes = ?, payment_mode = ?, " +
                "advance_paid = ?, status = ?, updated_at = ?, " +
                "cheque_number = ?, cheque_date = ? " +
                "WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int idx = 1;
            idx = setCommonFields(pstmt, entity, idx);
            pstmt.setObject(idx++, LocalDateTime.now()); // updated_at
            pstmt.setString(idx++, entity.getChequeNumber());
            pstmt.setObject(idx++, entity.getChequeDate());
            pstmt.setInt(idx++, entity.getId()); // WHERE id = ?

            pstmt.executeUpdate();
            return entity;
        } catch (SQLException e) {
            System.err.println("Error updating purchase: " + e.getMessage());
            throw new RuntimeException("Failed to update purchase", e);
        }
    }

    private int setCommonFields(PreparedStatement pstmt, PurchaseEntity entity, int startIndex)
            throws SQLException {
        int index = startIndex;
        pstmt.setObject(index++, entity.getEntryDate());
        pstmt.setInt(index++, entity.getVendorId());
        pstmt.setInt(index++, entity.getBags());
        pstmt.setBigDecimal(index++, entity.getRate() != null ? entity.getRate() : BigDecimal.ZERO);
        pstmt.setObject(index++, entity.getWeightKg());
        pstmt.setBoolean(index++, entity.getIsLumpsum() != null && entity.getIsLumpsum());
        pstmt.setBigDecimal(index++,
                entity.getMarketFeePercent() != null ? entity.getMarketFeePercent() : BigDecimal.valueOf(0.70));
        pstmt.setBigDecimal(index++,
                entity.getCommissionPercent() != null ? entity.getCommissionPercent() : BigDecimal.valueOf(2.00));
        pstmt.setBigDecimal(index++,
                entity.getMarketFeeAmount() != null ? entity.getMarketFeeAmount() : BigDecimal.ZERO);
        pstmt.setBigDecimal(index++,
                entity.getCommissionFeeAmount() != null ? entity.getCommissionFeeAmount() : BigDecimal.ZERO);
        pstmt.setBigDecimal(index++, entity.getBaseAmount() != null ? entity.getBaseAmount() : BigDecimal.ZERO);
        pstmt.setBigDecimal(index++, entity.getGrandTotal() != null ? entity.getGrandTotal() : BigDecimal.ZERO);
        pstmt.setString(index++, entity.getNotes() != null ? entity.getNotes() : "");
        pstmt.setString(index++, entity.getPaymentMode() != null ? entity.getPaymentMode() : "CHEQUE");
        pstmt.setBoolean(index++, entity.getAdvancePaid() != null && entity.getAdvancePaid());
        pstmt.setString(index++, entity.getStatus() != null ? entity.getStatus() : "UNPAID");
        return index;
    }

    @Override
    public Optional<PurchaseEntity> findById(Integer id) {
        String sql = "SELECT * FROM purchase_entries WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding purchase by ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<PurchaseEntity> findAll() {
        List<PurchaseEntity> purchases = new ArrayList<>();
        String sql = "SELECT * FROM purchase_entries WHERE is_deleted = 0 ORDER BY entry_date DESC, id DESC";

        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                purchases.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all purchases: " + e.getMessage());
        }
        return purchases;
    }

    @Override
    public List<PurchaseEntity> findByDate(LocalDate date) {
        List<PurchaseEntity> purchases = new ArrayList<>();
        String sql = "SELECT * FROM purchase_entries WHERE DATE(entry_date) = ? AND is_deleted = 0 ORDER BY entry_date DESC, id DESC";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, date);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                purchases.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching purchases by date: " + e.getMessage());
        }
        return purchases;
    }

    @Override
    public List<PurchaseEntity> findByVendorId(Integer vendorId) {
        List<PurchaseEntity> purchases = new ArrayList<>();
        String sql = "SELECT * FROM purchase_entries WHERE vendor_id = ? AND is_deleted = 0 ORDER BY entry_date DESC, id DESC";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, vendorId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                purchases.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching purchases by vendor: " + e.getMessage());
        }
        return purchases;
    }

    @Override
    public List<PurchaseEntity> findByDateRange(LocalDate startDate, LocalDate endDate) {
        List<PurchaseEntity> purchases = new ArrayList<>();
        String sql = "SELECT * FROM purchase_entries WHERE entry_date >= ? AND entry_date <= ? AND is_deleted = 0 ORDER BY entry_date DESC, id DESC";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, startDate);
            pstmt.setObject(2, endDate);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                purchases.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching purchases by date range: " + e.getMessage());
        }
        return purchases;
    }

    @Override
    public List<PurchaseEntity> findFilteredPurchases(
            LocalDate startDate, LocalDate endDate, List<Integer> vendorIds,
            BigDecimal minAmount, BigDecimal maxAmount, Boolean chequeIssued,
            String searchQuery, int limit, int offset) {

        List<PurchaseEntity> purchases = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT p.* FROM purchase_entries p LEFT JOIN vendors v ON p.vendor_id = v.id WHERE p.is_deleted = 0");
        List<Object> params = new ArrayList<>();

        buildFilterQuery(sql, params, startDate, endDate, vendorIds, minAmount, maxAmount, chequeIssued,
                searchQuery);

        sql.append(" ORDER BY p.entry_date DESC, p.id DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    purchases.add(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching filtered purchases: " + e.getMessage());
        }
        return purchases;
    }

    @Override
    public int countFilteredPurchases(
            LocalDate startDate, LocalDate endDate, List<Integer> vendorIds,
            BigDecimal minAmount, BigDecimal maxAmount, Boolean chequeIssued,
            String searchQuery) {

        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(p.id) FROM purchase_entries p LEFT JOIN vendors v ON p.vendor_id = v.id WHERE p.is_deleted = 0");
        List<Object> params = new ArrayList<>();

        buildFilterQuery(sql, params, startDate, endDate, vendorIds, minAmount, maxAmount, chequeIssued,
                searchQuery);

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error counting filtered purchases: " + e.getMessage());
        }
        return 0;
    }

    private void buildFilterQuery(StringBuilder sql, List<Object> params,
            LocalDate startDate, LocalDate endDate, List<Integer> vendorIds,
            BigDecimal minAmount, BigDecimal maxAmount, Boolean chequeIssued,
            String searchQuery) {

        if (startDate != null) {
            sql.append(" AND p.entry_date >= ?");
            params.add(startDate);
        }
        if (endDate != null) {
            sql.append(" AND p.entry_date <= ?");
            params.add(endDate);
        }
        if (vendorIds != null && !vendorIds.isEmpty()) {
            sql.append(" AND p.vendor_id IN (");
            for (int i = 0; i < vendorIds.size(); i++) {
                sql.append("?");
                if (i < vendorIds.size() - 1)
                    sql.append(", ");
                params.add(vendorIds.get(i));
            }
            sql.append(")");
        }
        if (minAmount != null) {
            sql.append(" AND p.grand_total >= ?");
            params.add(minAmount);
        }
        if (maxAmount != null) {
            sql.append(" AND p.grand_total <= ?");
            params.add(maxAmount);
        }
        if (chequeIssued != null) {
            if (chequeIssued) {
                sql.append(" AND p.cheque_number IS NOT NULL AND p.cheque_number != ''");
            } else {
                sql.append(" AND (p.cheque_number IS NULL OR p.cheque_number = '')");
            }
        }
        if (searchQuery != null && !searchQuery.isBlank()) {
            sql.append(" AND (v.name LIKE ? OR p.cheque_number LIKE ? OR p.status LIKE ?)");
            String likeQuery = "%" + searchQuery + "%";
            params.add(likeQuery);
            params.add(likeQuery);
            params.add(likeQuery);
        }
    }

    @Override
    public List<PurchaseEntity> findTodaysPurchases() {
        return findByDate(LocalDate.now());
    }

    @Override
    public List<PurchaseEntity> findThisMonthPurchases() {
        YearMonth currentMonth = YearMonth.now();
        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();
        return findByDateRange(startDate, endDate);
    }

    @Override
    public Integer getBagsCount(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT COALESCE(SUM(bags), 0) as total_bags FROM purchase_entries " +
                "WHERE entry_date BETWEEN ? AND ? AND is_deleted = 0";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, startDate);
            pstmt.setObject(2, endDate);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("total_bags");
            }
        } catch (SQLException e) {
            System.err.println("Error getting bags count: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public Double getTotalAmount(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT COALESCE(SUM(grand_total), 0.0) as total_amount FROM purchase_entries " +
                "WHERE entry_date BETWEEN ? AND ? AND is_deleted = 0";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, startDate);
            pstmt.setObject(2, endDate);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total_amount");
            }
        } catch (SQLException e) {
            System.err.println("Error getting total amount: " + e.getMessage());
        }
        return 0.0;
    }

    @Override
    public Integer countPendingCheques(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT COUNT(*) as pending_count FROM purchase_entries " +
                "WHERE UPPER(payment_mode) = 'CHEQUE' AND status != 'PAID' " +
                "AND entry_date BETWEEN ? AND ? AND is_deleted = 0";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, startDate);
            pstmt.setObject(2, endDate);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("pending_count");
            }
        } catch (SQLException e) {
            System.err.println("Error counting pending cheques: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public List<PurchaseEntity> findByVendorAndStatus(Integer vendorId, String status) {
        List<PurchaseEntity> purchases = new ArrayList<>();
        String sql = "SELECT * FROM purchase_entries " +
                "WHERE vendor_id = ? AND status = ? AND is_deleted = 0 " +
                "ORDER BY entry_date DESC";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, vendorId);
            pstmt.setString(2, status);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                purchases.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching purchases by vendor and status: " + e.getMessage());
        }
        return purchases;
    }

    @Override
    public Integer countChequesByClearingDate(LocalDate date) {
        String sql = "SELECT COUNT(*) as clearing_count FROM purchase_entries " +
                "WHERE UPPER(payment_mode) = 'CHEQUE' AND cheque_date = ? AND is_deleted = 0";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, date);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("clearing_count");
            }
        } catch (SQLException e) {
            System.err.println("Error counting clearing cheques: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public Integer countPendingClearing() {
        String sql = "SELECT COUNT(*) as pending_count FROM purchase_entries " +
                "WHERE status = 'PAID' AND is_deleted = 0";
        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("pending_count");
            }
        } catch (SQLException e) {
            System.err.println("Error counting pending clearing: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public int getLastInsertedId() {
        String sql = "SELECT MAX(id) FROM purchase_entries";
        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void delete(Integer id) {
        String sql = "UPDATE purchase_entries SET is_deleted = 1, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error soft deleting purchase: " + e.getMessage());
            throw new RuntimeException("Failed to delete purchase", e);
        }
    }

    @Override
    public List<PurchaseEntity> findAllDeleted() {
        List<PurchaseEntity> purchases = new ArrayList<>();
        String sql = "SELECT * FROM purchase_entries WHERE is_deleted = 1 ORDER BY updated_at DESC";

        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                purchases.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching deleted purchases: " + e.getMessage());
        }
        return purchases;
    }

    @Override
    public void restore(Integer id) {
        String sql = "UPDATE purchase_entries SET is_deleted = 0, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error restoring purchase: " + e.getMessage());
            throw new RuntimeException("Failed to restore purchase", e);
        }
    }

    private PurchaseEntity mapResultSetToEntity(ResultSet rs) throws SQLException {
        return PurchaseEntity.builder()
                .id(rs.getInt("id"))
                .entryDate(rs.getObject("entry_date", LocalDate.class))
                .vendorId(rs.getInt("vendor_id"))
                .bags(rs.getInt("bags"))
                .rate(rs.getObject("rate", BigDecimal.class))
                .weightKg(rs.getObject("weight_kg", BigDecimal.class))
                .isLumpsum(rs.getBoolean("is_lumpsum"))
                .marketFeePercent(rs.getObject("market_fee_percent", BigDecimal.class))
                .commissionPercent(rs.getObject("commission_percent", BigDecimal.class))
                .marketFeeAmount(rs.getObject("market_fee_amount", BigDecimal.class))
                .commissionFeeAmount(rs.getObject("commission_amount", BigDecimal.class))
                .baseAmount(rs.getObject("base_amount", BigDecimal.class))
                .grandTotal(rs.getObject("grand_total", BigDecimal.class))
                .paymentMode(rs.getString("payment_mode"))
                .notes(rs.getString("notes"))
                .advancePaid(rs.getBoolean("advance_paid"))
                .status(rs.getString("status"))
                .createdAt(rs.getObject("created_at", LocalDateTime.class))
                .updatedAt(rs.getObject("updated_at", LocalDateTime.class))
                .chequeNumber(rs.getString("cheque_number"))
                .chequeDate(rs.getObject("cheque_date", LocalDate.class))
                .isDeleted(rs.getBoolean("is_deleted"))
                .createdByUser(rs.getString("created_by_user"))
                .build();
    }

    @Override
    public int archiveOldData(LocalDate beforeDate) {
        // Trigger backup before archiving
        LOGGER.info("Triggering automatic backup before archiving old data.");
        new com.lax.sme_manager.util.BackupService().performBackup();

        // Use explicit column names to avoid mismatch from ALTER TABLE column ordering
        String insertSql = """
                INSERT INTO purchase_entries_archive
                    (id, entry_date, vendor_id, bags, rate, weight_kg, is_lumpsum,
                     market_fee_percent, commission_percent, market_fee_amount, commission_amount,
                     base_amount, grand_total, notes, payment_mode, advance_paid, status,
                     cheque_number, cheque_date, created_by_user, is_deleted, created_at, updated_at,
                     archived_at)
                SELECT id, entry_date, vendor_id, bags, rate, weight_kg, is_lumpsum,
                       market_fee_percent, commission_percent, market_fee_amount, commission_amount,
                       base_amount, grand_total, notes, payment_mode, advance_paid, status,
                       cheque_number, cheque_date, created_by_user, is_deleted, created_at, updated_at,
                       CURRENT_TIMESTAMP
                FROM purchase_entries
                WHERE entry_date < ? AND is_deleted = 0
                """;
        String deleteSql = "DELETE FROM purchase_entries WHERE entry_date < ? AND is_deleted = 0";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            int movedCount = 0;

            try (PreparedStatement pstmtInsert = conn.prepareStatement(insertSql);
                    PreparedStatement pstmtDelete = conn.prepareStatement(deleteSql)) {

                pstmtInsert.setObject(1, beforeDate);
                movedCount = pstmtInsert.executeUpdate();

                if (movedCount > 0) {
                    pstmtDelete.setObject(1, beforeDate);
                    pstmtDelete.executeUpdate();
                }

                conn.commit();
                return movedCount;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Error archiving data: " + e.getMessage());
            e.printStackTrace();
            return -1;
        } finally {
            // Reclaim space in a separate connection with auto-commit
            try (Connection vacuumConn = DatabaseManager.getConnection();
                    Statement stmt = vacuumConn.createStatement()) {
                stmt.execute("VACUUM");
            } catch (SQLException e) {
                System.err.println("Error during VACUUM: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<PurchaseEntity> findAllArchived() {
        String sql = "SELECT * FROM purchase_entries_archive WHERE is_deleted = 0 ORDER BY entry_date DESC";
        List<PurchaseEntity> list = new java.util.ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching archived data: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean restoreFromArchive(Integer id) {
        String insertSql = "INSERT INTO purchase_entries (id, entry_date, vendor_id, bags, rate, weight_kg, is_lumpsum, "
                +
                "market_fee_percent, commission_percent, market_fee_amount, commission_amount, base_amount, grand_total, "
                +
                "notes, payment_mode, advance_paid, status, cheque_number, cheque_date, created_by_user, is_deleted, " +
                "created_at, updated_at) " +
                "SELECT id, entry_date, vendor_id, bags, rate, weight_kg, is_lumpsum, " +
                "market_fee_percent, commission_percent, market_fee_amount, commission_amount, base_amount, grand_total, "
                +
                "notes, payment_mode, advance_paid, status, cheque_number, cheque_date, created_by_user, is_deleted, " +
                "created_at, updated_at FROM purchase_entries_archive WHERE id = ?";
        String deleteSql = "DELETE FROM purchase_entries_archive WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmtInsert = conn.prepareStatement(insertSql);
                    PreparedStatement pstmtDelete = conn.prepareStatement(deleteSql)) {

                pstmtInsert.setInt(1, id);
                int inserted = pstmtInsert.executeUpdate();

                if (inserted > 0) {
                    pstmtDelete.setInt(1, id);
                    pstmtDelete.executeUpdate();
                    conn.commit();
                    return true;
                }
                conn.rollback();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Error restoring from archive: " + e.getMessage());
        }
        return false;
    }
}
