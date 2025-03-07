package ch.bpm.workflow.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.cibseven.bpm.engine.ProcessEngine;
import org.cibseven.bpm.engine.test.Deployment;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.cibseven.bpm.engine.test.assertions.bpmn.AbstractAssertions.init;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "camunda.bpm.job-execution.enabled=false",
    "camunda.bpm.generate-unique-process-engine-name=true",
    "camunda.bpm.generate-unique-process-application-name=true",
    "spring.datasource.generate-unique-name=true"
})
@DirtiesContext
@ActiveProfiles(value = "local")
@Deployment(resources = "process.bpmn")
@Slf4j
class CamundaRestApiIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    ProcessEngine processEngine;

    @BeforeEach
    void setUp() {
        init(processEngine);
        log.info("### ProcessEngine started: {}", processEngine.getName());
    }

    @Test
    void getEngineInfo() throws JSONException {
        ResponseEntity<String> response = restTemplate
            .getForEntity("/engine-rest/engine", String.class);

        log.info("######### Engine info: {}", response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        JSONArray jsonArray = new JSONArray(response.getBody());
        String expectedEngineName = processEngine.getName();
        boolean expectedEngineFound = false;
        for (int i = 0; i < jsonArray.length(); i++) {
            String engineName = jsonArray.getJSONObject(i).getString("name");
            if (engineName.equals(expectedEngineName)) {
                expectedEngineFound = true;
                break;
            }

        }
        assertThat("Expected engine name not found: " + expectedEngineName, expectedEngineFound, is(true));
    }
}
