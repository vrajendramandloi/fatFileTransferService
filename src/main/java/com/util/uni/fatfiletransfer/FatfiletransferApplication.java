package com.util.uni.fatfiletransfer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


@SpringBootConfiguration
@Configuration
@ComponentScan
@EnableAutoConfiguration
public class FatfiletransferApplication {
	private static final Logger logger = LoggerFactory.getLogger(FatfiletransferApplication.class);
	public static void main(String[] args) {
		System.setProperty("sharedFiles", args[0]);
		logger.info("Starting Spring Boot Application ===============================================================");
		SpringApplication.run(FatfiletransferApplication.class, args);
	}

}
