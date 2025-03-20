package ch.bpm.workflow.example.controller;

import ch.bpm.workflow.example.common.bpm.WorkflowException;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cibseven.bpm.engine.AuthorizationException;
import org.cibseven.bpm.engine.RuntimeService;
import org.cibseven.bpm.engine.runtime.ProcessInstance;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.Serializable;
import java.util.Map;

import static ch.bpm.workflow.example.common.bpm.WorkflowConstants.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/restapi/workflow", produces = "application/json")
public class WorkflowRestController {

    private final RuntimeService runtimeService;

    @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<HelloWorldWorklfowResponse> startProcess(@RequestBody @Valid InfoRequest infoRequest) {
        try {
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, BUSINESS_KEY, Map.of(INPUT_VARIABLE_NAME, infoRequest.input));
            return ResponseEntity.ok().body(createResponse(processInstance));
        } catch (AuthorizationException ex) {
            log.error("Failed to authorize user: {}", ex.getMessage());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ex.getMessage(), ex);
        } catch (WorkflowException ex) {
            log.error("Failed to process request: {}", infoRequest);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    private HelloWorldWorklfowResponse createResponse(ProcessInstance processInstance) {
        return HelloWorldWorklfowResponse.builder()
                                         .caseInstanceId(processInstance.getCaseInstanceId())
                                         .processInstanceId(processInstance.getProcessInstanceId())
                                         .id(processInstance.getId())
                                         .rootProcessInstanceId(processInstance.getRootProcessInstanceId())
                                         .processDefinitionId(processInstance.getProcessDefinitionId())
                                         .build();
    }

    @Builder
    public record HelloWorldWorklfowResponse(String caseInstanceId, String processDefinitionId, String rootProcessInstanceId, String id, String processInstanceId) {
    }

    @Builder
    public record InfoRequest(
        @NotNull(message = "Input must not be null")
        @JsonProperty(required = true)
        String input
    ) implements Serializable {}
}
