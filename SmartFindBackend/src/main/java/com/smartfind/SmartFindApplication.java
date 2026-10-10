package com.smartfind;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Entry Point for SmartFind - AI-Based Smart Lost & Found System.
 * Developed with Spring Boot and Spring Data JPA.
 */
@SpringBootApplication
public class SmartFindApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartFindApplication.class, args);
        System.out.println("====================================================================");
        System.out.println(" SmartFind Backend is running successfully at http://localhost:8083 ");
        System.out.println(" Database: MySQL (smartfind_db) | Port: 8083                        ");
        System.out.println(" Default Admin: admin@smartfind.com / admin123                     ");
        System.out.println(" Default Student: student@smartfind.com / student123               ");
        System.out.println("====================================================================");
    }
}
