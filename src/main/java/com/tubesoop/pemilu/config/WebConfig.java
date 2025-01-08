package com.tubesoop.pemilu.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Menyajikan file dari direktori 'uploads' di root proyek
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
