package ch.bpm.workflow.example.ldap.repository;

import ch.bpm.workflow.example.ldap.model.Group;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles(profiles = "test")
@TestPropertySource(properties = {
    "camunda.bpm.job-execution.enabled=false",
    "camunda.bpm.client.disable-auto-fetching=true"
})
@Slf4j
class GroupRepositoryTest {

    @Autowired
    GroupRepository groupRepository;

    @Test
    void findAllGroups() {
        Iterable<Group> groups = groupRepository.findAll();
        assertThat(groups).hasSize(3)
            .extracting("name")
            .containsExactlyInAnyOrder("camunda-admins", "readers", "editors");

        // Check members of a specific group
        Optional<Group> camundaAdmins = StreamSupport.stream(groups.spliterator(), false)
            .filter(g -> g.getName().equals("camunda-admins"))
            .findFirst();

        assertThat(camundaAdmins).isPresent();
        assertThat(camundaAdmins.get().getMembers())
            .containsExactly("uid=camunda-admin,ou=users,dc=example,dc=ch");
    }

}