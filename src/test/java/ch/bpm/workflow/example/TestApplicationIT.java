package ch.bpm.workflow.example;

import lombok.extern.slf4j.Slf4j;
import org.cibseven.bpm.engine.ProcessEngine;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@ActiveProfiles(value = "test")
@Slf4j
class TestApplicationIT {

    @Autowired
    ProcessEngine processEngine;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        log.info("Testing Spring 6 Application {}", applicationContext.getApplicationName());
        log.info("### ProcessEngine started: {}", processEngine.getName());
        assertAll(
            () -> assertNotNull(applicationContext),
            () -> assertEquals("default", processEngine.getName())
        );
    }
}
