package org.TaskMgmt.controller;

import org.TaskMgmt.model.AuthenticateUser;
import org.TaskMgmt.model.User;
import org.TaskMgmt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("/admin")
public class AdminController {

    @Autowired
    private AuthenticateUser authenticateUser;

    @Autowired
    private UserRepository userRepository;


    @GetMapping("/getAllUsersList")
    public ResponseEntity<List<User>> getAllUsersList(@RequestHeader String authToken) throws Exception {

        User IsAuthonticated = authenticateUser.authenticateUser(authToken);
        if(IsAuthonticated==null)
        {
            throw new Exception("Invalid credentials, please enter valid credentials");
        }

        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }
}
