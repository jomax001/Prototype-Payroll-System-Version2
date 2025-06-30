package leave.service;

import utils.SessionManager;
import utils.JWTUtil;

public class LeaveService {

    public static String viewLeaveRequests(String employeeId) {
        // Step 1: Validate token
        String token = SessionManager.getToken();
        if (token == null || !JWTUtil.validateToken(token)) {
            return "Access denied: You are not logged in.";
        }

        // Step 2: Check allowed roles
        String role = SessionManager.getRole();
        if (!role.equalsIgnoreCase("HR Personnel") &&
            !role.equalsIgnoreCase("Team Leader")) {
            return "Access denied: You do not have permission to view leave requests.";
        }

        // Step 3: Simulated data return
        return "Leave requests for " + employeeId + " fetched successfully.";
    }
}
