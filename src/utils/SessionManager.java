package utils;

public class SessionManager {
    private static String username;
    private static String role;
    private static String token;

    public static void setSession(String user, String userRole, String jwtToken) {
        username = user;
        role = userRole;
        token = jwtToken;
    }

    public static String getUsername() {
        return username;
    }

    public static String getRole() {
        return role;
    }

    public static String getToken() {
        return token;
    }

    public static boolean isLoggedIn() {
        return token != null;
    }

    public static void logout() {
        username = null;
        role = null;
        token = null;
    }
}
