package ch.bpm.workflow.example.ldap.repository;

import ch.bpm.workflow.example.ldap.model.Person;
import org.springframework.data.repository.CrudRepository;

import javax.naming.Name;
import java.util.List;

public interface PersonRepository extends CrudRepository<Person, Name> {
    List<Person> findByGivenName(String givenName);
}