package voting.system.example.demo.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Generated
@ToString

public class ContestantResponse {
    private String userId;
    private String name;
    private String email;
    private String viceName;
    private String position;
}
