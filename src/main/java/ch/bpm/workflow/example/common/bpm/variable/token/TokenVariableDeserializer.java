package ch.bpm.workflow.example.common.bpm.variable.token;

import spinjar.tools.jackson.core.JsonParser;
import spinjar.tools.jackson.databind.DeserializationContext;
import spinjar.tools.jackson.databind.JsonNode;
import spinjar.tools.jackson.databind.ValueDeserializer;

import java.util.Arrays;
import java.util.stream.Collectors;

class TokenVariableDeserializer extends ValueDeserializer<TokenVariable> {

    @Override
    public TokenVariable deserialize(JsonParser parser, DeserializationContext ctxt) {
        JsonNode node = ctxt.readTree(parser);
        if (node.isTextual()) {
            try (JsonParser nestedParser = ctxt.createParser(node.asString())) {
                node = ctxt.readTree(nestedParser);
            }
        }

        String statusString = node.get("status").asText();
        try {
            TokenStatus.valueOf(statusString);
        }
        catch (IllegalArgumentException e) {
            String validStatuses = Arrays.stream(TokenStatus.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
            throw new IllegalArgumentException(
                    "Invalid status: " + statusString + ". Status should be one of " + validStatuses, e);
        }
        Input input = ctxt.readTreeAsValue(node.get("input"), Input.class);

        return new TokenVariable(input, statusString);
    }

}
