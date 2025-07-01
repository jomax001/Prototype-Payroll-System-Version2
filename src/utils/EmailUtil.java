package utils;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class EmailUtil {

    /**
     * ✅ Sends an account lock email if user failed login too many times.
     * This email warns the user that their account is locked.
     */
    public static void sendLockNotification(String toEmail) {
        String fromEmail = null; // The email address used to send
        String password = null;  // The app password used for authentication

        // 🔹 Step 1: Get sender email and password from database table `email_config`
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT sender_email, app_password FROM email_config LIMIT 1";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                fromEmail = rs.getString("sender_email");
                password = rs.getString("app_password");
            } else {
                // No email config found
                System.err.println("❌ No email config found in the database.");
                logEmail(toEmail, "Account Locked - FinMark Payroll System", "FAILED", "No email config found");
                return;
            }
        } catch (SQLException e) {
            // Error while reading from database
            System.err.println("❌ Error fetching email config: " + e.getMessage());
            e.printStackTrace();
            logEmail(toEmail, "Account Locked - FinMark Payroll System", "FAILED", e.getMessage());
            return;
        }

        // 🔹 Step 2: Stop if email or password is missing
        if (fromEmail == null || password == null) {
            System.err.println("❌ Email or password is missing.");
            logEmail(toEmail, "Account Locked - FinMark Payroll System", "FAILED", "Missing email or password");
            return;
        }

        // 🔹 Step 3: Set up Gmail SMTP server properties
        final String finalEmail = fromEmail;
        final String finalPass = password;

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");        // Gmail server
        props.put("mail.smtp.port", "587");                   // TLS port
        props.put("mail.smtp.auth", "true");                  // Requires login
        props.put("mail.smtp.starttls.enable", "true");       // Enables TLS encryption
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");   // Trust Gmail server

        // 🔹 Step 4: Create email session with login authentication
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(finalEmail, finalPass);
            }
        });

        try {
            // 🔹 Step 5: Compose the email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(finalEmail));                    // Sender email
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail)); // Receiver
            message.setSubject("Account Locked - FinMark Payroll System");      // Subject of email
            message.setText("Your FinMark account has been locked for 24 hours due to 3 failed login attempts.\n\n"
                    + "If this was not you, please contact your system administrator.");

            // 🔹 Step 6: Send the email
            Transport.send(message);
            System.out.println("✅ Lock notification email sent to: " + toEmail);

            // 🔹 Step 7: Save success log to database
            logEmail(toEmail, "Account Locked - FinMark Payroll System", "SENT", null);

        } catch (MessagingException e) {
            // 🔹 If email failed to send, show error and log to database
            System.err.println("❌ Failed to send lock email: " + e.getMessage());
            e.printStackTrace();
            logEmail(toEmail, "Account Locked - FinMark Payroll System", "FAILED", e.getMessage());
        }
    }

    /**
     * ✅ Sends a One-Time Password (OTP) to user's email during login (2FA)
     */
    public static void sendOtpCode(String toEmail, String otpCode) {
        String fromEmail = null;
        String password = null;

        // 🔹 Step 1: Get sender credentials from database
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT sender_email, app_password FROM email_config LIMIT 1";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                fromEmail = rs.getString("sender_email");
                password = rs.getString("app_password");
            } else {
                System.err.println("❌ No email config found in the database.");
                logEmail(toEmail, "Your OTP Code - FinMark Payroll System", "FAILED", "No email config found");
                return;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error fetching email config: " + e.getMessage());
            e.printStackTrace();
            logEmail(toEmail, "Your OTP Code - FinMark Payroll System", "FAILED", e.getMessage());
            return;
        }

        if (fromEmail == null || password == null) {
            System.err.println("❌ Email or password is missing.");
            logEmail(toEmail, "Your OTP Code - FinMark Payroll System", "FAILED", "Missing email or password");
            return;
        }

        // 🔹 Step 2: Setup SMTP config
        final String finalEmail = fromEmail;
        final String finalPass = password;

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        // 🔹 Step 3: Setup mail session
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(finalEmail, finalPass);
            }
        });

        try {
            // 🔹 Step 4: Create the OTP email
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(finalEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Your OTP Code - FinMark Payroll System");
            message.setText("Your One-Time Password (OTP) is: " + otpCode
                    + "\n\nThis code is valid for only 1 minute. Do not share it with anyone.");

            // 🔹 Step 5: Send the email
            Transport.send(message);
            System.out.println("📧 OTP email sent to: " + toEmail);

            // 🔹 Step 6: Log success
            logEmail(toEmail, "Your OTP Code - FinMark Payroll System", "SENT", null);

        } catch (MessagingException e) {
            System.err.println("❌ Failed to send OTP email: " + e.getMessage());
            e.printStackTrace();

            // 🔹 Step 7: Log failure
            logEmail(toEmail, "Your OTP Code - FinMark Payroll System", "FAILED", e.getMessage());
        }
    }

    /**
     * ✅ Logs the result of email sending (success or failed) to a database.
     * This is useful for audit trail or troubleshooting.
     */
    public static void logEmail(String recipient, String subject, String status, String errorMessage) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO email_logs (recipient, subject, status, error_message, sent_at) " +
                         "VALUES (?, ?, ?, ?, NOW())";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, recipient);     // Who received the email
            ps.setString(2, subject);       // Email subject
            ps.setString(3, status);        // "SENT" or "FAILED"
            ps.setString(4, errorMessage);  // If failed, what error
            ps.executeUpdate();             // Save log
        } catch (SQLException e) {
            System.err.println("❌ Failed to log email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
