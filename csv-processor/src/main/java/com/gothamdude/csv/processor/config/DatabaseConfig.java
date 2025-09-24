package com.gothamdude.csv.processor.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class DatabaseConfig {

    @Value("${postgresql.url}")
    private String postgresUrl;

    @Value("${postgresql.username}")
    private String postgresUsername;

    @Value("${postgresql.password}")
    private String postgresPassword;

    @Value("${postgresql.driver}")
    private String postgresDriver;

    @Bean
    @Primary
    public DataSource dataSource() {
        log.info("Initializing PostgreSQL DataSource with URL: {}", postgresUrl);

        HikariConfig config = new HikariConfig();
        config.setDriverClassName(postgresDriver);
        config.setJdbcUrl(postgresUrl);
        config.setUsername(postgresUsername);
        config.setPassword(postgresPassword);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(20000);
        config.setLeakDetectionThreshold(60000);

        HikariDataSource dataSource = new HikariDataSource(config);

        log.info("PostgreSQL DataSource initialized successfully");
        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        log.info("Creating JdbcTemplate bean");
        return new JdbcTemplate(dataSource);
    }

    @Bean
    private Map<String,String> sparkPostgresProperties(){
        log.info("Setting up Spark PostgreSQL connection properties");
        Map<String, String> props = new HashMap<>();
        props.put("driver", postgresDriver);
        props.put("url", postgresUrl);
        props.put("user", postgresUsername);
        props.put("password", postgresPassword);
        props.put("fetchSize", "1000");
        props.put("batchSize", "1000");
        log.debug("Spark PostgreSQL properties: {}", props);
        return props;

    }


}
