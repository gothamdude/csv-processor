package com.gothamdude.csv.processor.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "processor")
@Data
public class ProcessorConfig {
    private String csvFilePath;
    private String processName;
    private String interval;
    private boolean enableScheduling = false;
    private int maxRetries = 3;
    private long retryDelayMs = 5000;
}
