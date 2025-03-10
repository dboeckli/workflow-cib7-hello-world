package ch.bpm.workflow.example.delegate;

import ch.bpm.workflow.example.common.bpm.WorkflowException;
import ch.bpm.workflow.example.common.bpm.token.TokenVariable;
import ch.guru.springframework.apifirst.client.CustomerApi;
import ch.guru.springframework.apifirst.model.CustomerDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cibseven.bpm.engine.delegate.DelegateExecution;
import org.cibseven.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.List;

import static ch.bpm.workflow.example.common.bpm.token.TokenVariable.TOKEN_VARIABLE_NAME;

@Slf4j
@RequiredArgsConstructor
@Component
public class SayHelloDelegate implements JavaDelegate {

    final CustomerApi customerApi;

    @Override
    public void execute(DelegateExecution delegateExecution) {
        log.info("executing sayHelloDelegate: {}", delegateExecution);
        try {
            List<CustomerDto> customers = customerApi.listCustomers();
            log.info("Got response from apifirst. Customers: \n {}", customers);

            TokenVariable tokenVariable = (TokenVariable)delegateExecution.getVariable(TOKEN_VARIABLE_NAME);
            tokenVariable.setStatus("DONE");
        } catch (Exception ex) {
            log.error("Failed to call apifirst", ex);
            throw new WorkflowException("Failed to call apifirst", ex);
        }
    }
}
