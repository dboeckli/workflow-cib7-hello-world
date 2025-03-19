package ch.bpm.workflow.example.delegate;

import ch.bpm.workflow.example.common.bpm.WorkflowException;
import ch.bpm.workflow.example.common.bpm.variable.token.Input;
import ch.bpm.workflow.example.common.bpm.variable.token.TokenVariable;
import ch.bpm.workflow.example.ldap.GroupService;
import ch.bpm.workflow.example.ldap.PersonService;
import ch.bpm.workflow.example.ldap.model.Group;
import ch.bpm.workflow.example.ldap.model.Person;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cibseven.bpm.engine.delegate.DelegateExecution;
import org.cibseven.bpm.engine.delegate.JavaDelegate;
import org.cibseven.bpm.engine.variable.Variables;
import org.cibseven.bpm.engine.variable.value.ObjectValue;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.stereotype.Component;

import java.util.List;

import static ch.bpm.workflow.example.common.bpm.WorkflowConstants.INPUT_VARIABLE_NAME;
import static ch.bpm.workflow.example.common.bpm.variable.token.TokenStatus.STARTED;
import static ch.bpm.workflow.example.common.bpm.variable.token.TokenVariable.TOKEN_VARIABLE_NAME;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidateInputDelegate implements JavaDelegate {

    private final PersonService personService;
    private final GroupService groupService;

    LdapContextSource ldapContextSource;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        log.info("executing ValidateInputDelegate: {}", delegateExecution);
        log.info("executing ValidateInputDelegate. business key: {}", delegateExecution.getProcessBusinessKey());
        log.info("executing ValidateInputDelegate. variables: {}", delegateExecution.getVariables());

        // TODO: add person and group logic here
        List<Person> persons = personService.getAllPersons();
        List<Group> groups = groupService.getAllGroups();

        if (!delegateExecution.getVariables().containsKey(INPUT_VARIABLE_NAME) || delegateExecution.getVariables().get(INPUT_VARIABLE_NAME).toString().isEmpty()) {
            throw new WorkflowException("Variable " + INPUT_VARIABLE_NAME + " not found or empty");
        } else {
            TokenVariable tokenVariable = new TokenVariable(new Input(delegateExecution.getVariables().get(INPUT_VARIABLE_NAME).toString()));
            tokenVariable.setStatus(STARTED);

            ObjectValue tokenVariableValue = Variables
                .objectValue(tokenVariable)
                .serializationDataFormat(Variables.SerializationDataFormats.JAVA)
                //.serializationDataFormat(Variables.SerializationDataFormats.JSON)
                .create();

            delegateExecution.setVariable(TOKEN_VARIABLE_NAME, tokenVariableValue);
        }
    }
}
