package voting.system.example.demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import voting.system.example.demo.entities.AuthorityEntity;

public interface AuthorityRepository extends JpaRepository<AuthorityEntity, Long> {
    AuthorityEntity findByName(String name);
}
