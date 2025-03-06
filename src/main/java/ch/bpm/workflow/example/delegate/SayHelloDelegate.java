package ch.bpm.workflow.example.delegate;

import ch.guru.springframework.apifirst.client.CustomerApi;
import ch.guru.springframework.apifirst.model.CustomerDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class SayHelloDelegate implements JavaDelegate {

    final CustomerApi customerApi;

    @Override
    public void execute(DelegateExecution delegateExecution) {

        log.info("executing sayHelloDelegate: {}", delegateExecution);

        List<CustomerDto> customers = customerApi.listCustomers();
        log.info("Got response from apifirst. Customers: \n {}", customers);
    }

}
