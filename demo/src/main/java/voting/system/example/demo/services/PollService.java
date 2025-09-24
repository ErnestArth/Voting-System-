package voting.system.example.demo.services;

import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import voting.system.example.demo.dto.PollDto;
import voting.system.example.demo.entities.PollEntity;
import voting.system.example.demo.response.PollResponse;

import java.util.List;

@Service
public interface PollService {
    PollEntity createPoll(@Valid PollDto pollDto);

    List<PollResponse> getAllPolls();

    PollResponse getPollById(Long id);

    PollResponse updatePoll(Long id, @Valid PollDto pollDto);

    void deletePoll(Long id);
}
