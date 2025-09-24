package voting.system.example.demo.services;

import org.springframework.stereotype.Service;
import voting.system.example.demo.dto.ContestantDto;
import voting.system.example.demo.entities.ContestantEntity;
import voting.system.example.demo.response.ContestantResponse;

import java.util.List;

@Service
public interface ContestantService {
    ContestantDto addContestant(ContestantDto contestantDto);

    List<ContestantResponse> getAllContestants();

    ContestantResponse getContestantByUserId(String userId);
}
