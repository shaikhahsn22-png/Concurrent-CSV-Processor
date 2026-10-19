package com.ga.concurrent_csv_processor;

import com.ga.concurrent_csv_processor.Service.EmployeeProcessingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ConcurrentCsvProcessorApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConcurrentCsvProcessorApplication.class, args);
	}
		@Bean
		public CommandLineRunner run(EmployeeProcessingService service) {
			return args -> {
				service.startProcessing();
			};
		}



}
