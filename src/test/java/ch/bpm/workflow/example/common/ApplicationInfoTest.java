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
        assertAll(() -> assertNotNull(buildProperties.getVersion()), () -> assertFalse(buildProperties.getVersion().isEmpty()));
    }


    @Test
    void testApplicationMavenJavaVersion() {
        assertAll(() -> assertNotNull(buildProperties.get("javaVersion")), () -> assertFalse(buildProperties.get("javaVersion").isEmpty()));
    }

    @Test
    void testApplicationMavenJavaVendor() {
        assertAll(() -> assertNotNull(buildProperties.get("javaVendor")), () -> assertFalse(buildProperties.get("javaVendor").isEmpty()));
    }

    @Test
    void testApplicationMavenUser() {
        assertAll(() -> assertNotNull(buildProperties.get("mavenUser")), () -> assertFalse(buildProperties.get("mavenUser").isEmpty()));
    }

    @Test
    void testApplicationMavenGroupId() {
        assertAll(() -> assertNotNull(buildProperties.getGroup()), () -> assertFalse(buildProperties.getGroup().isEmpty()));
    }

    @Test
    void testApplicationMavenArtifactId() {
        assertAll(() -> assertNotNull(buildProperties.getArtifact()), () -> assertFalse(buildProperties.getArtifact().isEmpty()));
    }


    @Test
    void testApplicationMavenTimeStamp() {
        assertAll(() -> assertNotNull(buildProperties.getTime()), () -> assertFalse(buildProperties.getTime().toString().isEmpty()));
    }

    @Test
    void testApplicationGitCommitId() {
        assertAll(() -> assertNotNull(buildProperties.get("commit-id")), () -> assertFalse(buildProperties.get("commit-id").isEmpty()));
    }

}
