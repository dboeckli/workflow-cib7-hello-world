package ch.bpm.workflow.example.common;

import org.slf4j.helpers.MessageFormatter;

public enum LogMessage {

    // System
    READING_CONFIG_CLASS(10001, "[{}] Reading configuration from {} config map: {}"),
    RECEIVED_REFRESH_EVENT_FOR_CONFIG_MAP(10002, "[{}] Received Refresh event for Config Map configuration: {}");

    private final int id;

    private final String format;

    LogMessage(final int id, final String format) {
        this.format = format;
        this.id = id;
    }

    public String formatMessage(final Object... arguments) {
        return MessageFormatter.arrayFormat(getMessage(), arguments).getMessage();
    }

    public String getMessage() {
        return MessageFormatter.format(format, "logid=" + id).getMessage();
    }

}
