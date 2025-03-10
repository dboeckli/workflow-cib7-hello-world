package ch.bpm.workflow.example.common.bpm.token;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class TokenVariable implements Serializable {

    public static final String TOKEN_VARIABLE_NAME = "tokenVariable";

    private String status;

    @Setter(AccessLevel.NONE)
    private Input input;

    public TokenVariable(Input input) {
        this.input = input;
    }

}
