/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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

    public static void sendLockNotification(String toEmail) {
        String fromEmail = null;
        String password = null;

        // 🔹 Fetch email credentials from database
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT sender_email, app_password FROM email_config LIMIT 1";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                fromEmail = rs.getString("sender_email");
                password = rs.getString("app_password");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        // ✅ Convert to final variables for Authenticator
        final String finalEmail = fromEmail;
        final String finalPass = password;

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com"); // Trust Gmail

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(finalEmail, finalPass); // ✅ Now it works!
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(finalEmail));
            message.setRecipients(
                Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Account Locked - FinMark Payroll System");
            message.setText("Your FinMark account has been locked for 24 hours due to 3 invalid login attempts.\n\n"
                          + "If this was not you, please contact your system administrator.");

            Transport.send(message);
            System.out.println("✅ Lock email sent to " + toEmail);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}