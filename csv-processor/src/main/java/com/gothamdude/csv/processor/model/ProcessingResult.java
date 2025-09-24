package com.gothamdude.csv.processor.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
public class ProcessingResult {
    private Integer runId;
    private String processName;
    private boolean success;
    private long recordsProcessed;
    private long executionTimeMs;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String errorMessage;
    private Map<String, Object> metrics;

    public ProcessingResult(String processName) {
        this.processName = processName;
        this.startTime = LocalDateTime.now();
    }

    public void markComplete(long recordsProcessed) {
        this.endTime = LocalDateTime.now();
        this.success = true;
        this.recordsProcessed = recordsProcessed;
        this.executionTimeMs = java.time.Duration.between(startTime, endTime).toMillis();
    }

    public void markFailed(String errorMessage) {
        this.endTime = LocalDateTime.now();
        this.success = false;
        this.errorMessage = errorMessage;
        this.executionTimeMs = java.time.Duration.between(startTime, endTime).toMillis();
    }
}