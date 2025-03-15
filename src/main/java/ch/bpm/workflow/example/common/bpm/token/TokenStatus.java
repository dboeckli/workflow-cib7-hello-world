package ch.bpm.workflow.example.common.bpm.token;

import java.io.Serializable;

public enum TokenStatus implements Serializable {
    STARTED,
    RUNNING,
    COMPLETED,
    FINISHED
}
