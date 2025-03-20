package ch.bpm.workflow.example.ldap.repository;

import ch.bpm.workflow.example.ldap.model.Group;
import org.springframework.data.repository.CrudRepository;

import javax.naming.Name;

public interface GroupRepository extends CrudRepository<Group, Name> {
}
