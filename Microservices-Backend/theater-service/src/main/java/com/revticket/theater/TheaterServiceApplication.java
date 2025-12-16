package com.revticket.theater;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class TheaterServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TheaterServiceApplication.class, args);
    }
}
