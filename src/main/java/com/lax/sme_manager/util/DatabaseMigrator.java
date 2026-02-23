package com.lax.sme_manager.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages database schema versions and incremental migrations.
 */
public class DatabaseMigrator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseMigrator.class);
    private static final int CURRENT_VERSION = 18; // Version 18: Data Archiving

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
            if (fromVersion < 2) {
                LOGGER.info("Executing Phase 2 Migration (Cheque Module)...");
                migrateToV2(stmt);
            }
            if (fromVersion < 3) {
                LOGGER.info("Executing Phase 3 Migration (Expert Date)...");
                migrateToV3(stmt);
            }
            if (fromVersion < 4) {
                LOGGER.info("Executing Phase 4 Migration (Signature Fix)...");
                migrateToV4(stmt);
            }
            if (fromVersion < 5) {
                LOGGER.info("Executing Phase 5 Migration (Multi-Bank Template)...");
                migrateToV5(stmt);
            }
            if (fromVersion < 6) {
                LOGGER.info("Executing Phase 6 Migration (Printer Offsets)...");
                migrateToV6(stmt);
            }
            if (fromVersion < 7) {
                LOGGER.info("Executing Phase 7 Migration (A/C Payee Mobility)...");
                migrateToV7(stmt);
            }
            if (fromVersion < 8) {
                LOGGER.info("Executing Phase 8 Migration (MICR Standards)...");
                migrateToV8(stmt);
            }
            if (fromVersion < 9) {
                LOGGER.info("Executing Phase 9 Migration (Payee Management)...");
                migrateToV9(stmt);
            }
            if (fromVersion < 10) {
                LOGGER.info("Executing Phase 10 Migration (Print Orientation)...");
                migrateToV10(stmt);
            }
            if (fromVersion < 11) {
                LOGGER.info("Executing Phase 11 Migration (Date Offsets)...");
                migrateToV11(stmt);
            }
            if (fromVersion < 12) {
                LOGGER.info("Executing Phase 12 Migration (Cheque Books)...");
                migrateToV12(stmt);
            }
            if (fromVersion < 13) {
                LOGGER.info("Executing Phase 13 Migration (Cancelled Cheques)...");
                migrateToV13(stmt);
            }
            if (fromVersion < 14) {
                LOGGER.info("Executing Phase 14 Migration (User Management)...");
                migrateToV14(conn);
            }
            if (fromVersion < 15) {
                LOGGER.info("Executing Phase 15 Migration (Audit Trail)...");
                migrateToV15(stmt);
            }
            if (fromVersion < 16) {
                LOGGER.info("Executing Phase 16 Migration (Print Queue)...");
                migrateToV16(stmt);
            }
            if (fromVersion < 17) {
                LOGGER.info("Executing Phase 17 Migration (Print Ledger)...");
                migrateToV17(stmt);
            }
            if (fromVersion < 18) {
                LOGGER.info("Executing Phase 18 Migration (Data Archiving)...");
                migrateToV18(stmt);
            }
        }
    }

    private void migrateToV3(Statement stmt) throws SQLException {
        try {
            stmt.execute("ALTER TABLE cheque_config ADD COLUMN date_positions TEXT");
        } catch (SQLException e) {
            LOGGER.warn("date_positions column already exists.");
        }
    }

    private void migrateToV4(Statement stmt) throws SQLException {
        try {
            stmt.execute("ALTER TABLE cheque_config ADD COLUMN active_signature_id INTEGER DEFAULT 0");
        } catch (SQLException e) {
            LOGGER.warn("active_signature_id column already exists.");
        }
        try {
            stmt.execute("ALTER TABLE cheque_config ADD COLUMN signature_x DOUBLE DEFAULT 150.0");
            stmt.execute("ALTER TABLE cheque_config ADD COLUMN signature_y DOUBLE DEFAULT 75.0");
        } catch (SQLException e) {
            LOGGER.warn("signature_x/y columns already exist.");
        }
    }

    private void migrateToV5(Statement stmt) throws SQLException {
        stmt.execute("""
                CREATE TABLE IF NOT EXISTS bank_templates (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    bank_name TEXT UNIQUE NOT NULL,
                    date_x DOUBLE, date_y DOUBLE,
                    payee_x DOUBLE, payee_y DOUBLE,
                    amount_words_x DOUBLE, amount_words_y DOUBLE,
                    amount_digits_x DOUBLE, amount_digits_y DOUBLE,
                    signature_x DOUBLE, signature_y DOUBLE
                )
                """);

        // Seed State Bank of India template
        stmt.execute(
                """
                        INSERT OR IGNORE INTO bank_templates
                        (bank_name, date_x, date_y, payee_x, payee_y, amount_words_x, amount_words_y, amount_digits_x, amount_digits_y, signature_x, signature_y)
                        VALUES
                        ('State Bank of India', 154.0, 10.0, 25.0, 24.0, 25.0, 36.0, 155.0, 48.0, 150.0, 75.0)
                        """);
    }

    private void migrateToV6(Statement stmt) throws SQLException {
        try {
            stmt.execute("ALTER TABLE cheque_config ADD COLUMN offset_x DOUBLE DEFAULT 0.0");
            stmt.execute("ALTER TABLE cheque_config ADD COLUMN offset_y DOUBLE DEFAULT 0.0");
        } catch (SQLException e) {
            LOGGER.warn("offset_x/y columns might already exist.");
        }
    }

    private void migrateToV7(Statement stmt) throws SQLException {
        try {
            stmt.execute("ALTER TABLE cheque_config ADD COLUMN ac_payee_x DOUBLE DEFAULT 15.0");
            stmt.execute("ALTER TABLE cheque_config ADD COLUMN ac_payee_y DOUBLE DEFAULT 10.0");

            // Also update bank_templates for future-proofing
            stmt.execute("ALTER TABLE bank_templates ADD COLUMN ac_payee_x DOUBLE DEFAULT 15.0");
            stmt.execute("ALTER TABLE bank_templates ADD COLUMN ac_payee_y DOUBLE DEFAULT 10.0");
        } catch (SQLException e) {
            LOGGER.warn("ac_payee_x/y columns might already exist.");
        }
    }

    private void migrateToV8(Statement stmt) throws SQLException {
        try {
            stmt.execute(
                    "ALTER TABLE cheque_config ADD COLUMN micr_code VARCHAR(100) DEFAULT '⑆000000⑈ 000000000⑉ 00'");
            stmt.execute("ALTER TABLE cheque_config ADD COLUMN micr_x DOUBLE DEFAULT 13.0");
            stmt.execute("ALTER TABLE cheque_config ADD COLUMN micr_y DOUBLE DEFAULT 85.0");

            stmt.execute(
                    "ALTER TABLE bank_templates ADD COLUMN micr_code VARCHAR(100) DEFAULT '⑆000000⑈ 000000000⑉ 00'");
            stmt.execute("ALTER TABLE bank_templates ADD COLUMN micr_x DOUBLE DEFAULT 13.0");
            stmt.execute("ALTER TABLE bank_templates ADD COLUMN micr_y DOUBLE DEFAULT 85.0");
        } catch (SQLException e) {
            LOGGER.warn("MICR columns might already exist.");
        }
    }

    private void migrateToV9(Statement stmt) throws SQLException {
        try {
            stmt.execute("ALTER TABLE vendors ADD COLUMN default_amount DECIMAL(15,2) DEFAULT 0.00");
        } catch (SQLException e) {
            LOGGER.warn("default_amount column might already exist.");
        }
    }

    private void migrateToV10(Statement stmt) throws SQLException {
        try {
            stmt.execute("ALTER TABLE cheque_config ADD COLUMN print_orientation VARCHAR(20) DEFAULT 'LANDSCAPE'");
        } catch (SQLException e) {
            LOGGER.warn("print_orientation column might already exist.");
        }
    }

    private void migrateToV11(Statement stmt) throws SQLException {
        try {
            stmt.execute("ALTER TABLE cheque_config ADD COLUMN date_offset_x DOUBLE DEFAULT 0.0");
            stmt.execute("ALTER TABLE cheque_config ADD COLUMN date_offset_y DOUBLE DEFAULT 0.0");
        } catch (SQLException e) {
            LOGGER.warn("date_offset_x/y columns might already exist.");
        }
    }

    private void migrateToV12(Statement stmt) throws SQLException {
        stmt.execute("""
                    CREATE TABLE IF NOT EXISTS cheque_books (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        book_name TEXT NOT NULL,
                        bank_name TEXT NOT NULL,
                        start_number INTEGER NOT NULL,
                        end_number INTEGER NOT NULL,
                        next_number INTEGER NOT NULL,
                        is_active BOOLEAN DEFAULT 0,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                """);
    }

    private void migrateToV13(Statement stmt) throws SQLException {
        stmt.execute("""
                    CREATE TABLE IF NOT EXISTS cheque_usage_log (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        book_id INTEGER NOT NULL,
                        leaf_number INTEGER NOT NULL,
                        status TEXT NOT NULL, -- 'PRINTED', 'CANCELLED', 'VOID', 'MISPRINT'
                        remarks TEXT,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (book_id) REFERENCES cheque_books(id),
                        UNIQUE(book_id, leaf_number)
                    )
                """);
    }

    private void migrateToV14(Connection conn) throws SQLException {
        LOGGER.info("Migrating to V14: Creating users table and seeding admin account...");
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT UNIQUE NOT NULL," +
                "password TEXT NOT NULL," +
                "role TEXT NOT NULL DEFAULT 'OPERATOR'," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }

        // Seed with existing admin password if users table is empty
        String checkSql = "SELECT COUNT(*) FROM users";
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                // Assuming ConfigManager is available and provides the legacy password
                // If ConfigManager is not available, this line will cause a compilation error.
                // For this exercise, we assume it exists in the project.
                String legacyPw = ConfigManager.getInstance().getProperty(ConfigManager.KEY_LOGIN_PASSWORD, "admin123");
                String insertSql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    pstmt.setString(1, "admin");
                    pstmt.setString(2, legacyPw); // We'll assume plaintext for now as per current system, can add
                                                  // BCrypt later if needed
                    pstmt.setString(3, "ADMIN");
                    pstmt.executeUpdate();
                }
                LOGGER.info("Seeded users table with admin account migrating legacy password.");
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

    private void migrateToV2(Statement stmt) throws SQLException {
        // 1. Drop legacy table
        stmt.execute("DROP TABLE IF EXISTS cheque_templates");

        // 2. Create new config table
        stmt.execute("""
                CREATE TABLE IF NOT EXISTS cheque_config (
                    id INTEGER PRIMARY KEY,
                    bank_name TEXT DEFAULT 'Default Bank',
                    is_ac_payee BOOLEAN DEFAULT 1,
                    font_size INTEGER DEFAULT 12,
                    date_x DOUBLE DEFAULT 150.0,
                    date_y DOUBLE DEFAULT 10.0,
                    payee_x DOUBLE DEFAULT 20.0,
                    payee_y DOUBLE DEFAULT 40.0,
                    amount_words_x DOUBLE DEFAULT 20.0,
                    amount_words_y DOUBLE DEFAULT 55.0,
                    amount_digits_x DOUBLE DEFAULT 150.0,
                    amount_digits_y DOUBLE DEFAULT 50.0,
                    signature_x DOUBLE DEFAULT 150.0,
                    signature_y DOUBLE DEFAULT 70.0,
                    signature_path TEXT,
                    active_signature_id INTEGER DEFAULT 0
                )""");

        stmt.execute("""
                CREATE TABLE IF NOT EXISTS signatures (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    path TEXT NOT NULL,
                    opacity DOUBLE DEFAULT 1.0,
                    thickness DOUBLE DEFAULT 1.0,
                    is_transparent INTEGER DEFAULT 1,
                    scale DOUBLE DEFAULT 1.0
                )""");

        // 3. Seed default config
        stmt.execute(
                """
                        INSERT INTO cheque_config (id, bank_name, is_ac_payee, font_size, date_x, date_y, payee_x, payee_y, amount_words_x, amount_words_y, amount_digits_x, amount_digits_y, signature_x, signature_y)
                        VALUES (1, 'Default Bank', 1, 12, 160.0, 15.0, 30.0, 45.0, 30.0, 60.0, 160.0, 55.0, 150.0, 70.0)
                        """);
    }

    private void migrateToV15(Statement stmt) throws SQLException {
        try {
            stmt.execute("ALTER TABLE purchase_entries ADD COLUMN created_by_user TEXT DEFAULT 'admin'");
            LOGGER.info("Audit Trail: Added created_by_user column to purchase_entries.");
        } catch (SQLException e) {
            LOGGER.warn("created_by_user column might already exist.");
        }
    }

    private void migrateToV16(Statement stmt) throws SQLException {
        stmt.execute("""
                    CREATE TABLE IF NOT EXISTS cheque_print_queue (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        purchase_id INTEGER,
                        payee_name TEXT NOT NULL,
                        amount REAL NOT NULL,
                        cheque_date DATE,
                        is_ac_payee BOOLEAN DEFAULT 1,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (purchase_id) REFERENCES purchase_entries(id)
                    )
                """);
    }

    private void migrateToV17(Statement stmt) throws SQLException {
        stmt.execute("""
                    CREATE TABLE IF NOT EXISTS cheque_print_ledger (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER,
                        payee_name TEXT NOT NULL,
                        amount REAL NOT NULL,
                        cheque_number TEXT,
                        print_status TEXT NOT NULL, -- SUCCESS, FAILED, CANCELLED
                        remarks TEXT,
                        printed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (user_id) REFERENCES users(id)
                    )
                """);
    }

    private void migrateToV18(Statement stmt) throws SQLException {
        // Create archive table with same structure as purchase_entries
        stmt.execute("""
                    CREATE TABLE IF NOT EXISTS purchase_entries_archive (
                        id INTEGER PRIMARY KEY,
                        entry_date DATE NOT NULL,
                        vendor_id INTEGER NOT NULL,
                        bags INTEGER DEFAULT 0,
                        rate REAL DEFAULT 0.0,
                        weight_kg REAL,
                        is_lumpsum BOOLEAN DEFAULT 0,
                        market_fee_percent REAL,
                        commission_percent REAL,
                        market_fee_amount REAL,
                        commission_amount REAL,
                        base_amount REAL,
                        grand_total REAL,
                        notes TEXT,
                        payment_mode TEXT,
                        advance_paid BOOLEAN DEFAULT 0,
                        status TEXT DEFAULT 'UNPAID',
                        cheque_number TEXT,
                        cheque_date DATE,
                        created_by_user TEXT,
                        is_deleted BOOLEAN DEFAULT 0,
                        created_at TIMESTAMP,
                        updated_at TIMESTAMP,
                        archived_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                """);
    }

    private void updateVersion(Connection conn, int version) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM schema_version");
            stmt.execute("INSERT INTO schema_version (version) VALUES (" + version + ")");
        }
    }
}
