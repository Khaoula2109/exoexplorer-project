package com.example.exoExplorer.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity for backup codes used in two-factor authentication.
 */
@Entity
@Table(name = "TwoFactorBackupCode")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorBackupCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "backup_id")
    private Integer backupId;

    @Column(name = "backup_code", nullable = false)
    private String backupCode;

    @Column(name = "used")
    private Boolean used = false;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}