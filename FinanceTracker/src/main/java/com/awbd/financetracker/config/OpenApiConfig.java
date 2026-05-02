package com.awbd.financetracker.config;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        return openApi -> openApi.getInfo()
                .title("Personal Finance & Subscription Tracker API")
                .version("1.0.0")
                .description("REST API for managing personal finances and subscription tracking.");
    }
}