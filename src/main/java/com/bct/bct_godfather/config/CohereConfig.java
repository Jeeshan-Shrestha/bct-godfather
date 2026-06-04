package com.bct.bct_godfather.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class CohereConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
