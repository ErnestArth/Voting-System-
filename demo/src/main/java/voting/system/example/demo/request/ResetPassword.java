package voting.system.example.demo.request;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Generated

public class ResetPassword {
    private String token;
    private String newPassword;
    private String confirmNewPassword;
}
