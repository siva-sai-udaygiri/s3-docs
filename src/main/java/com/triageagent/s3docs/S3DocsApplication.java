package com.triageagent.s3docs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class S3DocsApplication {
    public static void main(String[] args) {
        SpringApplication.run(S3DocsApplication.class, args);
    }
}
