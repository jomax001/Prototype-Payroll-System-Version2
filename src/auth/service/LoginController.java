package auth.service;

import java.sql.*;
import javax.swing.JOptionPane;
import utils.DBConnection;
import utils.EmailUtil;
import utils.JWTUtil;
import utils.SessionManager;

public class LoginController {

    // This method handles the login logic and returns a String result
    public static String login(String username, String password, String role) {
        try (Connection conn = DBConnection.getConnection()) {

            // Check if the user exists with the given username and role
            String checkUser = "SELECT * FROM users WHERE username = ? AND role = ?";
            PreparedStatement ps = conn.prepareStatement(checkUser);
            ps.setString(1, username);
            ps.setString(2, role);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Get account lock status and time
                boolean isLocked = rs.getBoolean("account_locked");
                int failedAttempts = rs.getInt("failed_attempts");
                Timestamp lockTime = rs.getTimestamp("lock_time");

                // If the account is locked
                if (isLocked) {
                    long lockDuration = System.currentTimeMillis() - lockTime.getTime();

                    // If 24 hours have passed, unlock the account
                    if (lockDuration >= 86400000) {
                        resetLock(conn, username); // reset lock status
                    } else {
                        return "locked"; // account still locked
                    }
                }

                // Check if password matches
                String dbPassword = rs.getString("password");
                if (password.equals(dbPassword)) {
                    resetLock(conn, username); // reset attempts if correct

                    // I generate a token to secure the session
                    String token = JWTUtil.generateToken(username);

                    // store the token in the database so we can use it later
                    String updateTokenSQL = "UPDATE users SET jwt_token = ? WHERE username = ?";
                    PreparedStatement tokenStmt = conn.prepareStatement(updateTokenSQL);
                    tokenStmt.setString(1, token);
                    tokenStmt.setString(2, username);
                    tokenStmt.executeUpdate();

                    // set the session with username, role, and the token
                    SessionManager.setSession(username, role, token);

                    return "success"; // login passed
                } else {
                    incrementFailedAttempts(conn, username, failedAttempts); // add to failed count
                    return "invalid"; // wrong password
                }
            } else {
                return "not_found"; // no user found
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "error"; // return error if exception occurs
        }
    }

    // This method resets the lock state of a user after successful login or 24 hours passed
    private static void resetLock(Connection conn, String username) {
        try {
            // Update the user record to remove lock and reset failed attempts
            String sql = "UPDATE users SET failed_attempts = 0, account_locked = false, lock_time = NULL WHERE username = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            // If something goes wrong, show error
            JOptionPane.showMessageDialog(null, "Failed to reset user lock status: " + e.getMessage());
        }
    }

    // This method increases the failed login attempts and locks the account after 3 tries
    private static void incrementFailedAttempts(Connection conn, String username, int current) {
        try {
            current++; // adding 1 to the current failed attempts

            // If user failed 3 times already
            if (current >= 3) {
                // get the current time
                Timestamp now = new Timestamp(System.currentTimeMillis());

                // set lockedUntil to 24 hours from now
                Timestamp lockedUntil = new Timestamp(System.currentTimeMillis() + (24 * 60 * 60 * 1000));

                // lock the account and store lock time and unlock time
                String lockSQL = "UPDATE users SET account_locked = true, lock_time = ?, locked_until = ?, failed_attempts = ? WHERE username = ?";
                PreparedStatement lockStmt = conn.prepareStatement(lockSQL);
                lockStmt.setTimestamp(1, now);
                lockStmt.setTimestamp(2, lockedUntil); // ✅ This is used by the GUI timer
                lockStmt.setInt(3, current);
                lockStmt.setString(4, username);
                lockStmt.executeUpdate();

                // get the user's email so I can notify them
                PreparedStatement emailStmt = conn.prepareStatement("SELECT email FROM users WHERE username = ?");
                emailStmt.setString(1, username);
                ResultSet rs = emailStmt.executeQuery();
                if (rs.next()) {
                    String userEmail = rs.getString("email");
                    EmailUtil.sendLockNotification(userEmail); // I send the lock email here
                }

                // show a message so the user knows what happened
                JOptionPane.showMessageDialog(null, "Your account has been locked due to 3 failed login attempts.\nPlease check your email or contact your admin.");
            } else {
                // If failed attempts are less than 3, I just update the count
                String updateSQL = "UPDATE users SET failed_attempts = ? WHERE username = ?";
                PreparedStatement ps = conn.prepareStatement(updateSQL);
                ps.setInt(1, current);
                ps.setString(2, username);
                ps.executeUpdate();
            }

        } catch (SQLException e) {
            // show an error if something went wrong
            JOptionPane.showMessageDialog(null, "Error updating failed attempts: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
