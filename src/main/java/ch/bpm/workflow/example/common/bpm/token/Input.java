package ch.bpm.workflow.example.common.bpm.token;

import lombok.Getter;
import lombok.NoArgsConstructor;
import spinjar.com.fasterxml.jackson.annotation.JsonCreator;
import spinjar.com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@Getter
@NoArgsConstructor
public class Input implements Serializable {

    private String inputVariable;

    @JsonCreator
    public Input(@JsonProperty("inputVariable") String inputVariable) {
        this.inputVariable = inputVariable;
    }

}
