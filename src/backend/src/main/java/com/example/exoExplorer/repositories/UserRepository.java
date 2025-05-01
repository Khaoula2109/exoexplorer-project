package com.example.exoExplorer.repositories;

import com.example.exoExplorer.entities.Exoplanet;
import com.example.exoExplorer.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
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

    /**
     * Find users with expired OTP codes.
     *
     * @param currentTime The current time to compare against
     * @return List of users with expired OTP codes
     */
    @Query("SELECT u FROM User u WHERE u.otpExpiry < :currentTime AND u.otpExpiry IS NOT NULL")
    List<User> findByExpiredOtp(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Find users by first or last name containing the given text (case insensitive).
     *
     * @param name The name fragment to search for
     * @param pageable Pagination information
     * @return A Page of Users matching the search criteria
     */
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<User> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    /**
     * Find users who have a specific exoplanet in their favorites.
     *
     * @param exoplanetId The ID of the exoplanet
     * @return List of users who have favorited the exoplanet
     */
    @Query("SELECT u FROM User u JOIN u.favorites f WHERE f.id = :exoplanetId")
    List<User> findByFavoriteExoplanet(@Param("exoplanetId") Integer exoplanetId);

    /**
     * Count users with a specific exoplanet in their favorites.
     *
     * @param exoplanetId The ID of the exoplanet
     * @return The number of users who have favorited the exoplanet
     */
    @Query("SELECT COUNT(u) FROM User u JOIN u.favorites f WHERE f.id = :exoplanetId")
    Long countByFavoriteExoplanet(@Param("exoplanetId") Integer exoplanetId);

    /**
     * Find users who have favorited any of the given exoplanets.
     *
     * @param exoplanets List of exoplanets to check
     * @return List of users who have favorited any of the exoplanets
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.favorites f WHERE f IN :exoplanets")
    List<User> findByFavoritesIn(@Param("exoplanets") List<Exoplanet> exoplanets);
}