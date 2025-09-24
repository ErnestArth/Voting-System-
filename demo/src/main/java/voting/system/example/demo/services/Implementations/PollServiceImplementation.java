package voting.system.example.demo.services.Implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import voting.system.example.demo.dto.PollDto;
import voting.system.example.demo.entities.PollEntity;
import voting.system.example.demo.repositories.PollRepository;
import voting.system.example.demo.response.PollResponse;
import voting.system.example.demo.services.PollService;

import java.util.List;

@Service
public class PollServiceImplementation implements PollService {

    @Autowired
    PollRepository pollRepository;

    /**
     * @param pollDto
     * @return
     */
    @Override
    public PollEntity createPoll(PollDto pollDto) {
        if (pollDto.getEndDate().isBefore(pollDto.getStartDate())) {
            throw new RuntimeException("End date cannot be before start date");
        }

        PollEntity poll = PollEntity.builder()
                .title(pollDto.getTitle())
                .description(pollDto.getDescription())
                .startDate(pollDto.getStartDate())
                .endDate(pollDto.getEndDate())
                .build();

        return pollRepository.save(poll);
    }

    /**
     * @return
     */
    @Override
    public List<PollResponse> getAllPolls() {
        return pollRepository.findAll().stream()
                .map(poll -> PollResponse.builder()
                        .id(poll.getId())
                        .title(poll.getTitle())
                        .description(poll.getDescription())
                        .startDate(poll.getStartDate())
                        .endDate(poll.getEndDate())
                        .build())
                .toList();
    }

    /**
     * @param id
     * @return
     */
    @Override
    public PollResponse getPollById(Long id) {
        PollEntity poll = pollRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Poll not found"));

        return PollResponse.builder()
                .id(poll.getId())
                .title(poll.getTitle())
                .description(poll.getDescription())
                .startDate(poll.getStartDate())
                .endDate(poll.getEndDate())
                .build();
    }

    /**
     * @param id
     * @param pollDto
     * @return
     */
    @Override
    public PollResponse updatePoll(Long id, PollDto pollDto) {
        PollEntity poll = pollRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Poll not found"));

        poll.setTitle(pollDto.getTitle());
        poll.setDescription(pollDto.getDescription());
        poll.setStartDate(pollDto.getStartDate());
        poll.setEndDate(pollDto.getEndDate());

        PollEntity updated = pollRepository.save(poll);

        return PollResponse.builder()
                .id(updated.getId())
                .title(updated.getTitle())
                .description(updated.getDescription())
                .startDate(updated.getStartDate())
                .endDate(updated.getEndDate())
                .build();
    }

    /**
     * @param id
     */
    @Override
    public void deletePoll(Long id) {
        PollEntity poll = pollRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Poll not found"));
        pollRepository.delete(poll);
    }
}
