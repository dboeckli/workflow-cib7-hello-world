package ch.bpm.workflow.example.controller;

import lombok.Builder;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/restapi/ping", produces = "application/json")
public class PingRestController {

    final BuildProperties buildProperties;

    public PingRestController(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<PingResponse> getInfo() {
        return ResponseEntity.ok().body(createResponse());
    }

    private PingResponse createResponse() {
        return PingResponse.builder()
                           .mavenGroupdId(buildProperties.getGroup())
                           .mavenArtifactId(buildProperties.getArtifact())
                           .version(buildProperties.getVersion())
                           .vendor(buildProperties.get("javaVendor"))
                           .mavenTimeStamp(buildProperties.getTime().toString())
                           .mavenUser(buildProperties.get("mavenUser"))
                           .javaVersion(buildProperties.get("javaVersion"))
                           .gitCommitId(buildProperties.get("commit-id"))
                           .build();
    }

    @Builder
    public record PingResponse(String mavenGroupdId, String mavenArtifactId, String version, String vendor, String javaVersion, String mavenUser, String mavenTimeStamp,
                               String gitCommitId) {

    }

}
