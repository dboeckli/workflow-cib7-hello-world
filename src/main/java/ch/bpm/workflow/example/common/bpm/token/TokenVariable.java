package ch.bpm.workflow.example.common.bpm.token;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class TokenVariable implements Serializable {

    public TokenVariable(Input input) {
        this.input = input;
    }

    public static final String TOKEN_VARIABLE_NAME = "tokenVariable";

    public enum Status {
        STARTED,
        RUNNING,
        COMPLETED,
        FINISHED
    }

    private Status status;

    @Setter(AccessLevel.NONE)
    private Input input;
}
