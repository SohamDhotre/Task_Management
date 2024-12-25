package org.TaskMgmt.model;

import org.TaskMgmt.repository.AuthTokenRepository;
import org.TaskMgmt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Date;

@Service
public class AuthenticateUser {

    @Autowired
    private AuthTokenRepository authTokenRepository;

    @Autowired
    private UserRepository userRepository;

    public User authenticateUser(String token) throws Exception {
        AuthTokenBody authTokenBody = authTokenRepository.findByAuthToken(token);

        if(authTokenBody==null)
        {
            throw new Exception("Invalid Credentials");
        }

        Date dateNow = new Date();

        if(dateNow.before(authTokenBody.getExpiresAt()))
        {
            Date date = new Date();
            date = Date.from(date.toInstant().atZone(ZoneId.of("IST", ZoneId.SHORT_IDS)).plusMinutes(15).toInstant());
            authTokenBody.setExpiresAt(date);
            authTokenRepository.save(authTokenBody);
            return userRepository.findByUserName(authTokenBody.getUsername());
        }
        throw new Exception("You are timed out of your session, please login again");
    }

}
