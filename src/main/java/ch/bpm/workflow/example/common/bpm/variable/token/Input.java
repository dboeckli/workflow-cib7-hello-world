package ch.bpm.workflow.example.common.bpm.variable.token;

import lombok.Getter;
import lombok.ToString;
import spinjar.com.fasterxml.jackson.annotation.JsonCreator;
import spinjar.com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@Getter
@ToString
public class Input implements Serializable {

    @JsonProperty("inputVariable")
    private String inputVariable;

    @JsonCreator
    public Input() {
    }

    @JsonCreator
    public Input(@JsonProperty("inputVariable") String inputVariable) {
        this.inputVariable = inputVariable;
    }

}
