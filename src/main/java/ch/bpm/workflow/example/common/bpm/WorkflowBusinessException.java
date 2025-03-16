package ch.bpm.workflow.example.common.bpm;

import org.cibseven.bpm.engine.ProcessEngineException;

public class WorkflowBusinessException extends ProcessEngineException {

    public WorkflowBusinessException(String message, int code) {
        super(message, code);
    }

    public WorkflowBusinessException(String message, int code, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

}
