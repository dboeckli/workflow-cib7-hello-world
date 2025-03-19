package ch.bpm.workflow.example.ldap;

import ch.bpm.workflow.example.ldap.model.Group;
import ch.bpm.workflow.example.ldap.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;

    public List<Group> getAllGroups() {
        return StreamSupport.stream(groupRepository.findAll().spliterator(), false)
            .collect(Collectors.toList());
    }
}
