package ch.bpm.workflow.example.common.bpm.variable.token;

import spinjar.com.fasterxml.jackson.core.JsonParser;
import spinjar.com.fasterxml.jackson.databind.DeserializationContext;
import spinjar.com.fasterxml.jackson.databind.JsonDeserializer;
import spinjar.com.fasterxml.jackson.databind.JsonNode;
import spinjar.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

class TokenVariableDeserializer extends JsonDeserializer<TokenVariable> {
    @Override
    public TokenVariable deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) parser.getCodec();
        JsonNode node = mapper.readTree(parser);
        if (node.isTextual()) {
            node = mapper.readTree(node.asText());
        }

        String statusString = node.get("status").asText();
        try {
            TokenStatus.valueOf(statusString);
        } catch (IllegalArgumentException e) {
            String validStatuses = Arrays.stream(TokenStatus.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Invalid status: " + statusString + ". Status should be one of " + validStatuses, e);
        }
        JsonNode inputNode = node.get("input");
        Input input = mapper.treeToValue(inputNode, Input.class);

        return new TokenVariable(input, statusString);
    }
}
