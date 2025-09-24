package voting.system.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import voting.system.example.demo.entities.RoleEntity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Generated
//@ToString
public class UserDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String userId;

    private String firstName;

    private String lastName;

    private String email;

    private String password;

    private boolean emailVerificationStatus;

    private String role;

    @JsonIgnore
    private transient RoleEntity roleEntity;

    @JsonIgnore
    private String otp;

    @JsonIgnore
    private Date otpExpiryDate;

    @JsonIgnore
    private Integer otpFailedAttempts;

    @JsonIgnore
    private Date tempBlockTime;

    @JsonIgnore
    private Boolean accountLocked;

    @JsonIgnore
    private Integer resendOtpAttempts;

    @JsonIgnore
    private LocalDateTime lastOtpResendTime;
}
