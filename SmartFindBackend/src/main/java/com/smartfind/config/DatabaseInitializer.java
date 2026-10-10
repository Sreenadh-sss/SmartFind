package com.smartfind.config;

import com.smartfind.entity.ItemReport;
import com.smartfind.entity.ItemStatus;
import com.smartfind.entity.ReportType;
import com.smartfind.entity.User;
import com.smartfind.repository.ItemReportRepository;
import com.smartfind.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Seeds initial demo data (Default Admin, Student, and sample Lost/Found items)
 * if the database is newly created, allowing immediate evaluation.
 */
@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final UserRepository userRepository;
    private final ItemReportRepository itemReportRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseInitializer(UserRepository userRepository,
                               ItemReportRepository itemReportRepository,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.itemReportRepository = itemReportRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 1. Seed Default Admin User
        User admin = userRepository.findByEmail("admin@smartfind.com").orElseGet(() -> {
            User newAdmin = new User();
            newAdmin.setName("System Administrator");
            newAdmin.setEmail("admin@smartfind.com");
            newAdmin.setPassword(passwordEncoder.encode("admin123"));
            newAdmin.setRole("ADMIN");
            logger.info("Created default administrator account: admin@smartfind.com / admin123");
            return userRepository.save(newAdmin);
        });

        // 2. Seed Default Student User
        User student = userRepository.findByEmail("student@smartfind.com").orElseGet(() -> {
            User newStudent = new User();
            newStudent.setName("Alex Morgan");
            newStudent.setEmail("student@smartfind.com");
            newStudent.setPassword(passwordEncoder.encode("student123"));
            newStudent.setRole("USER");
            logger.info("Created default demo student account: student@smartfind.com / student123");
            return userRepository.save(newStudent);
        });

        // 3. Seed Realistic Sample Items if empty
        if (itemReportRepository.count() == 0) {
            LocalDate today = LocalDate.now();

            // Sample Lost Item 1 (Phone)
            ItemReport lostPhone = new ItemReport();
            lostPhone.setItemName("Apple iPhone 13 Black");
            lostPhone.setDescription("Black Apple iPhone 13 with matte black silicone case, slight scratch on corner. Lost near the central reading hall.");
            lostPhone.setLocation("Central Library");
            lostPhone.setItemDate(today.minusDays(2));
            lostPhone.setReportType(ReportType.LOST);
            lostPhone.setStatus(ItemStatus.ACTIVE);
            lostPhone.setUser(student);
            itemReportRepository.save(lostPhone);

            // Sample Found Item 1 (Matches Lost Phone)
            ItemReport foundPhone = new ItemReport();
            foundPhone.setItemName("Black iPhone");
            foundPhone.setDescription("Found an Apple black iPhone with dark silicone case on the library study desk.");
            foundPhone.setLocation("Central Library 2nd Floor");
            foundPhone.setItemDate(today.minusDays(1));
            foundPhone.setReportType(ReportType.FOUND);
            foundPhone.setStatus(ItemStatus.ACTIVE);
            foundPhone.setUser(admin);
            itemReportRepository.save(foundPhone);

            // Sample Lost Item 2 (Backpack)
            ItemReport lostBag = new ItemReport();
            lostBag.setItemName("Navy Blue Laptop Backpack");
            lostBag.setDescription("Navy blue Dell backpack containing engineering notebook, blue pen drive and wireless mouse.");
            lostBag.setLocation("Campus Cafeteria");
            lostBag.setItemDate(today.minusDays(3));
            lostBag.setReportType(ReportType.LOST);
            lostBag.setStatus(ItemStatus.ACTIVE);
            lostBag.setUser(student);
            itemReportRepository.save(lostBag);

            // Sample Found Item 2 (Matches Lost Bag)
            ItemReport foundBag = new ItemReport();
            foundBag.setItemName("Blue Dell Backpack");
            foundBag.setDescription("Navy blue laptop bag found near lunch counter. Contains engineering notes and mouse.");
            foundBag.setLocation("Main Cafeteria Counter");
            foundBag.setItemDate(today.minusDays(2));
            foundBag.setReportType(ReportType.FOUND);
            foundBag.setStatus(ItemStatus.ACTIVE);
            foundBag.setUser(admin);
            itemReportRepository.save(foundBag);

            // Sample Found Item 3 (Unrelated item)
            ItemReport foundCalc = new ItemReport();
            foundCalc.setItemName("Casio Scientific Calculator FX-991EX");
            foundCalc.setDescription("Black scientific calculator left on bench in Chemistry laboratory room 204.");
            foundCalc.setLocation("Chemistry Lab 204");
            foundCalc.setItemDate(today.minusDays(4));
            foundCalc.setReportType(ReportType.FOUND);
            foundCalc.setStatus(ItemStatus.ACTIVE);
            foundCalc.setUser(admin);
            itemReportRepository.save(foundCalc);

            logger.info("Successfully seeded demo lost and found items for testing matching algorithm.");
        }
    }
}
