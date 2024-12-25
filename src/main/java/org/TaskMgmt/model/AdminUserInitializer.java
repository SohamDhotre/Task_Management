package org.TaskMgmt.model;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.TaskMgmt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Collections;

@Component
@Slf4j
public class AdminUserInitializer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void initializeAdminUser() {

        String adminEmail = "admin@todo.com";
        String adminPassword = "admin";

        // Check if admin user exists
        if (userRepository.findByEmail(adminEmail) == null) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setUserName("Admin");

            Role adminRole = new Role("ADMIN");
            admin.setRoles(Collections.singleton(adminRole)); // Assign the "ADMIN" role

            userRepository.save(admin);

            log.info("Admin user created:");
            log.info("Email: {}", adminEmail);
            log.info("Password: {}", adminPassword);
        } else {
            log.info("Admin user exists.");
        }
    }


}
