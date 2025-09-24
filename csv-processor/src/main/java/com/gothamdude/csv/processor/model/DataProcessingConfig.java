package com.gothamdude.csv.processor.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

public class DataProcessingConfig {

    private String name;
    private String description;
    private SourceConfig source;
    private TargetConfig target;
    private List<TransformationStep> transformations;
    private Map<String, Object> parameters;


    @Data
    public static class SourceConfig {
        private String type;
        private String path;
        private Map<String, String> options;
    }

    @Data
    public static class TargetConfig {
        private String type;
        private String table;
        private String mode;
        private Map<String, String> options;
    }

    @Data
    public static class TransformationStep {
        private String type;
        private String sql;
        private Map<String, Object> config;
    }

}
