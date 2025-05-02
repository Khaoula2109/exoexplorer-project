package com.example.exoExplorer.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * User entity class representing application users.
 * Extends BaseEntity to inherit auditing functionality.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@ToString(exclude = {"favorites", "backupCodes"})
@EqualsAndHashCode(exclude = {"favorites", "backupCodes"}, callSuper = true)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private boolean otpVerified = false;

    private String otpCodeHash;

    private LocalDateTime otpExpiry;

    private boolean darkMode = false;

    private String language = "fr";

    private String firstName;

    private String lastName;

    private boolean isAdmin = false;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TwoFactorBackupCode> backupCodes = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "user_favorites",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "exoplanet_id")
    )
    @JsonIgnore
    private Set<Exoplanet> favorites = new HashSet<>();

    /**
     * Add an exoplanet to favorites.
     *
     * @param exoplanet The exoplanet to add
     * @return true if added, false if already in favorites
     */
    public boolean addFavorite(Exoplanet exoplanet) {
        return favorites.add(exoplanet);
    }

    /**
     * Remove an exoplanet from favorites.
     *
     * @param exoplanet The exoplanet to remove
     * @return true if removed, false if not in favorites
     */
    public boolean removeFavorite(Exoplanet exoplanet) {
        return favorites.remove(exoplanet);
    }

    /**
     * Check if an exoplanet is in favorites.
     *
     * @param exoplanet The exoplanet to check
     * @return true if in favorites, false otherwise
     */
    public boolean hasFavorite(Exoplanet exoplanet) {
        return favorites.contains(exoplanet);
    }

    /**
     * Add a backup code to the user.
     *
     * @param backupCode The backup code to add
     */
    public void addBackupCode(TwoFactorBackupCode backupCode) {
        backupCodes.add(backupCode);
        backupCode.setUser(this);
    }

    /**
     * Remove a backup code from the user.
     *
     * @param backupCode The backup code to remove
     */
    public void removeBackupCode(TwoFactorBackupCode backupCode) {
        backupCodes.remove(backupCode);
        backupCode.setUser(null);
    }
}