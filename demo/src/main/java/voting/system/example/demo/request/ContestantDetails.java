package voting.system.example.demo.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder

public class ContestantDetails {
    private String userId;
    private String viceUserId;
    private String position;
}
