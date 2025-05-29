package ch.bpm.workflow.example.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Antwortobjekt für den Ping-Endpoint")
public record PingResponse(
    @Schema(description = "Maven Group ID") String mavenGroupdId,
    @Schema(description = "Maven Artifact ID") String mavenArtifactId,
    @Schema(description = "Version der Anwendung") String version,
    @Schema(description = "Java Vendor") String vendor,
    @Schema(description = "Java Version") String javaVersion,
    @Schema(description = "Maven User") String mavenUser,
    @Schema(description = "Build Timestamp") String mavenTimeStamp,
    @Schema(description = "Git Commit ID") String gitCommitId
) {}