package ch.bpm.workflow.example.common;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "camunda.bpm.job-execution.enabled=false",
    "camunda.bpm.client.disable-auto-fetching=true"
})
@ActiveProfiles(profiles = "test")
class ApplicationInfoTest {

    @Autowired
    BuildProperties buildProperties;

    @Test
    void testApplicationMavenVersion() {
        assertAll(
            () -> assertNotNull(buildProperties.getVersion()),
            () -> assertFalse(buildProperties.getVersion().isEmpty())
        );
    }

    @Test
    void testApplicationMavenGroupId() {
        assertAll(
            () -> assertNotNull(buildProperties.getGroup()),
            () -> assertFalse(buildProperties.getGroup().isEmpty()));
    }

    @Test
    void testApplicationMavenArtifactId() {
        assertAll(
            () -> assertNotNull(buildProperties.getArtifact()),
            () -> assertFalse(buildProperties.getArtifact().isEmpty()));
    }

    @Test
    void testApplicationMavenName() {
        assertAll(
            () -> assertNotNull(buildProperties.getName()),
            () -> assertFalse(buildProperties.getName().isEmpty()));
    }

    @Test
    void testApplicationMavenTimeStamp() {
        assertAll(
            () -> assertNotNull(buildProperties.getTime()),
            () -> assertFalse(buildProperties.getTime().toString().isEmpty()));
    }
}
