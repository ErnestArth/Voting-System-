package voting.system.example.demo.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Generated

public class ContestantRest {
    private String userId;
    private String contestantName;
    private String vice;
    private String viceName;
    private String position;
}

