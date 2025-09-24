package com.gothamdude.csv.processor.template;

import com.gothamdude.csv.processor.model.DataProcessingConfig;
import com.gothamdude.csv.processor.service.ConfigurationService;
import com.gothamdude.csv.processor.template.DataProcessingTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Concrete implementation for CSV to PostgreSQL processing using Spark SQL
 */
@Component
@Slf4j
public class CsvToPostgreSqlProcessor extends DataProcessingTemplate {

    @Autowired
    private ConfigurationService configurationService;

    @Override
    protected DataProcessingConfig loadConfiguration(String processName) {
        log.info("Loading YAML configuration for process: {}", processName);
        DataProcessingConfig config = configurationService.getProcessingConfig(processName);
        log.info("Configuration loaded successfully for process: {}", processName);
        return config;
    }

    @Override
    protected Dataset<Row> loadCsvData(String csvFilePath, DataProcessingConfig config) {
        log.info("Loading CSV data using Spark SQL from: {}", csvFilePath);
        Dataset<Row> dataset = dataProcessingAdapter.loadCsvData(csvFilePath, config);
        log.info("CSV data loaded successfully from: {}", csvFilePath);
        return dataset;
    }

    @Override
    protected Dataset<Row> transformData(Dataset<Row> dataset, DataProcessingConfig config) {
        log.info("Applying Spark SQL transformations");
        Dataset<Row> transformedDataset = dataProcessingAdapter.applyTransformations(dataset, config);
        log.info("Spark SQL transformations applied successfully");
        return transformedDataset;
    }

    @Override
    protected void saveToPostgres(Dataset<Row> dataset, DataProcessingConfig config) {
        String tableName = config.getTarget().getTable();
        log.info("Saving data to PostgreSQL table using Spark JDBC: {}", tableName);
        dataProcessingAdapter.saveToPostgres(dataset, tableName);
        log.info("Data saved successfully to PostgreSQL table: {}", tableName);
    }

    @Override
    protected void validateLoadedData(Dataset<Row> dataset, DataProcessingConfig config) {
        super.validateLoadedData(dataset, config);

        // Additional validation specific to trade data
        long recordCount = dataset.count();
        log.info("Loaded {} trade records from CSV", recordCount);

        if (recordCount == 0) {
            throw new IllegalStateException("CSV file is empty or could not be processed");
        }

        // Show sample data for verification
        log.info("Sample trade data (first 5 records):");
        dataset.show(5, false);
    }
}