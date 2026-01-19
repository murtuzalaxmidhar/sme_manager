package com.lax.sme_manager.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages database schema versions and incremental migrations.
 */
public class DatabaseMigrator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseMigrator.class);
    private static final int CURRENT_VERSION = 1; // Version 1: Production Ready Base Schema

    public void migrate() {
        try (Connection conn = DatabaseManager.getConnection()) {
            int dbVersion = getDatabaseVersion(conn);

            if (dbVersion < CURRENT_VERSION) {
                LOGGER.info("Migrating database from version {} to {}", dbVersion, CURRENT_VERSION);
                performMigration(conn, dbVersion);
                updateVersion(conn, CURRENT_VERSION);
                LOGGER.info("Database migration successful.");
            } else {
                LOGGER.info("Database is up to date (version {})", dbVersion);
            }
        } catch (SQLException e) {
            LOGGER.error("Migration failed", e);
            throw new RuntimeException("Could not migrate database", e);
        }
    }

    private int getDatabaseVersion(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS schema_version (version INTEGER PRIMARY KEY)");
            ResultSet rs = stmt.executeQuery("SELECT version FROM schema_version");
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.warn("Could not retrieve version, assuming 0");
        }
        return 0;
    }

    private void performMigration(Connection conn, int fromVersion) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            if (fromVersion < 1) {
                LOGGER.info("Executing Phase 1 Schema Creation...");
                createBaseSchema(stmt);
            }
        }
    }

    private void createBaseSchema(Statement stmt) throws SQLException {
        // Vendors
        stmt.execute("""
                CREATE TABLE IF NOT EXISTS vendors (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT UNIQUE NOT NULL,
                    contact_person TEXT,
                    phone TEXT,
                    email TEXT,
                    address TEXT,
                    notes TEXT,
                    is_deleted BOOLEAN DEFAULT 0,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )""");

        // Purchase Entries
        stmt.execute("""
                CREATE TABLE IF NOT EXISTS purchase_entries (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    entry_date DATE NOT NULL,
                    vendor_id INTEGER NOT NULL,
                    bags INTEGER NOT NULL,
                    rate REAL NOT NULL,
                    weight_kg REAL,
                    is_lumpsum BOOLEAN DEFAULT 0,
                    market_fee_percent REAL DEFAULT 0.70,
                    commission_percent REAL DEFAULT 2.00,
                    market_fee_amount REAL DEFAULT 0.0,
                    commission_amount REAL DEFAULT 0.0,
                    base_amount REAL NOT NULL,
                    grand_total REAL NOT NULL,
                    notes TEXT,
                    payment_mode TEXT,
                    advance_paid BOOLEAN DEFAULT 0,
                    status TEXT,
                    cheque_number TEXT,
                    cheque_date DATE,
                    is_deleted BOOLEAN DEFAULT 0,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (vendor_id) REFERENCES vendors(id)
                )""");

        // Settings / Config table (for app-internal state if needed)
        stmt.execute("CREATE TABLE IF NOT EXISTS app_config (key TEXT PRIMARY KEY, value TEXT)");

        // Initial table creation
        createChequeTemplatesTable(stmt);
    }

    private void createChequeTemplatesTable(Statement stmt) throws SQLException {
        stmt.execute("""
                CREATE TABLE IF NOT EXISTS cheque_templates (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    bank_name TEXT NOT NULL,
                    template_name TEXT NOT NULL,
                    bg_path TEXT,
                    date_x DOUBLE, date_y DOUBLE,
                    payee_x DOUBLE, payee_y DOUBLE,
                    amount_words_x DOUBLE, amount_words_y DOUBLE,
                    amount_digits_x DOUBLE, amount_digits_y DOUBLE,
                    signature_x DOUBLE, signature_y DOUBLE,
                    font_size INTEGER DEFAULT 16,
                    font_color TEXT DEFAULT '#000000',
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    date_x_mm REAL,
                    date_y_mm REAL,
                    payee_x_mm REAL,
                    payee_y_mm REAL,
                    amount_words_x_mm REAL,
                    amount_words_y_mm REAL,
                    amount_digits_x_mm REAL,
                    amount_digits_y_mm REAL,
                    signature_x_mm REAL,
                    signature_y_mm REAL
                )""");
    }

    private void updateVersion(Connection conn, int version) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM schema_version");
            stmt.execute("INSERT INTO schema_version (version) VALUES (" + version + ")");
        }
    }
}
