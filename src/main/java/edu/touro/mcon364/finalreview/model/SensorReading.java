package edu.touro.mcon364.finalreview.model;

public record SensorReading(String sensorId, double value, long timestamp) {
    public SensorReading {
        if (sensorId == null || sensorId.isBlank()) {
            throw new IllegalArgumentException("sensorId must be non-blank");
        }
    }
}
