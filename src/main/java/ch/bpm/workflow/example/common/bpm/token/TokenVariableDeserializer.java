package ch.bpm.workflow.example.common.bpm.token;

import spinjar.com.fasterxml.jackson.core.JsonParser;
import spinjar.com.fasterxml.jackson.databind.DeserializationContext;
import spinjar.com.fasterxml.jackson.databind.JsonDeserializer;
import spinjar.com.fasterxml.jackson.databind.JsonNode;
import spinjar.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

class TokenVariableDeserializer extends JsonDeserializer<TokenVariable> {
    @Override
    public TokenVariable deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) parser.getCodec();
        JsonNode node = mapper.readTree(parser);
        if (node.isTextual()) {
            node = mapper.readTree(node.asText());
        }

        String status = node.get("status").asText();
        JsonNode inputNode = node.get("input");
        Input input = mapper.treeToValue(inputNode, Input.class);

        return new TokenVariable(input, status);
    }
}
