package voting.system.example.demo.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetails {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String role;

}
