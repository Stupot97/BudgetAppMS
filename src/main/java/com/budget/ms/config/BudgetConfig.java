package com.budget.ms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@ComponentScan(basePackages = "com.budget.ms")
public class BudgetConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}