package voting.system.example.demo.services;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import voting.system.example.demo.dto.Utils;
import voting.system.example.demo.entities.AuthorityEntity;
import voting.system.example.demo.entities.RoleEntity;
import voting.system.example.demo.entities.UserEntity;
import voting.system.example.demo.repositories.AuthorityRepository;
import voting.system.example.demo.repositories.RoleRepository;
import voting.system.example.demo.repositories.UserRepository;

import java.util.Arrays;
import java.util.Collection;

@Component
public class InitialUsers {

    private final AuthorityRepository authorityRepository;
    private final RoleRepository roleRepository;
    private final Utils utils;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserRepository userRepository;

    public InitialUsers(AuthorityRepository authorityRepository, RoleRepository roleRepository,
                        Utils utils, BCryptPasswordEncoder bCryptPasswordEncoder, UserRepository userRepository) {
        this.authorityRepository = authorityRepository;
        this.roleRepository = roleRepository;
        this.utils = utils;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userRepository = userRepository;
    }

    @EventListener
    @Transactional
    public void onApplicationEvent(ApplicationReadyEvent event) {

        AuthorityEntity readAuthority = createAuthority("READ_AUTHORITY");
        AuthorityEntity writeAuthority = createAuthority("WRITE_AUTHORITY");
        AuthorityEntity deleteAuthority = createAuthority("DELETE_AUTHORITY");
        AuthorityEntity updateAuthority = createAuthority("UPDATE_AUTHORITY");

        RoleEntity roleAdmin = createRole("ROLE_ADMIN",
                Arrays.asList(readAuthority, writeAuthority, deleteAuthority,  updateAuthority));

        RoleEntity roleVoter =  createRole("ROLE_VOTER",
                Arrays.asList(readAuthority, writeAuthority, updateAuthority));

//        RoleEntity roleTeamMember = createRole("ROLE_TEAM_MEMBER",
//                Arrays.asList(readAuthority, writeAuthority, updateAuthority));

        if (roleAdmin == null) {
            return;
        }

        if (userRepository.findByEmail("ernest5arthur@gmail.com") == null) {
            UserEntity adminUser = new UserEntity();
            adminUser.setFirstName("Ernest");
            adminUser.setLastName("Arthur");
            adminUser.setEmail("ernest5arthur@gmail.com");
            adminUser.setUserId(utils.generateUserId(30));
            adminUser.setEmailVerificationStatus(true);
            adminUser.setPassword(bCryptPasswordEncoder.encode("Xzibit5!"));
            adminUser.setRole(roleAdmin);

            userRepository.save(adminUser);

        }

        if (userRepository.findByEmail("kofisese04@gmail.com") == null) {
            UserEntity admin3 = new UserEntity();
            admin3.setFirstName("Kofi");
            admin3.setLastName("Sese");
            admin3.setEmail("kofisese04@gmail.com");
            admin3.setUserId(utils.generateUserId(30));
            admin3.setEmailVerificationStatus(true);
            admin3.setPassword(bCryptPasswordEncoder.encode("Kofi+Sese1"));
            admin3.setRole(roleAdmin);

            userRepository.save(admin3);
        }

        if (userRepository.findByEmail("Devonmario00@gmail.com") == null) {
            UserEntity voter = new UserEntity();
            voter.setFirstName("Godson");
            voter.setLastName("Sese");
            voter.setEmail("Devonmario00@gmail.com");
            voter.setUserId(utils.generateUserId(30));
            voter.setEmailVerificationStatus(true);
            voter.setPassword(bCryptPasswordEncoder.encode("New!_1Admin"));
            voter.setRole(roleVoter);

            userRepository.save(voter);

        }

    }

    protected AuthorityEntity createAuthority(String name) {
        AuthorityEntity authority = authorityRepository.findByName(name);
        if (authority == null) {
            authority = new AuthorityEntity(name);
            authorityRepository.save(authority);
        }

        return authority;
    }

    protected RoleEntity createRole(String name, Collection<AuthorityEntity> authorities) {
        RoleEntity role = roleRepository.findByName(name);
        if (role == null) {
            role = new RoleEntity(name);
            role.setAuthorities(authorities);
            roleRepository.save(role);

        }

        return role;
    }
}
