package ch.bpm.workflow.example.ldap;

import ch.bpm.workflow.example.ldap.model.Group;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles(profiles = "test")
@TestPropertySource(properties = {
    "camunda.bpm.job-execution.enabled=false",
    "camunda.bpm.client.disable-auto-fetching=true"
})
@Slf4j
class GroupServiceTest {

    @Autowired
    GroupService groupService;

    @Test
    void getAllGroups() {
        List<Group> groups = groupService.getAllGroups();
        log.info("Groups: {}", groups);
        assertThat(groups).isNotNull().hasSize(3);
    }
}