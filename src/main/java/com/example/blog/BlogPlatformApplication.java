package com.example.blog;

import com.example.blog.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableJpaAuditing
@EnableConfigurationProperties(AppProperties.class)
public class BlogPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogPlatformApplication.class, args);
    }
}
