package auth.service;

import auth.service.LoginController;
import java.awt.Color;
import javax.swing.JTextField;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import java.io.*;
import gui.AccountingHeadDashboard;
import gui.AdminDashboard;
import gui.EmployeeDashboard;
import gui.HRDashboard;
import gui.PayrollManagerDashboard;
import gui.TeamLeaderDashboard;
import utils.DBConnection;
import utils.JWTUtil;
import utils.SessionManager;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import utils.EmailUtil;
import utils.OTPUtil;
import utils.RememberTokenUtil;

/**
 *
 * @author Jomax
 */
public class LoginUI extends javax.swing.JFrame {
    
    // Placeholder and password visibility flags
    private final String passwordPlaceholder = "Enter password";
    private boolean showingPasswordPlaceholder = true;
    private boolean passwordVisible = false;

     // Constants for token handling
    private final String TOKEN_FILE = "remember_token.dat";
    private final String ENCRYPTION_KEY = "1234567890123456"; // 16-char key for AES
    /**
     * Creates new form NewLoginUI
     */
    public LoginUI() {
        initComponents();
        setLocationRelativeTo(null); // This centers the window
        setTitle("Login - FinMark Payroll System");
        setSize(400, 350);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        
        // Check if DB is connected
    if (LoginController.isDatabaseConnected()) {
        lblDBStatus.setText("✅ Connected to Database");
        lblDBStatus.setForeground(Color.GREEN.darker());
    } else {
        lblDBStatus.setText("❌ DB Connection Failed");
        lblDBStatus.setForeground(Color.RED);
    } 
        
         // Hide "Remember Me" checkbox for Administrator role
        String selectedRole = roleComboBox.getSelectedItem().toString();
        rememberMeCheckbox.setVisible(!selectedRole.equals("Administrator"));
        
        
        // This listens for changes in the role dropdown
        roleComboBox.addActionListener(e -> {
         String role = roleComboBox.getSelectedItem().toString();
    
        // Show or hide the checkbox based on selected role
        rememberMeCheckbox.setVisible(!role.equals("Administrator"));
        });
        
        // Set up field placeholders
        setPlaceholder(usernameField, "Enter username");
        setupPasswordPlaceholder();
    
    // Try to decrypt and auto-login using saved token
    try (FileInputStream fis = new FileInputStream("remember_token.dat")) {
        byte[] encrypted = fis.readAllBytes(); // Read encrypted token
        String token = decryptToken(encrypted); // Decrypt token using AES
        if (JWTUtil.validateToken(token)) { // Check if token is still valid
            String username = JWTUtil.getUsername(token); // Extract username
            String role = getUserRoleFromDatabase(username); // Get role from DB
            SessionManager.setSession(username, role, token); // Set session
            openDashboard(role); // Open corresponding dashboard
            dispose(); // Close login window
            return; // Exit constructor to avoid showing login GUI
        }
    } catch (Exception ignored) {
        // If token is invalid, expired, or file missing, do nothing
    }
    
    // Start lockout timer UI logic here
    javax.swing.Timer timer = new javax.swing.Timer(1000, (var e) -> {
    try (Connection conn = DBConnection.getConnection()) {
        String query = "SELECT locked_until FROM users WHERE username = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, usernameField.getText());
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            Timestamp lockedUntil = rs.getTimestamp("locked_until");
            if (lockedUntil != null && lockedUntil.after(new Timestamp(System.currentTimeMillis()))) {
                long remainingMillis = lockedUntil.getTime() - System.currentTimeMillis() / 1000;
                
                // Convert milliseconds to hours and minutes
                long hours = (remainingMillis / 1000) / 3600;
                long minutes = ((remainingMillis / 1000) % 3600) / 60;
                long seconds = (remainingMillis / 1000) % 60;
                
                loginButton.setEnabled(false);
                loginButton.setBackground(new Color(255, 0, 0)); // red
                loginButton.setForeground(Color.BLACK); // black text when locked
                loginButton.setText("Locked (" + hours + "h " + minutes + "m " + seconds + "s)"); // timer
            } else {
                loginButton.setEnabled(true);
                loginButton.setText("Login");
                loginButton.setForeground(Color.WHITE); // back to white text
            }
        }
        } catch (Exception ignored) {}
            // ignore errors in timer loop

        });
        timer.start();
        
// [AUTO-LOGIN LOGIC] This checks if a saved token exists for "Remember Me"
if (Files.exists(Paths.get(TOKEN_FILE))) {
    System.out.println(" Token file found, attempting auto-login...");

    try {
        // Read the encrypted token from the file
        byte[] encrypted = Files.readAllBytes(Paths.get(TOKEN_FILE));

        // Decrypt the token using AES
        String token = decryptToken(encrypted);
        System.out.println("🔓 Decrypted token: " + token);

        // Validate the JWT token's signature and expiration
        if (JWTUtil.validateToken(token)) {
            // Extract username from the token
            String username = JWTUtil.getUsername(token);

            // Get the corresponding role from the database
            String role = getUserRoleFromDatabase(username);

            // Store the session details in memory
            SessionManager.setSession(username, role, token);

            // Open the user’s dashboard based on their role
            openDashboard(role);

            // Close the login window since we auto-logged in
            dispose();

            return; // Exit constructor
        } else {
            System.out.println("❌ Token invalid or expired. Auto-login cancelled.");

            //  Optional: Show a message to the user
            JOptionPane.showMessageDialog(this, "Auto-login token has expired. Please log in manually.");
        }
    } catch (Exception e) {
        // Catch-all: Log errors if something fails
        System.out.println("⚠️ Error during auto-login:");
        e.printStackTrace();
    }
} else {
    System.out.println(" 📁 No token file found. Skipping auto-login.");
}


// For debug purposes: print current system time
System.out.println("🕒 Java time now: " + java.time.ZonedDateTime.now());
    }
    
     // Add placeholder for username input field
    private void setPlaceholder(JTextField field, String placeholder) {
        field.setForeground(Color.BLACK);
        field.setText(placeholder);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(Color.BLACK);
                    field.setText(placeholder);
                }
            }
        });
    }
    
     // Password field placeholder
    private void setupPasswordPlaceholder() {
        passwordField.setEchoChar((char) 0);
        passwordField.setForeground(Color.BLACK);
        passwordField.setText(passwordPlaceholder);

        passwordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (showingPasswordPlaceholder) {
                    passwordField.setText("");
                    passwordField.setForeground(Color.BLACK);
                    passwordField.setEchoChar('*'); // or '*'
                    showingPasswordPlaceholder = false;
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (passwordField.getPassword().length == 0) {
                    passwordField.setEchoChar((char) 0);
                    passwordField.setText(passwordPlaceholder);
                    passwordField.setForeground(Color.BLACK);
                    showingPasswordPlaceholder = true;
                }
            }
        });
    }
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        loginLabel = new javax.swing.JLabel();
        usernameField = new javax.swing.JTextField();
        roleComboBox = new javax.swing.JComboBox<>();
        loginButton = new javax.swing.JButton();
        companyLabel = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();
        togglePasswordBtn = new javax.swing.JButton();
        rememberMeCheckbox = new javax.swing.JCheckBox();
        lblDBStatus = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Login Dashboard");
        setMinimumSize(new java.awt.Dimension(400, 350));
        setPreferredSize(new java.awt.Dimension(400, 350));
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        loginLabel.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        loginLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        loginLabel.setText("LOGIN");
        loginLabel.setToolTipText("");
        loginLabel.setPreferredSize(new java.awt.Dimension(200, 30));
        getContentPane().add(loginLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 30, 200, 30));

        usernameField.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        usernameField.setToolTipText("Enter username");
        usernameField.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        usernameField.setPreferredSize(new java.awt.Dimension(220, 30));
        getContentPane().add(usernameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 80, -1, -1));

        roleComboBox.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        roleComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "HR Personnel", "Team Leader", "Payroll Manager", "Accounting Head", "Employee", "Administrator" }));
        roleComboBox.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        roleComboBox.setPreferredSize(new java.awt.Dimension(220, 30));
        getContentPane().add(roleComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 160, -1, -1));

        loginButton.setBackground(new java.awt.Color(0, 123, 255));
        loginButton.setFont(new java.awt.Font("Segoe UI Semibold", 1, 14)); // NOI18N
        loginButton.setText("Login");
        loginButton.setToolTipText("");
        loginButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        loginButton.setFocusPainted(false);
        loginButton.setPreferredSize(new java.awt.Dimension(220, 30));
        loginButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loginButtonActionPerformed(evt);
            }
        });
        getContentPane().add(loginButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 210, -1, -1));

        companyLabel.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        companyLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        companyLabel.setText("© 2025 FinMark Payroll System");
        companyLabel.setMaximumSize(new java.awt.Dimension(240, 20));
        companyLabel.setMinimumSize(new java.awt.Dimension(240, 20));
        companyLabel.setPreferredSize(new java.awt.Dimension(240, 20));
        getContentPane().add(companyLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 250, 240, 20));

        passwordField.setText("jPasswordField1");
        passwordField.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        passwordField.setPreferredSize(new java.awt.Dimension(220, 30));
        getContentPane().add(passwordField, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 120, -1, -1));

        togglePasswordBtn.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        togglePasswordBtn.setPreferredSize(new java.awt.Dimension(30, 30));
        togglePasswordBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                togglePasswordBtnActionPerformed(evt);
            }
        });
        getContentPane().add(togglePasswordBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 120, -1, -1));

        rememberMeCheckbox.setText("Remember Me");
        rememberMeCheckbox.setToolTipText("Remember Me");
        rememberMeCheckbox.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        getContentPane().add(rememberMeCheckbox, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 190, -1, -1));

        lblDBStatus.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblDBStatus.setMaximumSize(new java.awt.Dimension(250, 20));
        lblDBStatus.setMinimumSize(new java.awt.Dimension(250, 20));
        lblDBStatus.setPreferredSize(new java.awt.Dimension(250, 20));
        getContentPane().add(lblDBStatus, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 270, 250, 20));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void togglePasswordBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_togglePasswordBtnActionPerformed
    // Handle toggle button to show or hide password
        if (!showingPasswordPlaceholder) {
        if (passwordVisible) {
            passwordField.setEchoChar('*');
            togglePasswordBtn.setIcon(new ImageIcon(getClass().getResource("/resources/eye-off.png")));
            passwordVisible = false;
        } else {
            passwordField.setEchoChar((char) 0);
            togglePasswordBtn.setIcon(new ImageIcon(getClass().getResource("/resources/eye.png")));
            passwordVisible = true;
        }
    }
    }//GEN-LAST:event_togglePasswordBtnActionPerformed

    private void loginButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loginButtonActionPerformed
    // Step 1: Get input values
    String username = usernameField.getText();
    String password = new String(passwordField.getPassword());
    String role = roleComboBox.getSelectedItem().toString();

    // Step 2: Enforce "Remember Me" for non-Admins
    if (!role.equals("Administrator") && !rememberMeCheckbox.isSelected()) {
        JOptionPane.showMessageDialog(this, "You must check 'Remember Me' to continue.");
        return;
    }

            // Step 3: Authenticate login
            String result = LoginController.login(username, password, role);

            switch (result) {
            case "success":

            // Step 4: Generate OTP
            String otp = OTPUtil.generateOtp();
            String recipientEmail = OTPUtil.getUserEmailFromDatabase(username);

            if (recipientEmail != null) {
                Timestamp expiresAt = Timestamp.from(Instant.now().plusSeconds(300)); // expires in 5 minutes
                
                // ✅ Save OTP to database
                OTPUtil.saveOtpToDatabase(username, otp, expiresAt, 3); // 3 attempts allowed
                
                // ✅ Debug output
                System.out.println("Sending OTP to: " + recipientEmail);
                System.out.println("Generated OTP: " + otp);

                // ✅ Send the OTP via email
                EmailUtil.sendOtpCode(recipientEmail, otp);
                
                // Ask user to enter the OTP
                String enteredOtp = JOptionPane.showInputDialog(this, "Enter the OTP sent to your email:");

                // Get OTP from DB for comparison
                String dbOtp = null;
                Timestamp expirationTimeFromDB = null;
                try (Connection conn = DBConnection.getConnection()) {
                    String sql = "SELECT otp_code, expiration_time FROM otp_codes WHERE username = ?";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, username);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        dbOtp = rs.getString("otp_code");
                        expirationTimeFromDB = rs.getTimestamp("expiration_time");
                        
                                            }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                Timestamp currentTime = new Timestamp(System.currentTimeMillis());
                System.out.println("User entered OTP: " + enteredOtp);
                System.out.println("OTP from DB: " + dbOtp);
                System.out.println("Current time: " + currentTime);
                System.out.println("OTP expiration time: " + expirationTimeFromDB);
                
                // Check if OTP is valid
                if (enteredOtp != null && enteredOtp.equals(dbOtp)) {
                    if (currentTime.after(expirationTimeFromDB)) {
                        JOptionPane.showMessageDialog(this, "OTP has expired. Please try again.");
                        return;
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid or expired OTP. Login cancelled.");
                    return;
                }

            } else {
                JOptionPane.showMessageDialog(this, "Email not found. Cannot send OTP.");
                return;
            }
            
            JOptionPane.showMessageDialog(this, "Login successful!");
                
            // ✅ Save session now
            LoginController.saveSession(username, SessionManager.getToken()); 

            // ✅ Step 5: Save token in the database if "Remember Me" is checked
            if (rememberMeCheckbox.isSelected()) {
    
             // ✅ Get the generated JWT token from the SessionManager
            String token = SessionManager.getToken();
    
             // ✅ Set the token expiration time to 7 days from now
                Timestamp expiresAt = Timestamp.from(Instant.now().plusSeconds(7 * 24 * 60 * 60)); // 7 days

            // ✅ Save the token in the database table (e.g., remember_tokens) linked to the username
            RememberTokenUtil.saveToken(username, token, expiresAt);
                }

            // Step 6: Open dashboard
            openDashboard(role);
            this.dispose();
            break;

        case "locked":
            JOptionPane.showMessageDialog(this, "Your account is locked. Try again later.");
            break;

        case "invalid":
            JOptionPane.showMessageDialog(this, "Invalid credentials. Please try again.");
            break;

        case "not_found":
            JOptionPane.showMessageDialog(this, "User not found.");
            break;
            
        // System will check if User has active session    
        case "active_session":
            JOptionPane.showMessageDialog(this, "You are already logged in on another device.");
            return;

        default:
            JOptionPane.showMessageDialog(this, "An error occurred. Please try again later.");
            break;
    }
}

    // Opens the corresponding dashboard based on user role
 private void openDashboard(String role) {
        switch (role) {
            case "HR Personnel":
                new HRDashboard().setVisible(true);
                break;
            case "Team Leader":
                new TeamLeaderDashboard().setVisible(true);
                break;
            case "Payroll Manager":
                new PayrollManagerDashboard().setVisible(true);
                break;
            case "Accounting Head":
                new AccountingHeadDashboard().setVisible(true);
                break;
            case "Employee":
                new EmployeeDashboard().setVisible(true);
                break;
            case "Administrator":
                new AdminDashboard().setVisible(true);
                break;
            default:
                JOptionPane.showMessageDialog(this, "Unknown role: " + role);
                break;
                   
        }
}
    // Get user role from database based on username
    private String getUserRoleFromDatabase(String username) {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT role FROM users WHERE username = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("role");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] encryptToken(String token) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec keySpec = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        return cipher.doFinal(token.getBytes());
    }

    private String decryptToken(byte[] encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec keySpec = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), "AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        return new String(cipher.doFinal(encrypted));

    }//GEN-LAST:event_loginButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
    
        // Launch the login UI
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new LoginUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel companyLabel;
    private javax.swing.JLabel lblDBStatus;
    private javax.swing.JButton loginButton;
    private javax.swing.JLabel loginLabel;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JCheckBox rememberMeCheckbox;
    private javax.swing.JComboBox<String> roleComboBox;
    private javax.swing.JButton togglePasswordBtn;
    private javax.swing.JTextField usernameField;
    // End of variables declaration//GEN-END:variables
}


