package voting.system.example.demo.services.Implementations;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import voting.system.example.demo.dto.AmazonSES;
import voting.system.example.demo.dto.ContestantDto;
import voting.system.example.demo.dto.UserDto;
import voting.system.example.demo.dto.Utils;
import voting.system.example.demo.entities.ContestantEntity;
import voting.system.example.demo.entities.RoleEntity;
import voting.system.example.demo.entities.UserEntity;
import voting.system.example.demo.repositories.RoleRepository;
import voting.system.example.demo.repositories.UserRepository;
import voting.system.example.demo.security.UserPrincipal;
import voting.system.example.demo.services.UserService;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static voting.system.example.demo.security.SecurityConstants.*;

@Service
public class UserServiceImplementation implements UserService {

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    Utils utils;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    AmazonSES amazonSES;

    @Value("${OTP_Default_Value:}")
    private String otpDefaultValue;

    @Value("${OTP_Default_Boolean_Value:}")
    private Boolean otpDefaultBooleanValue;

    @Override
    public UserDto createUser(UserDto userDto) {
        if (userDto.getFirstName() == null || userDto.getFirstName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "First Name is required");
        }

        if (userDto.getEmail() == null || userDto.getEmail().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }

        if (!userDto.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email format");
        }

        if (userDto.getPassword() == null || userDto.getPassword().length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 6 characters");
        }

        if (userDto.getRole() == null || userDto.getRole().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role is required");
        }

        // Checking uniqueness
        if (userRepository.findByEmail(userDto.getEmail()) != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }


        UserEntity userEntity = modelMapper.map(userDto, UserEntity.class);
        userEntity.setUserId(utils.generateUserId(30));

        // Handling role
        String rawRole = userDto.getRole().trim();
        String roleName = rawRole.startsWith("ROLE_") ? rawRole : "ROLE_" + rawRole;
        RoleEntity role = roleRepository.findByName(roleName);

        if (role == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role");
        }
        userEntity.setRole(role);

        // Setting password and other defaults
        userEntity.setEmailVerificationStatus(true);
        userEntity.setPassword(bCryptPasswordEncoder.encode(userDto.getPassword()));

        userRepository.save(userEntity);

        UserDto responseDto = modelMapper.map(userEntity, UserDto.class);
        responseDto.setRole(userEntity.getRole().getName().replace("ROLE_", ""));

        amazonSES.sendOnboardingEmail(responseDto.getEmail(), responseDto.getFirstName());

        return responseDto;

}

    /**
     * @param email
     * @return
     */
    @Override
    public UserDto getUser(String email) {
        UserEntity userEntity = userRepository.findByEmail(email);

        if (userEntity == null) {
            throw new UsernameNotFoundException(email);
        }
        UserDto returnUser = new UserDto();
        returnUser.setUserId(userEntity.getUserId());
        returnUser.setEmail(userEntity.getEmail());
        returnUser.setPassword(userEntity.getPassword());
        returnUser.setFirstName(userEntity.getFirstName());
        returnUser.setLastName(userEntity.getLastName());
        returnUser.setOtpExpiryDate(userEntity.getOtpExpiryDate());
        returnUser.setOtp(userEntity.getOtp());
        returnUser.setId(userEntity.getId());
        returnUser.setOtpFailedAttempts(userEntity.getOtpFailedAttempts());

        return returnUser;
    }

    /**
     * @param email
     * @param otp
     * @param expiryTime
     */
    @Override
    public void saveOtp(String email, String otp, Date expiryTime) {
        UserEntity user = userRepository.findByEmail(email);

        user.setOtp(otp);
        user.setOtpExpiryDate(expiryTime);
        userRepository.save(user);
    }

    /**
     * @param email
     * @param otp
     * @return
     */
    @Override

    public boolean validateOtp(String email, String otp) {
        UserEntity user = userRepository.findByEmail(email);

        if (Boolean.TRUE.equals(user.getAccountLocked())) {
            throw new RuntimeException("Account is permanently locked. Contact Support");
        }

        // Checking if user is temporarily blocked
        if (user.getTempBlockTime() != null &&
                System.currentTimeMillis() - user.getTempBlockTime().getTime() < Temp_Block_Duration) {
            long remainingTime = (Temp_Block_Duration -
                    (System.currentTimeMillis() - user.getTempBlockTime().getTime())) / 60000;
            throw new RuntimeException("Account temporarily blocked. Try again in " + remainingTime + " minutes.");
        }

        // Allowing QA default OTP if enabled and matched
        if (otpDefaultBooleanValue && otpDefaultValue.equals(otp)) {
            // Resetting failed attempts and unblock the user if previously blocked
            user.setOtpFailedAttempts(0);
            user.setOtp(null);
            user.setTempBlockTime(null);
            userRepository.save(user);
            return true;
        }

        // Validate OTP
        if (user.getOtp() == null || !otp.equals(user.getOtp())) {
            user.setOtpFailedAttempts(user.getOtpFailedAttempts() + 1);

            // Temporary block on the 3rd attempt
            if (user.getOtpFailedAttempts() > Max_Temp_Attempts &&
                    user.getOtpFailedAttempts() < Max_Perm_Attempts) {
                user.setTempBlockTime(new Date());
                userRepository.save(user);
                throw new ResponseStatusException(HttpStatus.LOCKED,
                        "Too many attempts. Account temporarily blocked for 15 minutes.");
            }

            // Permanently locking on the 5th attempt
            if (user.getOtpFailedAttempts() > Max_Perm_Attempts) {
                user.setAccountLocked(true);
                userRepository.save(user);
                throw new ResponseStatusException(HttpStatus.LOCKED,
                        "Too many attempts. Account permanently locked. Contact support");
            }

            userRepository.save(user);
            return false;
        }

        if (new Date().after(user.getOtpExpiryDate())) {
            throw new RuntimeException("OTP expired");
        }

        //resetting attempts on success
        user.setOtpFailedAttempts(0);
        user.setOtp(null);
        user.setTempBlockTime(null);
        userRepository.save(user);
        return true;

    }

    /**
     * @param email
     * @return
     */
    @Override
    public String initiatePasswordReset(String email) {
        UserEntity user = userRepository.findByEmail(email);
        if (user == null) {
            return null;
        }
        String token = utils.generatePasswordResetToken();
        user.setPasswordResetToken(token);
        user.setPasswordResetExpiration(new Date(System.currentTimeMillis() + Password_Reset_Expiration_Time));

        userRepository.save(user);

        amazonSES.sendPasswordResetRequest(user.getFirstName(), user.getEmail(), token);

        // http://localhost:8080/reset-password?token=xyz123
        System.out.println("Password reset link: http://localhost:8080/reset-password?token=" + token);

        return token;
    }

    /**
     * @param email
     * @return
     */
    @Override
    public Map<String, Object> resendOtp(String email) {
            UserEntity user = userRepository.findByEmail(email);

            if (user == null) {
                throw new RuntimeException("User not found with email : " + email);
            }

            int attempts = Optional.ofNullable(user.getResendOtpAttempts()).orElse(0);
            LocalDateTime lastResendTime = user.getLastOtpResendTime();
            int remainingAttempts = Math.max(0, Max_Resend_Attempts - (attempts + 1));

            //  Validating resend attempts
            if (attempts >= Max_Resend_Attempts &&
                    lastResendTime != null &&
                    Duration.between(lastResendTime, LocalDateTime.now()).toMinutes() < Resend_Cooldown.toMinutes()) {

                long remainingCooldown = Resend_Cooldown.toMinutes() -
                        Duration.between(lastResendTime, LocalDateTime.now()).toMinutes();

                return Map.of(
                        "status", "ERROR",
                        "message", "Too many resend attempts. Try again after " + remainingCooldown + " minutes.",
                        "timestamp", LocalDateTime.now()
                );
            }

            //  Generating and sending new OTP
            String newOtp = String.format("%06d", new SecureRandom().nextInt(999999));
            user.setOtp(newOtp);
            user.setOtpExpiryDate(new Date(System.currentTimeMillis() + 180000));
            user.setResendOtpAttempts(attempts + 1);
            user.setLastOtpResendTime(LocalDateTime.now());
            userRepository.save(user);

            amazonSES.sendLoginOtpEmail(user.getFirstName(), email, newOtp);

            return Map.of(
                    "status", "SUCCESS",
                    "message", "New OTP sent successfully",
                    "timestamp", LocalDateTime.now(),
                    "resendAttemptsRemaining", remainingAttempts
            );
        }

    /**
     * @param token
     * @param newPassword
     * @param confirmNewPassword
     */
    @Override
    public void resetPassword(String token, String newPassword, String confirmNewPassword) {
        UserEntity user = userRepository.findByPasswordResetToken(token);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid password reset token");
        }
        if (user.getPasswordResetExpiration() == null || user.getPasswordResetExpiration().before(new Date())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password reset token has expired");
        }

        if (!newPassword.equals(confirmNewPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }

        user.setPassword(bCryptPasswordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiration(null);

        userRepository.save(user);
    }


    //clearing expired OTPs
    @Scheduled(fixedRate = 180000) // for 3 minutes
    public void cleanupExpiredOtps() {
        List<UserEntity> users = userRepository.findByOtpIsNotNull();
        Date now = new Date();

        users.forEach(user -> {
            if (user.getOtpExpiryDate() != null &&
                    now.after(user.getOtpExpiryDate())) {
                user.setOtp(null);
                user.setOtpExpiryDate(null);
                userRepository.save(user);
            }
        });
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByEmail(email);
        System.out.println("user entity :"+ userEntity);

        if (userEntity == null) throw new UsernameNotFoundException(email);

        return new UserPrincipal(userEntity);
    }
}
