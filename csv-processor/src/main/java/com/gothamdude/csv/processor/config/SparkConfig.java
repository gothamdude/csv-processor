package com.gothamdude.csv.processor.config;


import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.SparkSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Configuration
public class SparkConfig {

    @Value("${spark.app.name:CsvToPostgreSqlProcessor}")
    private String appName;

    @Value("${spark.master:local[*]}")
    private String master;

    @Bean
    public SparkSession sparkSession(){
        log.info("Creating Spark Session");
        SparkSession session = SparkSession.builder()
                .appName(appName)
                .master(master)
                .config("spark.sql.adaptive.enabled", "true")
                .config("spark.sql.adaptive.coalescePartitions.enabled", "true")
                .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
                .config("spark.sql.execution.arrow.pyspark.enabled", "true")
                .getOrCreate();

        session.sparkContext().setLogLevel("WARN");

        log.info("Spark Session initialized successfully");
        return session;
    }
}



