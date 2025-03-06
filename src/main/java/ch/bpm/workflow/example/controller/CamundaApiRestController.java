package ch.bpm.workflow.example.controller;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/restapi/camunda", produces = "application/json")
public class CamundaApiRestController {

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public String getFile() {
        InputStream inputStream = getClass().getResourceAsStream("/openapi.json");
        try {
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
            throw new WebApplicationException(response);
        }
    }

}
