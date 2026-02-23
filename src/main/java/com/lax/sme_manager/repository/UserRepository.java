package com.lax.sme_manager.repository;

import com.lax.sme_manager.domain.User;
import com.lax.sme_manager.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserRepository.class);

    public User authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    boolean passwordMatches = false;

                    // Check if the stored password is a BCrypt hash
                    if (storedHash != null && storedHash.startsWith("$2a$")) {
                        passwordMatches = BCrypt.checkpw(password, storedHash);
                    } else {
                        // Legacy plain-text fallback check
                        passwordMatches = password.equals(storedHash);
                        // Transparent upgrade to BCrypt if plain-text password is correct
                        if (passwordMatches) {
                            LOGGER.info("Upgrading legacy password to BCrypt hash for user: " + username);
                            updatePassword(rs.getInt("id"), password);
                        }
                    }

                    if (passwordMatches) {
                        return mapResultSet(rs);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Authentication error for user: " + username, e);
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to fetch all users", e);
        }
        return users;
    }

    public boolean createUser(String username, String password, User.Role role) {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, role.name());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to create user: " + username, e);
            return false;
        }
    }

    public boolean updatePassword(int userId, String newPassword) {
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, hashedPassword);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to update password for user ID: " + userId, e);
            return false;
        }
    }

    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to delete user ID: " + userId, e);
            return false;
        }
    }

    private User mapResultSet(ResultSet rs) throws SQLException {
        return User.builder()
                .id(rs.getInt("id"))
                .username(rs.getString("username"))
                .password(rs.getString("password"))
                .role(User.Role.valueOf(rs.getString("role")))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .build();
    }
}
