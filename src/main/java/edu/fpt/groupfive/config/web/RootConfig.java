package edu.fpt.groupfive.config.web;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ResourceBundleMessageSource;

@Configuration
@ComponentScan(basePackages = "edu.fpt.groupfive")
@PropertySource("classpath:application.properties")
@PropertySource("classpath:message-login.properties")
public class RootConfig {
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer =
                new PropertySourcesPlaceholderConfigurer();

        // set file properties sang UTF8
        propertySourcesPlaceholderConfigurer.setFileEncoding("UTF-8");

        return propertySourcesPlaceholderConfigurer;
    }

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("message-login");
        source.setDefaultEncoding("UTF-8");
        return source;
    }

}