package ch.bpm.workflow.example.common.bpm.token;

import lombok.*;
import spinjar.com.fasterxml.jackson.annotation.JsonCreator;
import spinjar.com.fasterxml.jackson.annotation.JsonProperty;
import spinjar.com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.Serializable;

@Getter
@Setter
@ToString
@NoArgsConstructor
@JsonDeserialize(using = TokenVariableDeserializer.class)
public class TokenVariable implements Serializable {

    public static final String TOKEN_VARIABLE_NAME = "tokenVariable";

    private String status;

    @Setter(AccessLevel.NONE)
    private Input input;

    @JsonCreator
    public TokenVariable(@JsonProperty("input") Input input, @JsonProperty("status") String status) {
        this.input = input;
        this.status = status;
    }

    public enum Status implements Serializable {
        STARTED,
        RUNNING,
        COMPLETED,
        FINISHED
    }
}
