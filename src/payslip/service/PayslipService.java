package payslip.service;

import utils.SessionManager;
import auth.service.JWTUtil;

public class PayslipService {

    // For Regular Employee viewing their own payslip
    public static String viewOwnPayslip(String employeeId) {
        String token = SessionManager.getToken();
        if (token == null || !JWTUtil.validateToken(token)) {
            return "Access denied: Please login first.";
        }

        String role = SessionManager.getRole();
        String username = SessionManager.getUsername();

        if (!role.equalsIgnoreCase("Regular Employee") || !username.equals(employeeId)) {
            return "Access denied: You can only view your own payslip.";
        }

        return "Payslip for " + employeeId + " loaded successfully.";
    }

    // For Payroll Manager viewing any employee's payslip
    public static String viewEmployeePayslip(String employeeId) {
        String token = SessionManager.getToken();
        if (token == null || !JWTUtil.validateToken(token)) {
            return "Access denied: Please login first.";
        }

        String role = SessionManager.getRole();

        if (!role.equalsIgnoreCase("Payroll Manager")) {
            return "Access denied: Only Payroll Manager can view other employees' payslips.";
        }

        return "Payslip for employee " + employeeId + " loaded by Payroll Manager.";
    }
}
