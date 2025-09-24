package com.gothamdude.csv.processor.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DataProcessingConfig {

    private String name;
    private String description;
    private Source source;
    private Target target;
    private List<Transformation> transformations;
    private Map<String, Object> parameters;


    @Data
    public static class Source {
        private String type;
        private String path;
        private Map<String, String> options;
    }

    @Data
    public static class Target {
        private String type;
        private String table;
        private String mode;
        private Map<String, String> options;
    }

    @Data
    public static class Transformation {
        private String type;
        private String sql;
        private Map<String, Object> config;
    }

}
