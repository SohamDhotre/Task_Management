package org.TaskMgmt.repository;

import org.TaskMgmt.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

// Repository interface for User entity, extending JpaRepository
@Repository
public interface UserRepository extends JpaRepository<User, Long>, PagingAndSortingRepository<User, Long> {
    // Custom method to find a User by their email
    User findByEmail(String email);
    User findByUserName(String username);
}
