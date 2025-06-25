/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

/**
 *
 * @author Jomax
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class DBConnection {

    private static final String URL = "jdbc:postgresql://ep-solitary-block-a1lz8vh0-pooler.ap-southeast-1.aws.neon.tech/neondb?sslmode=require";
    private static final String USER = "neondb_owner";
    private static final String PASSWORD = "npg_k4JtDmyBlb2L"; // <-- Palitan mo ito ng actual password mo

    public static Connection getConnection() {
        try {
            Class.forName("org.postgresql.Driver"); // optional sa latest JDBC, pero okay pa rin ilagay
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Connection error: " + e.getMessage());
            return null;
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "PostgreSQL JDBC Driver not found.");
            return null;
            
            
        }
    }
}
