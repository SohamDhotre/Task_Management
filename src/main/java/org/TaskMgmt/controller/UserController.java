package org.TaskMgmt.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.TaskMgmt.model.AuthTokenBody;
import org.TaskMgmt.model.AuthenticateUser;
import org.TaskMgmt.model.Role;
import org.TaskMgmt.model.User;
import org.TaskMgmt.repository.AuthTokenRepository;
import org.TaskMgmt.repository.UserRepository;
import org.TaskMgmt.service.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private AuthTokenRepository authTokenRepository;

    @Autowired
    private AuthenticateUser authenticateUser;

    // Constructor injection
    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    //should be removed from user controller , only admin should be accessible ot this functionality
    @GetMapping("/getUsers")
    public Page<User> getUsers(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size, @RequestHeader("authToken") String authToken) throws Exception {
        User user = authenticateUser.authenticateUser(authToken);
        if(user==null)
        {
            throw new Exception("Invalid credentials, please enter valid credentials");
        }
        return userRepository.findAll(PageRequest.of(page, size));
    }

    // Register a new user
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody User user) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            log.error("Email already registered.");
            return ResponseEntity.badRequest().body("Email already registered.");
        }
        if (userRepository.findByUserName(user.getUserName()) != null) {
            log.error("User name already registered.");
            return ResponseEntity.badRequest().body("User name already registered.");
        }
        Role userRole = new Role("USER");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Collections.singleton(userRole)); // Assign the "USER" role

        User savedUser = userRepository.save(user);
        log.info("User registered with email: {}", user.getEmail());
        return ResponseEntity.ok("User registered successfully with ID: " + savedUser.getUserId());
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
            log.error("Invalid password");
            // Increment the failed login attempts
            if (existingUser != null) {
                existingUser.setFailedLoginAttempts(existingUser.getFailedLoginAttempts() + 1);

                // Lock account if the maximum number of attempts is exceeded
                if (existingUser.getFailedLoginAttempts() >= Constants.MAX_FAILED_ATTEMPTS) {
                    existingUser.setLockoutTime(LocalDateTime.now().plusMinutes(Constants.TIMEOUT_DURATION));
                    userRepository.save(existingUser); // Save changes
                    log.info("Account is locked due to multiple failed attempts. Please try again later.");
                    return ResponseEntity.status(403).body("Account is locked due to multiple failed attempts. Please try again later.");
                }
                else {
                    int attempts_left = Math.abs(Constants.MAX_FAILED_ATTEMPTS - existingUser.getFailedLoginAttempts());
                    String loggingString = "Invalid Password , you have "+ Integer.toString(attempts_left)+ "attempts more, upon failed login more than "+ Integer.toString(Constants.MAX_FAILED_ATTEMPTS)+"attempts will results in account lock for "+ Long.toString(Constants.TIMEOUT_DURATION)+" minutes";
                    log.info(loggingString);
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
            log.error("Invalid credentials");
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        String body = "User with id: "+existingUser.getUserId()+", logged in successfully";
        log.info(body);

        String authToken = addAuthTokenBody(existingUser);

        HttpHeaders headers = new HttpHeaders();
        headers.add("authToken", authToken);

        return ResponseEntity
                .status(HttpStatus.OK) // Set HTTP status
                .headers(headers)      // Add headers
                .body(body);           // Set body content
    }

    public String addAuthTokenBody(User user){

        AuthTokenBody authTokenBody = authTokenRepository.findByUsername(user.getUserName());
        String authToken = generateString();
        Date date = new Date();
        date = Date.from(date.toInstant().atZone(ZoneId.of("IST", ZoneId.SHORT_IDS)).plusMinutes(15).toInstant());


        if(authTokenBody==null)
        {
            AuthTokenBody newAuthTokenBody = new AuthTokenBody();
            newAuthTokenBody.setUsername(user.getUserName());
            newAuthTokenBody.setExpiresAt(date);
            newAuthTokenBody.setAuthToken(authToken);
            authTokenRepository.save(newAuthTokenBody);

        }
        else
        {
            authTokenBody.setAuthToken(authToken);
            authTokenBody.setExpiresAt(date);
            authTokenRepository.save(authTokenBody);
        }
        return authToken;
    }

    public String generateString() {
        String uuid = UUID.randomUUID().toString();
        return  uuid.replace("-", "");
    }

    private ResponseEntity<String> handleInvalidEmail() {
        log.error("Invalid email");
        return ResponseEntity.status(401).body("No account found with that email.");
    }

    // Update user details by ID
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails, @RequestHeader String authToken) throws Exception {

        User IsAuthonticated = authenticateUser.authenticateUser(authToken);
        if(IsAuthonticated==null)
        {
            throw new Exception("Invalid credentials, please enter valid credentials");
        }

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
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, @RequestHeader String authToken) throws Exception {
        User IsAuthonticated = authenticateUser.authenticateUser(authToken);
        if(IsAuthonticated==null)
        {
            throw new Exception("Invalid credentials, please enter valid credentials");
        }
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        if(IsAuthonticated == userRepository.findById(id).get())
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Test endpoint is working!");
    }

    @GetMapping("/logoutUser")
    public ResponseEntity<String> logout(String authToken) throws Exception {
        User IsAuthonticated = authenticateUser.authenticateUser(authToken);
        if(IsAuthonticated==null)
        {
            throw new Exception("Invalid credentials, please enter valid credentials");
        }
        authTokenRepository.deleteByAuthToken(authToken);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("Logged out successfully");
    }

}
