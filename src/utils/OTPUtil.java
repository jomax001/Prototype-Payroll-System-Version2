/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.sql.Timestamp;
import java.util.Random;

import utils.CSVUtil;
/**
 *
 * @author Jomax
 */
public class OTPUtil {
    
    // 🔐 Generate a random 6-digit OTP
    public static String generateOtp() {
        int otp = 100000 + new Random().nextInt(900000); // Generates 100000 to 999999
        return String.valueOf(otp);
    }

    // ⏳ Get current time + 1 minute (for OTP expiration)
    public static Timestamp getOtpExpiryTime() {
        long now = System.currentTimeMillis();
        return new Timestamp(now + 60_000); // 1 minute = 60,000 ms
    }

    // ✉️ Fetch user email from the database based on username
    public static String getUserEmailFromDatabase(String username) {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT email FROM users WHERE username = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("email");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Return null if email not found or error
    }
    
        // 💾 Save the OTP to database with expiration
    public static void storeOtp(String username, String otpCode, Timestamp expiresAt) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO otp_requests (id, username, otp_code, expires_at, attempts, is_verified) " +
                         "VALUES (?, ?, ?, ?, ?, ?) " +
                         "ON CONFLICT (username) DO UPDATE SET otp_code = ?, expires_at = ?, attempts = 0";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, otpCode);
            ps.setTimestamp(3, expiresAt);
            ps.setString(4, otpCode);
            ps.setTimestamp(5, expiresAt);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
// Verify the OTP code entered by the user
    public static boolean verifyOtp(String username, String inputOtp) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT otp_code, expires_at, attempts FROM otp_requests WHERE username = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedOtp = rs.getString("otp_code"); // OTP stored in DB
                Timestamp expiresAt = rs.getTimestamp("expires_at"); // Expiry time
                int attempts = rs.getInt("attempts"); // Current number of attempts

                // Check if OTP is expired
                if (System.currentTimeMillis() > expiresAt.getTime()) {
                    return false; // OTP expired
                }

                // If OTP matches
                if (storedOtp.equals(inputOtp)) {
                    return true;
                } else {
                    // Increment attempt count if incorrect
                    PreparedStatement update = conn.prepareStatement("UPDATE otp_requests SET attempts = attempts + 1 WHERE username = ?");
                    update.setString(1, username);
                    update.executeUpdate();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false; // OTP is invalid or error occurred
    }

public static void saveOtpToDatabase(String username, String otp, Timestamp expiration, int maxAttempts) {
    try (Connection conn = DBConnection.getConnection()) {
        String sql = "INSERT INTO otp_codes (username, otp_code, expiration_time, attempts_count) " +
             "VALUES (?, ?, ?, 0) " +
             "ON CONFLICT (username) DO UPDATE SET otp_code = ?, expiration_time = ?, attempts_count = 0";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, username);     // username
        ps.setString(2, otp);          // otp_code
        ps.setTimestamp(3, expiration); // expiration_time

        ps.setString(4, otp);          // otp_code
        ps.setTimestamp(5, expiration); // expiration_time

        ps.executeUpdate();
    } catch (Exception e) {
        e.printStackTrace();
    }
}
    
    // Save OTP to database for tracking
public static void storeOtpInDatabase(String username, String otp, Timestamp expiresAt) {
    try (Connection conn = DBConnection.getConnection()) {
        String sql = "INSERT INTO otp_requests (id, username, otp_code, expires_at, attempts, is_verified) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, username);
        ps.setString(2, otp);
        ps.setTimestamp(3, expiresAt);
        ps.executeUpdate();
    } catch (Exception e) {
        e.printStackTrace();
    }
}

// ✅ Use this if you want to extract user email from users.csv
public static String getUserEmailFromCSV(String username) {
    // Read all rows from users.csv file
    List<String[]> rows = CSVUtil.readCSV("data/users.csv");

    // Loop through each row (skip header)
    for (int i = 1; i < rows.size(); i++) {
        String[] row = rows.get(i); // Get one user's row

        // Check if username matches
        if (row[0].equalsIgnoreCase(username)) {
            if (row.length > 22) { // Make sure email column exists
                System.out.println("✅ Found email: " + row[22]); // For debugging
                return row[22].trim(); // Return the found email
            } else {
                System.out.println("⚠️ Email column missing for user: " + username);
                return null;
            }
        }
    }

    // Username not found
    System.out.println("❌ Username not found in CSV: " + username);
    return null;
}


    public static String generateOtpCode() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public static String getExpiryTime(int i) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }


}