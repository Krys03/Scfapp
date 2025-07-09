package com.supplychain.scfapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.supplychain.scfapp") // 🔥 Forçage explicite du scan
public class ScfAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScfAppApplication.class, args);
    }
}
