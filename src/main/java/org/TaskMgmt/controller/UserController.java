package org.TaskMgmt.controller;

import jakarta.validation.Valid;
import org.TaskMgmt.model.User;
import org.TaskMgmt.repository.UserRepository;
import org.TaskMgmt.service.CustomUserDetailsService;
import org.TaskMgmt.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private static final long TIMEOUT_DURATION = 15;
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    // Constructor injection
    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, AuthenticationManager authenticationManager, CustomUserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    // Register a new user
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody User user) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            logger.error("Email already registered.");
            return ResponseEntity.badRequest().body("Email already registered.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        logger.info("User registered with email: {}", user.getEmail());
        return ResponseEntity.ok("User registered successfully with ID: " + savedUser.getId());
    }

    // Login user (check email and password)
    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@Valid @RequestBody User user) {

        User existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser == null) {
            return handleInvalidEmail();
        }
        // Check if the user is locked out
        else if (existingUser.getLockoutTime() != null) {
            if (existingUser.getLockoutTime().isAfter(LocalDateTime.now())) {
                long minutesUntilUnlock = ChronoUnit.MINUTES.between(LocalDateTime.now(), existingUser.getLockoutTime());
                return ResponseEntity.status(403).body("Account is locked. Please try again in " + minutesUntilUnlock + " minutes.");
            } else {
                // Reset failed login attempts after lockout period
                existingUser.setFailedLoginAttempts(0);
                existingUser.setLockoutTime(null);
                userRepository.save(existingUser);
            }
        }

        if (!passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
            logger.error("Invalid password");
            // Increment the failed login attempts
            if (existingUser != null) {
                existingUser.setFailedLoginAttempts(existingUser.getFailedLoginAttempts() + 1);

                // Lock account if the maximum number of attempts is exceeded
                if (existingUser.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
                    existingUser.setLockoutTime(LocalDateTime.now().plusMinutes(TIMEOUT_DURATION));
                    userRepository.save(existingUser); // Save changes
                    logger.info("Account is locked due to multiple failed attempts. Please try again later.");
                    return ResponseEntity.status(403).body("Account is locked due to multiple failed attempts. Please try again later.");
                }
                else {
                    int attempts_left = Math.abs(MAX_FAILED_ATTEMPTS - existingUser.getFailedLoginAttempts());
                    String loggingString = "Invalid Password , you have "+ Integer.toString(attempts_left)+ "attempts more, upon failed login more than "+ Integer.toString(MAX_FAILED_ATTEMPTS)+"attempts will results in account lock for "+ Long.toString(TIMEOUT_DURATION)+" minutes";
                    logger.info(loggingString);
                    return ResponseEntity.status(403).body(loggingString);
                }
            }
            userRepository.save(existingUser); // Save failed attempts
            return ResponseEntity.status(401).body("Invalid email or password");
        }

        // Reset failed attempts on successful login
        existingUser.setFailedLoginAttempts(0);
        existingUser.setLockoutTime(null);
        userRepository.save(existingUser); // Save changes

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword())
            );
        } catch (Exception e) {
            logger.error("Invalid credentials");
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getEmail());
        logger.info("User with id : {}, logged in successfully ", existingUser.getId());
        return ResponseEntity.ok(token);
    }

    private ResponseEntity<String> handleInvalidEmail() {
        logger.error("Invalid email");
        return ResponseEntity.status(401).body("No account found with that email.");
    }

    // Get all users
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Update user details by ID
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        String loggedInUserEmail = getLoggedInUserEmail();

        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = optionalUser.get();

        if (!user.getEmail().equals(loggedInUserEmail)) {
            return ResponseEntity.status(403).body(null); // Forbidden
        }

        user.setUserName(userDetails.getUserName());
        user.setPassword(passwordEncoder.encode(userDetails.getPassword()));

        User updatedUser = userRepository.save(user);
        return ResponseEntity.ok(updatedUser);
    }

    private static String getLoggedInUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) authentication.getPrincipal()).getUsername(); // Assuming username is email
        }
        return null;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Test endpoint is working!");
    }
}
