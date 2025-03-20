package ch.bpm.workflow.example.ldap.repository;

import ch.bpm.workflow.example.ldap.model.Person;
import org.springframework.data.repository.CrudRepository;

import javax.naming.Name;

public interface PersonRepository extends CrudRepository<Person, Name> {
}