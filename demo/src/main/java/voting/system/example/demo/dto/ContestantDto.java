package voting.system.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Generated
@ToString
public class ContestantDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String userId;

    private String contestantName;

    private String viceUserId;

    private String viceName;

    @NotBlank(message = "Position is required")
    private String position;

    private String poll;
}
