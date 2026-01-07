package com.triageagent.s3docs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Bean
    S3Client s3Client() {
        // Uses standard AWS credential + region provider chains (env, profile, EC2/ECS metadata, etc.)
        return S3Client.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(new DefaultAwsRegionProviderChain().getRegion())
                .build();
    }
}
