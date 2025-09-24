package com.gothamdude.csv.processor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.gothamdude.csv.processor.config.ProcessorConfig;
import com.gothamdude.csv.processor.runner.CsvProcessorRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableConfigurationProperties
@Slf4j
public class CsvProcessorApplication {

	public static void main(String[] args) {
		log.info("Starting CSV Processor Application");

		try (ConfigurableApplicationContext context = SpringApplication.run(CsvProcessorApplication.class, args)) {
			// Get the processor configuration
			ProcessorConfig config = context.getBean(ProcessorConfig.class);

			// Get the CSV processor runner
			CsvProcessorRunner runner = context.getBean(CsvProcessorRunner.class);

			// Execute the CSV processing
			runner.run(config);

		} catch (Exception e) {
			log.error("Application failed to start or execute", e);
			System.exit(1);
		}

		log.info("CSV Processor Application completed");
	}
}
