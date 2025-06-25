package employee.service;

import utils.SessionManager;
import auth.service.JWTUtil;

public class EmployeeService {

    public static String viewEmployeeList() {
        String token = SessionManager.getToken();
        if (token == null || !JWTUtil.validateToken(token)) {
            return "Access denied: Please login.";
        }

        String role = SessionManager.getRole();

        if (!role.equalsIgnoreCase("HR Personnel")) {
            return "Access denied: Only HR Personnel can view the employee list.";
        }

        return "Employee list loaded successfully.";
    }

    public static String addEmployee(String newEmployeeName) {
        String token = SessionManager.getToken();
        if (token == null || !JWTUtil.validateToken(token)) {
            return "Access denied: Please login.";
        }

        String role = SessionManager.getRole();

        if (!role.equalsIgnoreCase("HR Personnel")) {
            return "Access denied: Only HR Personnel can add employees.";
        }

        return "Employee '" + newEmployeeName + "' added successfully.";
    }
}
