package voting.system.example.demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import voting.system.example.demo.entities.PollEntity;

@Repository
public interface PollRepository extends JpaRepository<PollEntity, Long> {
}
