package ch.bpm.workflow.example.ldap.repository;

import ch.bpm.workflow.example.ldap.model.Person;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles(profiles = "test")
@TestPropertySource(properties = {
    "camunda.bpm.job-execution.enabled=false",
    "camunda.bpm.client.disable-auto-fetching=true"
})
@Slf4j
class PersonRepositoryTest {

    @Autowired
    PersonRepository personRepository;

    @Autowired
    private LdapTemplate ldapTemplate;

    @Test
    void testLdapConnection() {
        assertThat(ldapTemplate.list("")).isNotEmpty();
    }

    @Test
    void findPersonByUid() throws InvalidNameException {
        Optional<Person> person = personRepository.findById(new LdapName("cn=user01,ou=users,dc=example,dc=ch"));

        assertThat(person).hasValueSatisfying(p -> {
            assertThat(p.getMail()).isEqualTo("john.doe@example.com");
            assertThat(p.getCommonName()).isEqualTo("user01");
            assertThat(p.getGivenName()).isEqualTo("John");
            assertThat(p.getLastname()).isEqualTo("Doe");
            assertThat(p.getUid()).isEqualTo("user01");
        });
    }

    @Test
    void findAllPerson() {
        Iterable<Person> peoples = personRepository.findAll();
        assertThat(peoples).hasSize(4).extracting("uid").contains("camunda-admin", "user01", "user02", "user03");
    }
}