package voting.system.example.demo.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Generated

@Entity
@Table(name = "contestants")
public class ContestantEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Linking contestant to an existing user
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    // Optional: if contestant has a vice
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vice_user_id", nullable = true)
    private UserEntity vice;

    @Column(nullable = false)
    private String position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id")
    private PollEntity poll;
}
