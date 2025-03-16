package ch.bpm.workflow.example.common.bpm.variable.token;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spinjar.com.fasterxml.jackson.core.JsonParser;
import spinjar.com.fasterxml.jackson.databind.DeserializationContext;
import spinjar.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import static ch.bpm.workflow.example.common.bpm.variable.token.TokenStatus.RUNNING;
import static org.junit.jupiter.api.Assertions.*;

class TokenVariableDeserializerTest {

    private TokenVariableDeserializer deserializer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        deserializer = new TokenVariableDeserializer();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testDeserialize() throws IOException {
        // Given
        String json = "{\"status\":\"RUNNING\",\"input\":{\"inputVariable\":\"testInput\"}}";
        JsonParser jsonParser = objectMapper.getFactory().createParser(json);
        DeserializationContext ctxt = objectMapper.getDeserializationContext();

        // When
        TokenVariable result = deserializer.deserialize(jsonParser, ctxt);

        // Then
        assertNotNull(result);
        assertEquals(RUNNING, result.getStatus());
        assertNotNull(result.getInput());
        assertEquals("testInput", result.getInput().getInputVariable());
    }

    @Test
    void testDeserializeWithTextualNode() throws IOException {
        // Given
        String json = "\"" + "{\\\"status\\\":\\\"RUNNING\\\",\\\"input\\\":{\\\"inputVariable\\\":\\\"testInput\\\"}}" + "\"";
        JsonParser jsonParser = objectMapper.getFactory().createParser(json);
        DeserializationContext ctxt = objectMapper.getDeserializationContext();

        // When
        TokenVariable result = deserializer.deserialize(jsonParser, ctxt);

        // Then
        assertNotNull(result);
        assertEquals(RUNNING, result.getStatus());
        assertNotNull(result.getInput());
        assertEquals("testInput", result.getInput().getInputVariable());
    }

    @Test
    void testDeserializeWithInvalidStatus() throws IOException {
        // Given
        String json = "{\"status\":\"INVALID\",\"input\":{\"inputVariable\":\"testInput\"}}";
        JsonParser jsonParser = objectMapper.getFactory().createParser(json);
        DeserializationContext ctxt = objectMapper.getDeserializationContext();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            deserializer.deserialize(jsonParser, ctxt);
        });

        String expectedErrorMessage = "Invalid status: INVALID. Status should be one of STARTED, RUNNING, COMPLETED, BUSINESS_EXCEPTION, FINISHED";
        assertEquals(expectedErrorMessage, exception.getMessage());
    }
}