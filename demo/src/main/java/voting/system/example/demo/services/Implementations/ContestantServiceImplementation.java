package voting.system.example.demo.services.Implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import voting.system.example.demo.dto.ContestantDto;
import voting.system.example.demo.entities.ContestantEntity;
import voting.system.example.demo.entities.UserEntity;
import voting.system.example.demo.repositories.ContestantRepository;
import voting.system.example.demo.repositories.UserRepository;
import voting.system.example.demo.response.ContestantResponse;
import voting.system.example.demo.services.ContestantService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContestantServiceImplementation implements ContestantService {

    @Autowired
    ContestantRepository contestantRepository;

    @Autowired
    UserRepository userRepository;
    /**
     * @param contestantDto
     * @return
     */
    @Override
    public ContestantDto addContestant(ContestantDto contestantDto) {
        //checking if user is already contesting for this position
        if(contestantRepository.existsByUser_UserIdAndPosition(contestantDto.getUserId(), contestantDto.getPosition())) {
            throw new RuntimeException("User is already contesting for selected position");
        }

        //checking if user exists
        UserEntity user = userRepository.findByUserId(contestantDto.getUserId());;
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        //fetching vice if applicable
        UserEntity vice = null;
        if (contestantDto.getViceUserId() != null) {
            vice = userRepository.findByUserId(contestantDto.getViceUserId());
        }

        ContestantEntity contestant = new ContestantEntity();
        contestant.setUser(user);
        contestant.setVice(vice);
        contestant.setPosition(contestantDto.getPosition());

       ContestantEntity saved = contestantRepository.save(contestant);

        return ContestantDto.builder()
                .id(saved.getId())
                .userId(saved.getUser().getUserId())
                .contestantName(saved.getUser().getFirstName() + " " + saved.getUser().getLastName())
                .viceUserId(saved.getVice() != null ? saved.getVice().getUserId() : null)
                .viceName(saved.getVice() != null
                ? saved.getVice().getFirstName() + " " + saved.getVice().getLastName() : null)
                .position(saved.getPosition())
                .poll(saved.getPoll() != null ? saved.getPoll().getPollId() : null)
                .build();
    }

    /**
     * @return
     */
    @Override
    public List<ContestantResponse> getAllContestants() {
        return contestantRepository.findAll().stream()
                .map(contestant -> ContestantResponse.builder()
                        .userId(contestant.getUser().getUserId())
                        .name(contestant.getUser().getFirstName() + " " + contestant.getUser().getLastName())
                        .email(contestant.getUser().getEmail())
                        .viceName(contestant.getVice() != null
                                ? contestant.getVice().getFirstName() + " " + contestant.getVice().getLastName()
                                : null)
                        .position(contestant.getPosition())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * @param userId
     * @return
     */
    @Override
    public ContestantResponse getContestantByUserId(String userId) {
        ContestantEntity contestant = contestantRepository.findByUser_UserId(userId);
            if (contestant == null) {
                throw new RuntimeException("Contestant not found");
            }

        return ContestantResponse.builder()
                .userId(contestant.getUser().getUserId())
                .name(contestant.getUser().getFirstName() + " " + contestant.getUser().getLastName())
                .email(contestant.getUser().getEmail())
                .viceName(contestant.getVice() != null
                        ? contestant.getVice().getFirstName() + " " + contestant.getVice().getLastName()
                        : null)
                .position(contestant.getPosition())
                .build();
    }
}
