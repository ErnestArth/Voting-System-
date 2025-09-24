package voting.system.example.demo.repositories;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import voting.system.example.demo.entities.UserEntity;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    UserEntity findByEmail(String email);

    List<UserEntity> findByOtpIsNotNull();

    UserEntity findByPasswordResetToken(String token);

    UserEntity findByUserId(@NotNull(message = "User ID is required") String userId);

}
