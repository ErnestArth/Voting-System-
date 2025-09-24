package voting.system.example.demo.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.core.env.Environment;
import voting.system.example.demo.config.SpringApplicationContext;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;

public class SecurityConstants {
    public static final long Expiration_Time_In_Seconds = 3600000;    // 1 hour
    public static final String Token_Prefix = "Bearer ";
    public static final String Token_Header = "Authorization";
    public static final String Token_Secret = "bvgshg73hue7739349nfewywfw9wldsa73waada13948uewjew2d4f5z0s6xv";
    public static final int Max_Temp_Attempts = 2;
    public static final int Max_Perm_Attempts = 4;
    public static final long Temp_Block_Duration = 900000; // temporarily locked for 15 minutes
    public static final int Max_Resend_Attempts = 3;
    public static final Duration Resend_Cooldown = Duration.ofMinutes(15);
    public static final long Password_Reset_Expiration_Time = 1800000; // 30 minutes for password reset

    public static String generateToken(String username, long expirationTimeMillis) {
        byte[] secretKeyBytes = Base64.getEncoder().encode(getTokenSecret().getBytes());
        SecretKey secretKey = new SecretKeySpec(secretKeyBytes, SignatureAlgorithm.HS512.getJcaName());

        return Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + expirationTimeMillis))
                .setIssuedAt(new Date())
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }
    public  static String getTokenSecret() {
        Environment environment = (Environment) SpringApplicationContext.getBean("environment");
        return environment.getProperty("token.secret");
    }
}
