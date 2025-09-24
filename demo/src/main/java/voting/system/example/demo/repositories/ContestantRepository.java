package voting.system.example.demo.repositories;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import voting.system.example.demo.entities.ContestantEntity;
import voting.system.example.demo.response.ContestantResponse;


@Repository
public interface ContestantRepository extends JpaRepository<ContestantEntity, Long> {

   boolean existsByUser_UserIdAndPosition(@NotNull(message = "User ID is required") String userId,
                                      @NotBlank(message = "Position is required") String position);


    ContestantEntity findByUser_UserId(String userId);
}
