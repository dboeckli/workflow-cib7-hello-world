package ch.bpm.workflow.example.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.parser.OpenAPIV3Parser;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@OpenAPIDefinition(
    info = @Info(
        license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0")
    )
)
@Configuration
@RequiredArgsConstructor
public class OpenApiConfiguration {

    private final BuildProperties buildProperties;

    @Bean
    @Qualifier("customGlobalHeaderOpenApiCustomizer")
    public OpenApiCustomizer customGlobalHeaderOpenApiCustomizer() {
        return openApi -> {
            io.swagger.v3.oas.models.info.Info info = openApi.getInfo();
            info.setTitle(buildProperties.getName());
            info.setVersion(buildProperties.getVersion());
        };
    }

    @Bean
    // TODO: ADD SECURITY
    public GroupedOpenApi restapiApi(@Qualifier("customGlobalHeaderOpenApiCustomizer") OpenApiCustomizer customGlobalHeaderOpenApiCustomizer) {
        return GroupedOpenApi.builder()
            .group("restapi")
            .addOpenApiCustomizer(customGlobalHeaderOpenApiCustomizer)
            .displayName("REST API")
            .pathsToMatch("/restapi/**")
            .build();
    }

    @Bean
    public GroupedOpenApi actuatorApi(@Qualifier("customGlobalHeaderOpenApiCustomizer") OpenApiCustomizer customGlobalHeaderOpenApiCustomizer) {
        return GroupedOpenApi.builder()
            .group("actuator")
            .addOpenApiCustomizer(customGlobalHeaderOpenApiCustomizer)
            .displayName("Actuator")
            .pathsToMatch("/actuator/**")
            .build();
    }

    @Bean
    public GroupedOpenApi camnundaEngineRestApi(@Qualifier("customGlobalHeaderOpenApiCustomizer") OpenApiCustomizer customGlobalHeaderOpenApiCustomizer) {
        return GroupedOpenApi.builder()
            .group("camunda-engine-rest-api")
            .addOpenApiCustomizer(customGlobalHeaderOpenApiCustomizer)
            .displayName("Camunda Engine Rest API")
            .pathsToMatch("/engine-rest/**")
            .addOpenApiCustomizer(openApi -> {
                try {
                    // TODO: PROVIDE CONFIGURATION
                    URL url = new URL("http://localhost:8081/restapi/camunda");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    // Set up basic auth
                    // TODO: PROVIDE CONFIGURATION
                    String auth = "camunda-admin" + ":" + "camunda-admin-password";
                    String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
                    String authHeaderValue = "Basic " + encodedAuth;
                    connection.setRequestProperty("Authorization", authHeaderValue);

                    // Read the response
                    StringBuilder content = new StringBuilder();
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            content.append(inputLine);
                        }
                    }

                    OpenAPI externalOpenApi = new OpenAPIV3Parser().readContents(content.toString()).getOpenAPI();

                    // Set the correct server URL
                    externalOpenApi.getServers().clear();
                    externalOpenApi.addServersItem(new Server().url("http://localhost:8081/engine-rest"));

                    // Merge paths and components from external OpenAPI into the current one
                    openApi.getPaths().putAll(externalOpenApi.getPaths());
                    if (externalOpenApi.getComponents() != null) {
                        if (openApi.getComponents() == null) {
                            openApi.setComponents(new Components());
                        }
                        openApi.getComponents().getSchemas().putAll(externalOpenApi.getComponents().getSchemas());
                    }

                    openApi.setServers(externalOpenApi.getServers());

                } catch (Exception e) {
                    throw new RuntimeException("Failed to load external OpenAPI definition", e);
                }
            })
            .build();
    }
}
