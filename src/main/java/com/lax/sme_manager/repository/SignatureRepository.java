package com.lax.sme_manager.repository;

import com.lax.sme_manager.repository.model.SignatureConfig;
import com.lax.sme_manager.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SignatureRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(SignatureRepository.class);

    public List<SignatureConfig> getAllSignatures() {
        List<SignatureConfig> list = new ArrayList<>();
        String sql = "SELECT * FROM signatures";
        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToConfig(rs));
            }
        } catch (SQLException e) {
            LOGGER.error("Error fetching signatures", e);
        }
        return list;
    }

    public SignatureConfig getSignatureById(int id) {
        String sql = "SELECT * FROM signatures WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next())
                return mapResultSetToConfig(rs);
        } catch (SQLException e) {
            LOGGER.error("Error fetching signature", e);
        }
        return null;
    }

    public void saveSignature(SignatureConfig sig) {
        if (sig.getId() > 0) {
            updateSignature(sig);
        } else {
            insertSignature(sig);
        }
    }

    private void insertSignature(SignatureConfig sig) {
        String sql = "INSERT INTO signatures (name, path, opacity, thickness, is_transparent, scale) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sig.getName());
            pstmt.setString(2, sig.getPath());
            pstmt.setDouble(3, sig.getOpacity());
            pstmt.setDouble(4, sig.getThickness());
            pstmt.setInt(5, sig.isTransparent() ? 1 : 0);
            pstmt.setDouble(6, sig.getScale());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Error inserting signature", e);
        }
    }

    private void updateSignature(SignatureConfig sig) {
        String sql = "UPDATE signatures SET name=?, path=?, opacity=?, thickness=?, is_transparent=?, scale=? WHERE id=?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sig.getName());
            pstmt.setString(2, sig.getPath());
            pstmt.setDouble(3, sig.getOpacity());
            pstmt.setDouble(4, sig.getThickness());
            pstmt.setInt(5, sig.isTransparent() ? 1 : 0);
            pstmt.setDouble(6, sig.getScale());
            pstmt.setInt(7, sig.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Error updating signature", e);
        }
    }

    public void deleteSignature(int id) {
        String sql = "DELETE FROM signatures WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Error deleting signature", e);
        }
    }

    private SignatureConfig mapResultSetToConfig(ResultSet rs) throws SQLException {
        return SignatureConfig.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .path(rs.getString("path"))
                .opacity(rs.getDouble("opacity"))
                .thickness(rs.getDouble("thickness"))
                .isTransparent(rs.getInt("is_transparent") == 1)
                .scale(rs.getDouble("scale"))
                .build();
    }
}
