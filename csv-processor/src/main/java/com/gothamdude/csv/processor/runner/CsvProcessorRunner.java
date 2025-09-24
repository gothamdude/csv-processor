package com.gothamdude.csv.processor.runner;

import com.gothamdude.csv.processor.config.ProcessorConfig;
import com.gothamdude.csv.processor.model.ProcessingResult;
import com.gothamdude.csv.processor.service.DataProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CsvProcessorRunner {

    @Autowired
    private DataProcessingService dataProcessingService;

    public void run(ProcessorConfig config) {
        log.info("Starting CSV Processor with config: csvFilePath={}, processName={}, interval={}",
                config.getCsvFilePath(), config.getProcessName(), config.getInterval());

        if (config.getCsvFilePath() == null || config.getProcessName() == null) {
            log.error("Required parameters missing. Use: --processor.csv-file-path=<path> --processor.process-name=<name>");
            throw new IllegalArgumentException("CSV file path and process name are required");
        }

        try {
            ProcessingResult result = dataProcessingService.executeProcessing(
                    config.getProcessName(),
                    config.getCsvFilePath(),
                    config.getInterval() != null ? config.getInterval() : "once"
            );

            if (result.isSuccess()) {
                log.info("CSV processing completed successfully!");
                log.info("Records processed: {}", result.getRecordsProcessed());
                log.info("Execution time: {} ms", result.getExecutionTimeMs());
                log.info("Run ID: {}", result.getRunId());
            } else {
                log.error("CSV processing failed: {}", result.getErrorMessage());
                System.exit(1);
            }

        } catch (Exception e) {
            log.error("CSV processing failed with exception", e);
            System.exit(1);
        }
    }
}