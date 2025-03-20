package ch.bpm.workflow.example.ldap;

import ch.bpm.workflow.example.ldap.model.Person;
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
class PersonServiceTest {

    @Autowired
    PersonService personService;

    @Test
    void getAllPersons() {
        List<Person> persons = personService.getAllPersons();
        log.info("Persons: {}", persons);
        assertThat(persons).isNotNull().hasSize(4);
    }
}