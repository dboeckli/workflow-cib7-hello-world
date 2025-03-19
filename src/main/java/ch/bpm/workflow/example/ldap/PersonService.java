package ch.bpm.workflow.example.ldap;

import ch.bpm.workflow.example.ldap.model.Person;
import ch.bpm.workflow.example.ldap.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;

    public List<Person> getAllPersons() {
        return StreamSupport.stream(personRepository.findAll().spliterator(), false)
            .collect(Collectors.toList());
    }

}
