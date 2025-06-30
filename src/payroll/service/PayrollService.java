package payroll.service;

import utils.SessionManager;
import utils.JWTUtil;

public class PayrollService {

    // Simulated "REST-like" salary calculation method
    public static String calculateSalary(String employeeId) {
        // Check if there's a valid session/token
        String token = SessionManager.getToken();

        if (token == null || !JWTUtil.validateToken(token)) {
            return "Access denied: Invalid or expired token.";
        }

        String username = SessionManager.getUsername();
        String role = SessionManager.getRole();

        // Simulate response
        return "Salary calculated for employee " + employeeId +
               " by user: " + username + " (" + role + ")";
    }
}
