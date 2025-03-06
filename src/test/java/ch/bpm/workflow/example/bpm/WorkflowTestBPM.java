package ch.bpm.workflow.example.bpm;

import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.cibseven.bpm.engine.ProcessEngine;
import org.cibseven.bpm.engine.RuntimeService;
import org.cibseven.bpm.engine.runtime.ProcessInstance;
import org.cibseven.bpm.engine.test.Deployment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.cibseven.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;


@SpringBootTest
@TestPropertySource(properties = {
        "camunda.bpm.job-execution.enabled=false",
        "camunda.bpm.generate-unique-process-engine-name=true",
        "camunda.bpm.generate-unique-process-application-name=true",
        "spring.datasource.generate-unique-name=true"
})
@Deployment(resources = "process.bpmn")
@Slf4j
@ActiveProfiles(value = "local")
class WorkflowTestBPM {

    @Autowired
    public RuntimeService runtimeService;

    // See https://docs.camunda.org/manual/latest/user-guide/spring-boot-integration/develop-and-test/#using-assertions-with-context-caching
    @Autowired
    ProcessEngine processEngine;

    @Autowired
    private DataSource dataSource;


    @BeforeEach
    void setUp() {
        init(processEngine);
        log.info("### ProcessEngine started: {}", processEngine.getName());
    }

    @AfterEach
    void tearDown() {
        log.info("DataSource after test: {}", dataSource);
    }

    @Test
    void shouldExecuteHappyPath() {
        // given
        String processDefinitionKey = "hello-world-process";

        // when
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey);

        // then
        assertThat(processInstance).isStarted().task().hasDefinitionKey("say-hello").hasCandidateUser("admin").isNotAssigned();
        assertThat(processInstance).isWaitingAt("say-hello");

        complete(task());

        assertThat(processInstance).hasPassed("Activity_say_hello-via_delegate");
        assertThat(processInstance).isEnded();
    }

}
