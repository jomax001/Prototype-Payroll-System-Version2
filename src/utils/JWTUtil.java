package utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;

public class JWTUtil {
    // ✅ Secret key used to sign and verify JWT tokens using the HS256 algorithm
    private static final Key key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);

    // ✅ Token expiration time set to 10 minutes (in milliseconds)
    private static final long EXPIRATION_TIME = 1000 * 60 * 10;

    // ✅ This method generates a JWT token with the given username and expiration
    public static String generateToken(String username) {
        String token = Jwts.builder()
                .setSubject(username) // Set username as token subject
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 10 mins expiry
                .signWith(key) // Sign the token with secret key
                .compact();

        System.out.println("✅ JWT token generated for: " + username); // Print for debugging
        System.out.println("🔐 Token: " + token);
        return token;
    }

    // ✅ This method checks if the token is valid (signature and expiration)
    public static boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key) // Use same secret key
                    .build()
                    .parseClaimsJws(token); // Parse token to verify
            return true; // Valid token
        } catch (JwtException e) {
            // If expired or invalid signature
            System.out.println("❌ Invalid or expired token: " + e.getMessage());
            return false;
        }
    }

    // ✅ This method extracts the username from the token
    public static String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject(); // Returns the username
    }
}
