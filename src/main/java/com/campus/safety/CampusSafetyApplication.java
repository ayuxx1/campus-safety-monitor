package com.campus.safety;

import com.campus.safety.config.CampusProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(CampusProperties.class)
public class CampusSafetyApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusSafetyApplication.class, args);
    }
}
