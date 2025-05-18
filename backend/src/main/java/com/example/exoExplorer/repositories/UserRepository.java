package com.example.exoExplorer.repositories;

import com.example.exoExplorer.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity.
 * Provides methods for accessing user data from the database.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    /**
     * Find a user by email.
     *
     * @param email The email to search for
     * @return An Optional containing the User if found
     */
    Optional<User> findByEmail(String email);

}