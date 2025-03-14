package ch.bpm.workflow.example.execution.listener;

import ch.bpm.workflow.example.common.bpm.token.TokenVariable;
import lombok.extern.slf4j.Slf4j;
import org.cibseven.bpm.engine.delegate.DelegateExecution;
import org.cibseven.bpm.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

import static ch.bpm.workflow.example.common.bpm.token.TokenVariable.Status.COMPLETED;
import static ch.bpm.workflow.example.common.bpm.token.TokenVariable.TOKEN_VARIABLE_NAME;

@Slf4j
@Component
public class HelloExecutionListener implements ExecutionListener {

    @Override
    public void notify(DelegateExecution delegateExecution) {
        log.info("executing HelloExecutionListener: {}", delegateExecution.getCurrentActivityName());

        TokenVariable tokenVariable = (TokenVariable)delegateExecution.getVariable(TOKEN_VARIABLE_NAME);
        tokenVariable.setStatus(COMPLETED.name());

        log.info("executing HelloExecutionListener. Status has been set to: {}", tokenVariable.getStatus());
    }
}
