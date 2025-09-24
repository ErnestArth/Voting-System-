package voting.system.example.demo.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Generated

@Entity
@Table(name = "users")
public class UserEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String userId;

    private String firstName;

    private String lastName;

    private String email;

    private String password;

    private boolean emailVerificationStatus;

    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinTable(name = "users_roles", joinColumns = @JoinColumn(name = "users_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "roles_id", referencedColumnName = "id"))
    private RoleEntity role;

    private Date passwordResetExpiration;

    private String passwordResetToken;

    private String otp;

    private Date otpExpiryDate;

    @JsonIgnore
    @Column(name = "otp_failed_attempts")
    private Integer otpFailedAttempts = 0;

    @JsonIgnore
    @Column(name = "temp_block_time")
    private Date tempBlockTime;

    @JsonIgnore
    @Column(name = "account_locked")
    private Boolean accountLocked = false;

    @JsonIgnore
    @Column(name = "resend_otp_attempts")
    private Integer resendOtpAttempts = 0;

    @JsonIgnore
    @Column(name = "last_otp_resend")
    private LocalDateTime lastOtpResendTime;
}
