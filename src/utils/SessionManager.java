package utils;

import java.sql.*; // ✅ Needed for DB operations

public class SessionManager {

    private static String currentUsername; // Store the logged-in username
    private static String currentRole;     // Store the logged-in user role
    private static String token;           // Store the JWT token

    // ✅ Set session data after successful login
    public static void setSession(String username, String role, String jwtToken) {
        currentUsername = username;
        currentRole = role;
        token = jwtToken;
    }

    // ✅ Sets the current username
    public static void setUsername(String username) {
        currentUsername = username;
    }

    // ✅ Returns the current username
    public static String getUsername() {
        return currentUsername;
    }

    // ✅ Sets the current user role
    public static void setRole(String role) {
        currentRole = role;
    }

    // ✅ Returns the current user role
    public static String getRole() {
        return currentRole;
    }

    // ✅ Sets the JWT token
    public static void setToken(String jwtToken) {
        token = jwtToken;
    }

    // ✅ Returns the JWT token
    public static String getToken() {
        return token;
    }

    // ✅ Clears all session data (used on logout)
    public static void logout() {
        currentUsername = null;
        currentRole = null;
        token = null;
    }

    /**
     * ✅ Checks if the given username has an active session (already logged in).
     * Returns true if a session exists in the database.
     */
    public static boolean hasActiveSession(String username) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM active_sessions WHERE username = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            return rs.next(); // If a row exists, session is active
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
 * Deletes all expired sessions from the active_sessions table.
 * This helps remove sessions where the token is already expired.
 */
public static void cleanupExpiredSessions() {
    try (Connection conn = DBConnection.getConnection()) {
        // Delete sessions where expiration time is already passed
        String sql = "DELETE FROM active_sessions WHERE expires_at < NOW()";
        PreparedStatement ps = conn.prepareStatement(sql);

        // Execute and get how many rows were deleted
        int deleted = ps.executeUpdate();

        // Print based on result
        if (deleted > 0) {
            System.out.println("✅ Expired sessions deleted: " + deleted);
        } else {
            System.out.println("⚠️ No expired sessions were deleted. (Maybe none are expired yet.)");
        }
    } catch (Exception e) {
        System.err.println("❌ Error while deleting expired sessions: " + e.getMessage());
        e.printStackTrace();
    }
}

/**
 * ✅ Saves the session to the active_sessions table with an expiration timestamp.
 * This allows me to track valid sessions and auto-expire them after 20 minutes.
 */
public static void saveSessionToDatabase(String username, String token, String role) {
    try (Connection conn = DBConnection.getConnection()) {
        String sql = "INSERT INTO active_sessions (username, token, role, expires_at) VALUES (?, ?, ?, ?)";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, username);
        ps.setString(2, token);
        ps.setString(3, role);

        // Set expires_at to current time + 20 minutes
        Timestamp expiresAt = Timestamp.valueOf(java.time.LocalDateTime.now().plusMinutes(20));
        ps.setTimestamp(4, expiresAt);

        ps.executeUpdate();
        System.out.println("✅ Session saved to database with expiration: " + expiresAt);
    } catch (Exception e) {
        System.err.println("❌ Failed to save session to database: " + e.getMessage());
        e.printStackTrace();
    }
}

/**
 * Deletes all expired remember tokens from the database.
 */
public static void cleanupExpiredRememberTokens() {
    try (Connection conn = DBConnection.getConnection()) {
        String sql = "DELETE FROM remember_tokens WHERE expires_at < NOW()";
        PreparedStatement ps = conn.prepareStatement(sql);
        int deleted = ps.executeUpdate();
        System.out.println(" Expired remember tokens deleted: " + deleted);
    } catch (Exception e) {
        System.err.println("❌ Failed to clean up expired remember tokens: " + e.getMessage());
        e.printStackTrace();
    }
}

}
