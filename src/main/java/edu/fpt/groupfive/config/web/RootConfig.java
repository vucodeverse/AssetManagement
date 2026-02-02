package edu.fpt.groupfive.config.web;

import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@ComponentScan(basePackages = "edu.fpt.groupfive")
@PropertySource("classpath:application.properties")
@PropertySource("classpath:message-login.properties")
public class RootConfig {
    private RootConfig(){
        throw new IllegalStateException("DDaay laf class Utility");
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer =
                new PropertySourcesPlaceholderConfigurer();

        // set file properties sang UTF8
        propertySourcesPlaceholderConfigurer.setFileEncoding("UTF-8");

        return propertySourcesPlaceholderConfigurer;
    }

}