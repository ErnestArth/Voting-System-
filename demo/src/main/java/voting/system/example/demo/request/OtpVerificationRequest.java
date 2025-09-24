package voting.system.example.demo.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;



@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Generated

public class OtpVerificationRequest {

    private String email;

    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "\\d{4}", message = "OTP must be 4 digits")
    private String otp;

    public Object getRoles() {
        return null;
    }
}
