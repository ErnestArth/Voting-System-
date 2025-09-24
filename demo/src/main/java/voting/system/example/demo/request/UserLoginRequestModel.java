package voting.system.example.demo.request;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Generated
public class UserLoginRequestModel {
    private String email;
    private String password;
}
