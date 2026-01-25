package io.factorialsystems.auth;

import io.factorialsystems.auth.config.AuthProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(AuthProperties.class)
public class HmsAuthorizationServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(HmsAuthorizationServerApplication.class, args);
    }
}
