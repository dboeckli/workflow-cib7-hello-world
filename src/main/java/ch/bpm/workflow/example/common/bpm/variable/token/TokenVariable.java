package ch.bpm.workflow.example.common.bpm.variable.token;

import lombok.*;
import spinjar.com.fasterxml.jackson.annotation.JsonCreator;
import spinjar.com.fasterxml.jackson.annotation.JsonProperty;
import spinjar.com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.Serializable;

@Getter
@Setter
@ToString
@JsonDeserialize(using = TokenVariableDeserializer.class)
public class TokenVariable implements Serializable {

    public static final String TOKEN_VARIABLE_NAME = "tokenVariable";

    @JsonProperty("status")
    private String status;

    @JsonProperty("input")
    @Setter(AccessLevel.NONE)
    private Input input;

    public TokenVariable(Input input) {
        this.input = input;
    }

    @JsonCreator
    public TokenVariable() {
    }

    @JsonCreator
    public TokenVariable(@JsonProperty("input") Input input, @JsonProperty("status") String status) {
        this.input = input;
        this.status = status;
    }

    public void setStatus(TokenStatus status) {
        this.status = status.name();
    }

    public TokenStatus getStatus() {
        return TokenStatus.valueOf(status);
    }

}
