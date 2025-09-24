package voting.system.example.demo.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import voting.system.example.demo.config.SpringApplicationContext;
import voting.system.example.demo.dto.ContestantDto;
import voting.system.example.demo.dto.PollDto;
import voting.system.example.demo.dto.UserDto;
import voting.system.example.demo.entities.AuthorityEntity;
import voting.system.example.demo.entities.ContestantEntity;
import voting.system.example.demo.entities.PollEntity;
import voting.system.example.demo.entities.UserEntity;
import voting.system.example.demo.repositories.UserRepository;
import voting.system.example.demo.request.*;
import voting.system.example.demo.response.ContestantResponse;
import voting.system.example.demo.response.ContestantRest;
import voting.system.example.demo.response.PollResponse;
import voting.system.example.demo.response.UserRest;
import voting.system.example.demo.security.SecurityConstants;
import voting.system.example.demo.services.ContestantService;
import voting.system.example.demo.services.PollService;
import voting.system.example.demo.services.UserService;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/v1/users")
public class UserController {

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ContestantService contestantService;

    @Autowired
    PollService pollService;



    //user creation(Sign up)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserRest> createUser(@RequestBody UserDetails userDetails) throws Exception {

        UserDto userDto = modelMapper.map(userDetails, UserDto.class);

        UserDto createdUser = userService.createUser(userDto);
        UserRest userRest = modelMapper.map(createdUser, UserRest.class);

        return ResponseEntity.ok(userRest);
    }

    //OTP verification
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Validated @RequestBody OtpVerificationRequest request) {
        // Validating OTP
        boolean isValid = userService.validateOtp(request.getEmail(), request.getOtp());

        if (!isValid) {
            return ResponseEntity.ok(Map.of(
                    "message", "Invalid OTP.",
                    "status", "FAILED"
            ));
        }

        // Getting user details from the database
        UserEntity userEntity = userRepository.findByEmail(request.getEmail());
        if (userEntity == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("message", "User not found", "status", "FAILED")
            );
        }

        // Generating JWT
        String tokenSecret = (String) SpringApplicationContext.getBean("secretKey");

        byte[] secretKeyBytes = Base64.getEncoder().encode(tokenSecret.getBytes());
        SecretKey secretKey = new SecretKeySpec(secretKeyBytes, SignatureAlgorithm.HS512.getJcaName());
        Instant now = Instant.now();

        long expirationTime = SecurityConstants.Expiration_Time_In_Seconds;

        String token = Jwts.builder()
                .setSubject(request.getEmail())
                .claim("roles", userEntity.getRole().getName())
                .claim("authorities", getAuthorityNames(userEntity.getRole().getAuthorities()))
                .setExpiration(Date.from(now.plusMillis(expirationTime)))
                .setIssuedAt(Date.from(now))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();

        return ResponseEntity.ok(Map.of(
                "token", token,
                "status", "LOGIN_SUCCESS"
        ));

    }

    // Helper method to extract authority names
    private List<String> getAuthorityNames(Collection<AuthorityEntity> authorities) {
        return authorities.stream()
                .map(AuthorityEntity::getName)
                .collect(Collectors.toList());
    }


    //Resend OTP
    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody ResendOtpRequest request) {
        try {
            Map<String, Object> response = userService.resendOtp(request.getEmail());
            HttpStatus status = response.get("status").equals("SUCCESS")
                    ? HttpStatus.OK
                    : HttpStatus.TOO_MANY_REQUESTS;
            return ResponseEntity.status(status).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "status", "ERROR",
                            "message", e.getMessage(),
                            "timestamp", LocalDateTime.now()
                    ));
        }
    }


    //Forgot password request
    @PostMapping("/forgot-password-request")
    public ResponseEntity<?> forgotPassword(@Validated @RequestBody ForgotPasswordRequest request) {
        System.out.println("hitting endpoint");

        String token = userService.initiatePasswordReset(request.getEmail());

        if (token != null) {
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "message", "Password reset instructions have been sent to your email.",
                    "status", "SUCCESS"
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "No user found with the provided email.",
                    "status", "FAILED"
            ));
        }
    }


    //Reset password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Validated @RequestBody ResetPassword request) {

        try {
            userService.resetPassword(
                    request.getToken(),
                    request.getNewPassword(),
                    request.getConfirmNewPassword());

            return ResponseEntity.ok(Map.of(
                    "message", "Password reset successful.",
                    "status", "SUCCESS"
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage(),
                    "status", "FAILED"
            ));
        }
    }


    //Adding a contestant
    @PostMapping("/add-contestant")
    public ResponseEntity<?> addContestant(@Valid @RequestBody ContestantDetails contestantDetails) throws Exception {
        ContestantDto contestantDto = ContestantDto.builder()
                .userId(contestantDetails.getUserId())
                .viceUserId(contestantDetails.getViceUserId())
                .position(contestantDetails.getPosition())
                .build();

        ContestantDto createdContestant = contestantService.addContestant(contestantDto);
        ContestantRest contestantRest = ContestantRest.builder()
                .userId(createdContestant.getUserId())
                .contestantName(createdContestant.getContestantName())
                .vice(createdContestant.getViceUserId())
                .viceName(createdContestant.getViceName())
                .position(createdContestant.getPosition())
                .build();

        return ResponseEntity.ok(contestantRest);
    }


    //Fetching all contestants
    @GetMapping("/all-contestants")
    public ResponseEntity<List<ContestantResponse>> getAllContestants() {
        return ResponseEntity.ok(contestantService.getAllContestants());
    }


    //Fetching a contestant
    @GetMapping("/all-contestants/{id}")
    public ResponseEntity<ContestantResponse> getContestantById(@PathVariable String userId) {
        return ResponseEntity.ok(contestantService.getContestantByUserId(userId));
    }


    //creating a poll
    @PostMapping("/create-poll")
    public ResponseEntity<PollEntity> createPoll(@Valid @RequestBody PollDto pollDto) {
        PollEntity poll = pollService.createPoll(pollDto);
        return ResponseEntity.ok(poll);
    }


    //Fetching all polls
    @GetMapping("/all-polls")
    public ResponseEntity<List<PollResponse>> getAllPolls() {
        return ResponseEntity.ok(pollService.getAllPolls());
    }


    //Fetching a poll
    @GetMapping("/all-polls/{id}")
    public ResponseEntity<PollResponse> getPollById(@PathVariable Long id) {
        return ResponseEntity.ok(pollService.getPollById(id));
    }


    //Updating a poll
    @PutMapping("/edit-poll/{id}")
    public ResponseEntity<PollResponse> updatePoll(
            @PathVariable Long id,
            @Valid @RequestBody PollDto pollDto) {
        return ResponseEntity.ok(pollService.updatePoll(id, pollDto));
    }


    //Deleting a poll
    @DeleteMapping("delete-poll/{id}")
    public ResponseEntity<Void> deletePoll(@PathVariable Long id) {
        pollService.deletePoll(id);
        return ResponseEntity.noContent().build();
    }

}
