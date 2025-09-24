package voting.system.example.demo.services;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import voting.system.example.demo.dto.ContestantDto;
import voting.system.example.demo.dto.UserDto;
import voting.system.example.demo.entities.ContestantEntity;

import java.util.Date;
import java.util.Map;

@Service
public interface UserService extends UserDetailsService {
    UserDto createUser(UserDto userDto);

    UserDto getUser(String userName);

    void saveOtp(String email, String otp, Date expiryTime);

    boolean validateOtp(String email, String otp);

    String initiatePasswordReset(String email);

    Map<String, Object> resendOtp(String email);

    void resetPassword(String token, String newPassword, String confirmNewPassword);

//    ContestantEntity addContestant(ContestantDto contestantDto);
}
