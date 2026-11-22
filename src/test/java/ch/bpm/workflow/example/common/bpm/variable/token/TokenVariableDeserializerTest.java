package ch.bpm.workflow.example.common.bpm.variable.token;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spinjar.tools.jackson.databind.ObjectMapper;
import spinjar.tools.jackson.databind.json.JsonMapper;

import static ch.bpm.workflow.example.common.bpm.variable.token.TokenStatus.RUNNING;
import static org.junit.jupiter.api.Assertions.*;

class TokenVariableDeserializerTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().build();
    }

    @Test
    void testDeserialize() {
        // Given
        String json = "{\"status\":\"RUNNING\",\"input\":{\"inputVariable\":\"testInput\"}}";

        // When
        TokenVariable result = objectMapper.readValue(json, TokenVariable.class);

        // Then
        assertNotNull(result);
        assertEquals(RUNNING, result.getStatus());
        assertNotNull(result.getInput());
        assertEquals("testInput", result.getInput().getInputVariable());
    }

    @Test
    void testDeserializeWithTextualNode() {
        // Given
        String json = "\"" + "{\\\"status\\\":\\\"RUNNING\\\",\\\"input\\\":{\\\"inputVariable\\\":\\\"testInput\\\"}}"
                + "\"";

        // When
        TokenVariable result = objectMapper.readValue(json, TokenVariable.class);

        // Then
        assertNotNull(result);
        assertEquals(RUNNING, result.getStatus());
        assertNotNull(result.getInput());
        assertEquals("testInput", result.getInput().getInputVariable());
    }

    @Test
    void testDeserializeWithInvalidStatus() {
        // Given
        String json = "{\"status\":\"INVALID\",\"input\":{\"inputVariable\":\"testInput\"}}";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            objectMapper.readValue(json, TokenVariable.class);
        });

        String expectedErrorMessage = "Invalid status: INVALID. Status should be one of STARTED, RUNNING, COMPLETED, BUSINESS_EXCEPTION, FINISHED";
        assertEquals(expectedErrorMessage, exception.getMessage());
    }

}
