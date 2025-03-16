package ch.bpm.workflow.example.delegate;

import ch.bpm.workflow.example.common.bpm.WorkflowException;
import ch.bpm.workflow.example.common.bpm.variable.token.TokenVariable;
import ch.guru.springframework.apifirst.client.CustomerApi;
import ch.guru.springframework.apifirst.model.CustomerDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cibseven.bpm.engine.delegate.BpmnError;
import org.cibseven.bpm.engine.delegate.DelegateExecution;
import org.cibseven.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static ch.bpm.workflow.example.common.bpm.variable.token.TokenStatus.BUSINESS_EXCEPTION;
import static ch.bpm.workflow.example.common.bpm.variable.token.TokenStatus.FINISHED;
import static ch.bpm.workflow.example.common.bpm.variable.token.TokenVariable.TOKEN_VARIABLE_NAME;

@Slf4j
@RequiredArgsConstructor
@Component
public class SayHelloDelegate implements JavaDelegate {

    final CustomerApi customerApi;

    @Override
    public void execute(DelegateExecution delegateExecution) throws WorkflowException {
        TokenVariable tokenVariable = (TokenVariable)delegateExecution.getVariable(TOKEN_VARIABLE_NAME);
        String errorCode = Optional.ofNullable(delegateExecution.getVariable("errorCode"))
            .map(Object::toString)
            .orElse(null);
        log.info("### executing sayHelloDelegate: {}. Variable status: {}. Error code: {}", delegateExecution, tokenVariable.getStatus(), errorCode);
        try {
            if (tokenVariable.getInput().getInputVariable().equals("fail") && errorCode == null) {
                throw new WorkflowException("fail required");
            } else {
                List<CustomerDto> customers = customerApi.listCustomers();
                log.info("Got response from apifirst. Customers: \n {}", customers);
                tokenVariable.setStatus(FINISHED);
            }
        } catch (Exception ex) {
            log.error("Failed to call apifirst. Creating Business Error", ex);
            tokenVariable.setStatus(BUSINESS_EXCEPTION);
            throw new BpmnError(BUSINESS_EXCEPTION.name(), "Failed to call apifirst.");
            //throw new WorkflowBusinessException(ex.getMessage(), 20000, ex);
        }
    }
}
