package com.cloudops.dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * CloudOps Dashboard - Main entry point
 *
 * Yeh application ka starting point hai - Spring Boot ka @SpringBootApplication
 * annotation teen kaam karta hai: component scan, auto-configuration, aur
 * configuration properties. Basically ek annotation mein poora setup.
 *
 * @EnableScheduling isliye lagaya hai kyunki service health checks aur
 * metrics collection ke liye scheduled tasks chalate hain baad mein.
 */
@SpringBootApplication
@EnableScheduling
public class CloudOpsApplication {

    public static void main(String[] args) {
        // Bas yahi ek line se poora enterprise application shuru hota hai - Spring ki khoobsurti
        SpringApplication.run(CloudOpsApplication.class, args);
    }
}
