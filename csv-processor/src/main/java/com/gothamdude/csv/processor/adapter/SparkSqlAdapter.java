package com.gothamdude.csv.processor.adapter;

import com.gothamdude.csv.processor.exception.DataProcessingException;
import com.gothamdude.csv.processor.model.DataProcessingConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class SparkSqlAdapter implements DataProcessingAdapter {

    @Autowired
    private SparkSession sparkSession;

    @Autowired
    private Map<String, String> sparkPostgresProperties;

    @Override
    public Dataset<Row> loadCsvData(String csvFilePath, DataProcessingConfig config) {
        log.info("Loading CSV data from: {}", csvFilePath);

        try {
            var reader = sparkSession.read().format("csv");

            // Apply CSV options from config
            if (config != null && config.getSource() != null && config.getSource().getOptions() != null) {
                log.info("Applying CSV options from configuration");
                config.getSource().getOptions().forEach((key, value) -> {
                    log.debug("CSV option: {}={}", key, value);
                    reader.option(key, value);
                });
            } else {
                log.info("Using default CSV options for trade data");
                reader.option("header", "true")
                        .option("inferSchema", "true")
                        .option("timestampFormat", "yyyy-MM-dd HH:mm:ss")
                        .option("dateFormat", "yyyy-MM-dd");
            }

            Dataset<Row> dataset = reader.load(csvFilePath);

            // Log schema for debugging
            log.info("CSV Schema loaded:");
            dataset.printSchema();

            long recordCount = dataset.count();
            log.info("Loaded {} records from CSV file: {}", recordCount, csvFilePath);

            return dataset;

        } catch (Exception e) {
            log.error("Error loading CSV data from: {}", csvFilePath, e);
            throw new DataProcessingException("Failed to load CSV data", e);
        }
    }

    @Override
    public Dataset<Row> applyTransformations(Dataset<Row> dataset, DataProcessingConfig config) {
        if (config == null || config.getTransformations() == null || config.getTransformations().isEmpty()) {
            log.info("No transformations to apply, returning original dataset");
            return dataset;
        }

        log.info("Applying {} transformations to dataset", config.getTransformations().size());

        Dataset<Row> result = dataset;
        int stepNumber = 1;

        for (DataProcessingConfig.Transformation step : config.getTransformations()) {
            log.info("Applying transformation step {}/{}: {}", stepNumber, config.getTransformations().size(), step.getType());
            result = applyTransformation(result, step, config.getParameters());

            long recordCount = result.count();
            log.info("After transformation step {}, dataset has {} records", stepNumber, recordCount);

            stepNumber++;
        }

        log.info("Applied all transformations successfully");

        return result;
    }

    private Dataset<Row> applyTransformation(Dataset<Row> dataset,
                                             DataProcessingConfig.Transformation step,
                                             Map<String, Object> parameters) {
        switch (step.getType().toLowerCase()) {
            case "sql":
                return applySqlTransformation(dataset, step.getSql(), parameters);
            case "filter":
                String condition = (String) step.getConfig().get("condition");
                log.info("Applying filter condition: {}", condition);
                return dataset.filter(replaceParameters(condition, parameters));
            default:
                log.warn("Unknown transformation type: {}, skipping", step.getType());
                return dataset;
        }
    }

    private Dataset<Row> applySqlTransformation(Dataset<Row> dataset, String sql, Map<String, Object> parameters) {
        String tempViewName = "temp_trade_data_" + System.currentTimeMillis();
        dataset.createOrReplaceTempView(tempViewName);

        String processedSql = replaceParameters(sql, parameters);
        processedSql = processedSql.replace("${table}", tempViewName);

        log.info("Executing SQL transformation with temp view: {}", tempViewName);
        log.debug("SQL query: {}", processedSql);

        Dataset<Row> result = sparkSession.sql(processedSql);

        log.info("SQL transformation completed");

        return result;
    }

    @Override
    public void saveToPostgres(Dataset<Row> dataset, String tableName) {
        long recordCount = dataset.count();
        log.info("Saving {} records to PostgreSQL table: {}", recordCount, tableName);

        try {
            // Log the final schema before saving
            log.info("Final dataset schema before saving to {}:", tableName);
            dataset.printSchema();

            dataset.write()
                    .format("jdbc")
                    .options(sparkPostgresProperties)
                    .option("dbtable", tableName)
                    .mode("append") // Always append to existing table
                    .save();

            log.info("Successfully saved {} records to PostgreSQL table: {}", recordCount, tableName);

        } catch (Exception e) {
            log.error("Error saving data to PostgreSQL table: {}", tableName, e);
            throw new DataProcessingException("Failed to save data to PostgreSQL", e);
        }
    }

    @Override
    public long getRecordCount(Dataset<Row> dataset) {
        long count = dataset.count();
        log.debug("Dataset record count: {}", count);
        return count;
    }

    @Override
    public boolean validateData(Dataset<Row> dataset, DataProcessingConfig config) {
        log.info("Validating dataset");

        try {
            // Check if dataset has data
            long count = dataset.count();
            if (count == 0) {
                log.warn("Dataset validation failed: Dataset is empty");
                return false;
            }

            log.info("Dataset contains {} records", count);

            // Validate required columns for transaction data
            String[] actualColumns = dataset.columns();
            String[] requiredColumns = {
                    "TRANSACTION_ID", "TICKER", "QUANTITY", "DIRECTION",
                    "EMPLOYEE_ID", "TRANSACTION_DATE", "TRANSACTION_TIMESTAMP", "TRANSACTION_STATUS"
            };

            log.debug("Dataset columns: {}", String.join(", ", actualColumns));

            for (String required : requiredColumns) {
                boolean found = false;
                for (String actual : actualColumns) {
                    if (actual.equalsIgnoreCase(required)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    log.error("Dataset validation failed: Required column not found: {}", required);
                    return false;
                }
            }

            log.info("Dataset validation passed. Records: {}, Columns: {}", count, actualColumns.length);
            return true;

        } catch (Exception e) {
            log.error("Dataset validation failed with exception", e);
            return false;
        }
    }

    private String replaceParameters(String text, Map<String, Object> parameters) {
        if (parameters == null || text == null) {
            return text;
        }

        String result = text;
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            String value = String.valueOf(entry.getValue());
            result = result.replace(placeholder, value);
            log.debug("Replaced parameter {} with value {}", placeholder, value);
        }
        return result;
    }

    @Override
    public String getAdapterType() {
        return "spark-sql-postgres";
    }
}