package ch.bpm.workflow.example.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cibseven.bpm.engine.AuthorizationService;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import org.cibseven.bpm.engine.authorization.Authorization;
import org.cibseven.bpm.engine.authorization.Permissions;
import org.cibseven.bpm.engine.authorization.Resources;

import static ch.bpm.workflow.example.common.bpm.WorkflowConstants.PROCESS_DEFINITION_KEY;
import static org.cibseven.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;

@Configuration
@Profile({"local", "ci"})
@RequiredArgsConstructor
@Slf4j
// TODO: ADD CONFIGURABLE PERMISSIONS HERE FOR USERS (user01, user02,...) AND GROUPS
public class AuthorizationConfig {

    private final AuthorizationService authorizationService;

    @PostConstruct
    public void init() {
        setPermissions();
    }

    private void setPermissions() {
        Authorization authProcessDefinition = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
        Authorization authProcessInstance = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);

        authProcessDefinition.setResource(Resources.PROCESS_DEFINITION);
        authProcessInstance.setResource(Resources.PROCESS_INSTANCE);

        authProcessDefinition.setUserId("user01");
        authProcessInstance.setUserId("user01");

        authProcessDefinition.setResourceId(PROCESS_DEFINITION_KEY);
        authProcessInstance.setResourceId("*");
        authProcessDefinition.addPermission(Permissions.CREATE_INSTANCE);
        authProcessInstance.addPermission(Permissions.CREATE);

        authorizationService.saveAuthorization(authProcessDefinition);
        authorizationService.saveAuthorization(authProcessInstance);

        log.info("Permissions set successfully! User 'user01' has the necessary permissions to start and manage processes." +
            " authProcessDefinition: {} and authProcessInstance: {}", authProcessDefinition, authProcessInstance);
    }

}
