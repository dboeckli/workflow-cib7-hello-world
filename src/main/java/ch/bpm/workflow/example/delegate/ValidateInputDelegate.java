package ch.bpm.workflow.example.delegate;

import ch.bpm.workflow.example.common.bpm.WorkflowException;
import ch.bpm.workflow.example.common.bpm.token.Input;
import ch.bpm.workflow.example.common.bpm.token.TokenVariable;
import lombok.extern.slf4j.Slf4j;
import org.cibseven.bpm.engine.delegate.DelegateExecution;
import org.cibseven.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import static ch.bpm.workflow.example.common.bpm.WorkflowConstants.INPUT_VARIABLE_NAME;
import static ch.bpm.workflow.example.common.bpm.token.TokenVariable.TOKEN_VARIABLE_NAME;

@Slf4j
@Component
public class ValidateInputDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        log.info("executing ValidateInputDelegate: {}", delegateExecution);
        log.info("executing ValidateInputDelegate. business key: {}", delegateExecution.getProcessBusinessKey());
        log.info("executing ValidateInputDelegate. variables: {}", delegateExecution.getVariables());
        if (!delegateExecution.getVariables().containsKey(INPUT_VARIABLE_NAME) || delegateExecution.getVariables().get(INPUT_VARIABLE_NAME).toString().isEmpty()) {
            throw new WorkflowException("Variable " + INPUT_VARIABLE_NAME + " not found or empty");
        } else {
            TokenVariable tokenVariable = new TokenVariable(new Input(delegateExecution.getVariables().get(INPUT_VARIABLE_NAME).toString()));
            tokenVariable.setStatus(TokenVariable.STATUS.STARTED);
            delegateExecution.setVariable(TOKEN_VARIABLE_NAME, tokenVariable);
        }
    }
}
