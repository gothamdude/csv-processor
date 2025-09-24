package com.gothamdude.csv.processor.adapter;

import com.gothamdude.csv.processor.model.DataProcessingConfig;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

public interface DataProcessingAdapter {

    /**
     * Load CSV data from file path using spark sql
     */
    Dataset<Row> loadCsvData(String csvFilepath, DataProcessingConfig config);

    /**
     * Apply transformation to the dataset using spark sql
     */
    Dataset<Row> applyTransformation(Dataset<Row> dataset, DataProcessingConfig config);

    /**
     * Save data to postgresql table using spark jdbc writer
     */
    void saveToPostgresql(Dataset<Row> dataset, String tableName);

    /**
     * Get record count efficiently
     */
    long getRecordCount(Dataset<Row> dataset);

    /**
     * Validation data quality
     */
    boolean validateDataQuality(Dataset<Row> dataset, DataProcessingConfig config);

    /**
     * Get adapter type
     */
    String adapterType();



}
