package edu.touro.mcon364.finalreview.model;

/**
 * One message submitted by a producer and processed by a consumer.
 */
public record LogMessage(String source, String message, LogLevel level, long timestamp) {
    public LogMessage {
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("source must be non-blank");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must be non-blank");
        }
        if (level == null) {
            throw new IllegalArgumentException("level must not be null");
        }
    }
}
