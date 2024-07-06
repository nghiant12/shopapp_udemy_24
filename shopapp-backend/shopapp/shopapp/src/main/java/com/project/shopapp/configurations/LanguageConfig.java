package com.project.shopapp.configurations;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@Configuration
public class LanguageConfig {
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        // Ensure the path is correct and the messages file is located there
        messageSource.setBasename("classpath:i18n/messages");
        // Setting the default encoding to UTF-8
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}
