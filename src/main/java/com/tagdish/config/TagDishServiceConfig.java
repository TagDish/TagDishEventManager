package com.tagdish.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import com.google.gson.Gson;

@Configuration
@EnableAsync
@PropertySource("classpath:service.properties")
public class TagDishServiceConfig {

    @Bean
    public Gson foo() {
        return new Gson();
    }
    
	//To resolve ${} in @Value
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyConfig() {
		return new PropertySourcesPlaceholderConfigurer();
	}
}
