package com.example.AwsResourceExplorer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AwsResourceExplorerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AwsResourceExplorerApplication.class, args);
	}

}
