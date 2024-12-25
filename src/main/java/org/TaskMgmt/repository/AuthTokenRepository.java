package org.TaskMgmt.repository;

import org.TaskMgmt.model.AuthTokenBody;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthTokenRepository extends JpaRepository<AuthTokenBody, String> {
    AuthTokenBody findByUsername(String username);
    AuthTokenBody findByAuthToken(String authToken);
    void deleteByAuthToken(String authToken);
}
