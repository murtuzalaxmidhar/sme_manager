package com.lax.sme_manager.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer id;
    private String username;
    private String password; // hashed
    private Role role;
    private LocalDateTime createdAt;

    public enum Role {
        ADMIN, OPERATOR;

        public boolean isAdmin() {
            return this == ADMIN;
        }
    }

    public boolean isAdmin() {
        return role != null && role.isAdmin();
    }
}
