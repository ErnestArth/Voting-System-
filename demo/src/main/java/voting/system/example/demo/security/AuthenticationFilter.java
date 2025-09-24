package voting.system.example.demo.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import voting.system.example.demo.config.SpringApplicationContext;
import voting.system.example.demo.dto.AmazonSES;
import voting.system.example.demo.dto.UserDto;
import voting.system.example.demo.dto.Utils;
import voting.system.example.demo.entities.UserEntity;
import voting.system.example.demo.repositories.UserRepository;
import voting.system.example.demo.request.UserLoginRequestModel;
import voting.system.example.demo.services.UserService;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final UserRepository userRepository;

    public AuthenticationFilter(AuthenticationManager authenticationManager, UserRepository userRepository) {
        super(authenticationManager);
        this.userRepository = userRepository;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        try{
            UserLoginRequestModel creds = new ObjectMapper().readValue(request.getInputStream(), UserLoginRequestModel.class);
            return getAuthenticationManager().authenticate(new UsernamePasswordAuthenticationToken(creds.getEmail(),creds.getPassword()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult)
            throws IOException, ServletException {

        String tokenSecret = (String) SpringApplicationContext.getBean("secretKey");

        byte[] secretKeyBytes = Base64.getEncoder().encode(tokenSecret.getBytes());
        SecretKey secretKey = new SecretKeySpec(secretKeyBytes, SignatureAlgorithm.HS512.getJcaName());
        Instant now = Instant.now();


        String userName = ((UserPrincipal) authResult.getPrincipal()).getUsername();

        UserService userService = (UserService) SpringApplicationContext.getBean("userServiceImplementation");
        UserDto userDto = userService.getUser(userName);

            // Generating OTP
            UserEntity userEntity = userRepository.findByEmail(userName);
            String otp = String.format("%04d", new SecureRandom().nextInt(9999));
            userService.saveOtp(userName, otp, new Date(System.currentTimeMillis() + 180000));

            AmazonSES emailService = (AmazonSES) SpringApplicationContext.getBean("amazonSES");
            emailService.sendLoginOtpEmail(userDto.getFirstName(), userName, otp);

            // Normal login case
            response.setContentType("application/json");
            new ObjectMapper().writeValue(
                    response.getWriter(),
                    Map.of(
                            "status", "OTP_SENT",
                            "email", userName,
                            "userId", userEntity.getUserId(),
                            "message", "OTP sent to registered email",
                            "role", userEntity.getRole().getName()
                    )
            );

        }

    }



