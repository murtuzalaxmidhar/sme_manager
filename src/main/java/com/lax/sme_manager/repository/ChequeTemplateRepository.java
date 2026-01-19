package com.lax.sme_manager.repository;

import com.lax.sme_manager.domain.ChequeTemplate;
import com.lax.sme_manager.util.DatabaseManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChequeTemplateRepository {

    public void save(ChequeTemplate template) {
        String sql = "INSERT OR REPLACE INTO cheque_templates (id, bank_name, template_name, bg_path, " +
                "date_x, date_y, payee_x, payee_y, amount_words_x, amount_words_y, " +
                "amount_digits_x, amount_digits_y, signature_x, signature_y, font_size, font_color) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (template.getId() != null && template.getId() > 0) {
                pstmt.setInt(1, template.getId());
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }

            pstmt.setString(2, template.getBankName());
            pstmt.setString(3, template.getTemplateName());
            pstmt.setString(4, template.getBackgroundImagePath());
            pstmt.setDouble(5, template.getDateX());
            pstmt.setDouble(6, template.getDateY());
            pstmt.setDouble(7, template.getPayeeX());
            pstmt.setDouble(8, template.getPayeeY());
            pstmt.setDouble(9, template.getAmountWordsX());
            pstmt.setDouble(10, template.getAmountWordsY());
            pstmt.setDouble(11, template.getAmountDigitsX());
            pstmt.setDouble(12, template.getAmountDigitsY());
            pstmt.setDouble(13, template.getSignatureX());
            pstmt.setDouble(14, template.getSignatureY());
            pstmt.setInt(15, template.getFontSize());
            pstmt.setString(16, template.getFontColor());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving cheque template", e);
        }
    }

    public List<ChequeTemplate> findAll() {
        List<ChequeTemplate> templates = new ArrayList<>();
        String sql = "SELECT * FROM cheque_templates ORDER BY bank_name, template_name";
        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                templates.add(mapResultSetToTemplate(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching templates", e);
        }
        return templates;
    }

    public List<String> findAllBanks() {
        List<String> banks = new ArrayList<>();
        String sql = "SELECT DISTINCT bank_name FROM cheque_templates ORDER BY bank_name";
        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                banks.add(rs.getString(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching banks", e);
        }
        return banks;
    }

    public List<ChequeTemplate> findByBank(String bankName) {
        List<ChequeTemplate> templates = new ArrayList<>();
        String sql = "SELECT * FROM cheque_templates WHERE bank_name = ? ORDER BY template_name";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bankName);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    templates.add(mapResultSetToTemplate(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching templates for bank: " + bankName, e);
        }
        return templates;
    }

    private ChequeTemplate mapResultSetToTemplate(ResultSet rs) throws SQLException {
        return ChequeTemplate.builder()
                .id(rs.getInt("id"))
                .bankName(rs.getString("bank_name"))
                .templateName(rs.getString("template_name"))
                .backgroundImagePath(rs.getString("bg_path"))
                .dateX(rs.getDouble("date_x"))
                .dateY(rs.getDouble("date_y"))
                .payeeX(rs.getDouble("payee_x"))
                .payeeY(rs.getDouble("payee_y"))
                .amountWordsX(rs.getDouble("amount_words_x"))
                .amountWordsY(rs.getDouble("amount_words_y"))
                .amountDigitsX(rs.getDouble("amount_digits_x"))
                .amountDigitsY(rs.getDouble("amount_digits_y"))
                .signatureX(rs.getDouble("signature_x"))
                .signatureY(rs.getDouble("signature_y"))
                .fontSize(rs.getInt("font_size"))
                .fontColor(rs.getString("font_color"))
                .fontFamily("Inter")
                .build();
    }
}
