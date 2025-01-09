package com.tubesoop.pemilu.uploads;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Resource handler for uploads
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");

        // Resource handler for images
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:images/");

        // Resource handler for documents
        registry.addResourceHandler("/documents/**")
                .addResourceLocations("file:documents/");

        // Resource handler for videos
        registry.addResourceHandler("/videos/**")
                .addResourceLocations("file:videos/");

        // Resource handler for static files
        registry.addResourceHandler("/static/**")
                .addResourceLocations("file:static/");
    }
}
