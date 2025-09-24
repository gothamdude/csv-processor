package com.gothamdude.csv.processor.service;

import com.gothamdude.csv.processor.model.DataProcessingConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ConfigurationService {

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private final Map<String, DataProcessingConfig> configurations = new HashMap<>();

    @PostConstruct
    public void loadConfigurations() {
        try {
            log.info("Loading YAML configurations...");

            // Load YAML configuration
            loadConfig("trade-ingestion");

            log.info("Loaded {} data processing configurations", configurations.size());

        } catch (Exception e) {
            log.error("Failed to load configurations", e);
            throw new RuntimeException("Configuration loading failed", e);
        }
    }

    private void loadConfig(String configName) throws IOException {
        try {
            log.info("Loading configuration: {}", configName);

            ClassPathResource resource = new ClassPathResource("data-configs/" + configName + ".yml");
            DataProcessingConfig config = yamlMapper.readValue(resource.getInputStream(), DataProcessingConfig.class);
            configurations.put(configName, config);

            log.info("Successfully loaded configuration: {} - {}", configName, config.getDescription());

        } catch (IOException e) {
            log.warn("Could not load configuration: {} - {}", configName, e.getMessage());
            throw e;
        }
    }

    public DataProcessingConfig getProcessingConfig(String configName) {
        log.info("Retrieving processing configuration: {}", configName);

        DataProcessingConfig config = configurations.get(configName);
        if (config == null) {
            log.warn("Configuration not found: {}, using default", configName);
            return createDefaultConfig();
        }

        log.info("Retrieved configuration: {} - {}", configName, config.getName());
        return config;
    }

    private DataProcessingConfig createDefaultConfig() {
        log.info("Creating default CSV processing configuration");

        DataProcessingConfig config = new DataProcessingConfig();
        config.setName("default-trade-processing");
        config.setDescription("Default CSV to PostgreSQL trade processing");

        // Default source config for CSV
        DataProcessingConfig.Source source = new DataProcessingConfig.Source();
        source.setType("csv");
        source.setOptions(Map.of(
                "header", "true",
                "inferSchema", "true",
                "timestampFormat", "yyyy-MM-dd HH:mm:ss",
                "dateFormat", "yyyy-MM-dd"
        ));
        config.setSource(source);

        // Default target config for PostgreSQL
        DataProcessingConfig.Target target = new DataProcessingConfig.Target();
        target.setType("jdbc");
        target.setTable("TRANSACTIONS");
        target.setMode("append");
        config.setTarget(target);

        log.info("Default configuration created");

        return config;
    }
}