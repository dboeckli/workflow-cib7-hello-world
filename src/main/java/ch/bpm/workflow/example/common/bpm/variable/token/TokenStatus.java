package ch.bpm.workflow.example.common.bpm.variable.token;

import lombok.ToString;

import java.io.Serializable;

@ToString
public enum TokenStatus implements Serializable {
    STARTED,
    RUNNING,
    COMPLETED,
    BUSINESS_EXCEPTION,
    FINISHED
}
