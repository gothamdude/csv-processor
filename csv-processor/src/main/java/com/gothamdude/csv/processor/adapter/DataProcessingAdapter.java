package com.gothamdude.csv.processor.adapter;

import com.gothamdude.csv.processor.model.DataProcessingConfig;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

/**
 * Adapter interface that abstracts Spark SQL data processing operations
 */
public interface DataProcessingAdapter {

    /**
     * Load CSV data from file path using Spark SQL
     */
    Dataset<Row> loadCsvData(String csvFilePath, DataProcessingConfig config);

    /**
     * Apply transformations to the dataset using Spark SQL
     */
    Dataset<Row> applyTransformations(Dataset<Row> dataset, DataProcessingConfig config);

    /**
     * Save data to PostgreSQL table using Spark JDBC writer
     */
    void saveToPostgres(Dataset<Row> dataset, String tableName);

    /**
     * Get record count efficiently
     */
    long getRecordCount(Dataset<Row> dataset);

    /**
     * Validate data quality
     */
    boolean validateData(Dataset<Row> dataset, DataProcessingConfig config);

    /**
     * Get adapter type
     */
    String getAdapterType();
}
