package com.usds.regulations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class RegulationsApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(RegulationsApplication.class);
    
    public static void main(String[] args) {
        logger.info("Starting USDS Regulations Analysis Backend Application");
        SpringApplication.run(RegulationsApplication.class, args);
        logger.info("USDS Regulations Analysis Backend Application started successfully");
    }
    
    /**
     * Configure RestTemplate bean for HTTP client operations
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}