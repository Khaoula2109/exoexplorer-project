package com.example.exoExplorer.repositories;

import com.example.exoExplorer.entities.TwoFactorBackupCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for TwoFactorBackupCode entity.
 * Provides methods for accessing backup code data from the database.
 */
@Repository
public interface TwoFactorBackupCodeRepository extends JpaRepository<TwoFactorBackupCode, Integer> {

    /**
     * Find backup codes for a user.
     *
     * @param userId The user ID
     * @return List of backup codes for the user
     */
    List<TwoFactorBackupCode> findByUserId(Long userId);

    /**
     * Find unused backup codes for a user.
     *
     * @param userId The user ID
     * @return List of unused backup codes for the user
     */
    @Query("SELECT b FROM TwoFactorBackupCode b WHERE b.user.id = :userId AND b.used = false")
    List<TwoFactorBackupCode> findUnusedByUserId(@Param("userId") Long userId);

    /**
     * Find a specific backup code by its hash.
     *
     * @param codeHash The hashed backup code
     * @param userId The user ID
     * @return An Optional containing the backup code if found
     */
    @Query("SELECT b FROM TwoFactorBackupCode b WHERE b.backupCode = :codeHash AND b.user.id = :userId AND b.used = false")
    Optional<TwoFactorBackupCode> findValidBackupCode(@Param("codeHash") String codeHash, @Param("userId") Long userId);

    /**
     * Count unused backup codes for a user.
     *
     * @param userId The user ID
     * @return The number of unused backup codes for the user
     */
    @Query("SELECT COUNT(b) FROM TwoFactorBackupCode b WHERE b.user.id = :userId AND b.used = false")
    Long countUnusedByUserId(@Param("userId") Long userId);
}