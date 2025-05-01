package com.example.exoExplorer.repositories;

import com.example.exoExplorer.entities.TwoFactorBackupCode;
import com.example.exoExplorer.entities.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
public class TwoFactorBackupCodeRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TwoFactorBackupCodeRepository backupCodeRepository;

    @Test
    @DisplayName("Should find backup codes by user ID")
    void testFindByUserId() {
        // given
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("hashedpassword");
        userRepository.save(user);

        TwoFactorBackupCode code1 = new TwoFactorBackupCode(null, "codehash1", false, user);
        TwoFactorBackupCode code2 = new TwoFactorBackupCode(null, "codehash2", false, user);
        backupCodeRepository.saveAll(List.of(code1, code2));

        // when
        List<TwoFactorBackupCode> foundCodes = backupCodeRepository.findByUserId(user.getId());

        // then
        assertThat(foundCodes).hasSize(2);
        assertThat(foundCodes.get(0).getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("Should return empty list when user ID has no codes")
    void testFindByUserId_NoResults() {
        List<TwoFactorBackupCode> found = backupCodeRepository.findByUserId(999L);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find unused backup codes by user ID")
    void testFindUnusedByUserId() {
        // given
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("hashedpassword");
        userRepository.save(user);

        TwoFactorBackupCode unusedCode = new TwoFactorBackupCode(null, "codehash1", false, user);
        TwoFactorBackupCode usedCode = new TwoFactorBackupCode(null, "codehash2", true, user);
        backupCodeRepository.saveAll(List.of(unusedCode, usedCode));

        // when
        List<TwoFactorBackupCode> foundCodes = backupCodeRepository.findUnusedByUserId(user.getId());

        // then
        assertThat(foundCodes).hasSize(1);
        assertThat(foundCodes.get(0).getBackupCode()).isEqualTo("codehash1");
        assertThat(foundCodes.get(0).getUsed()).isFalse();
    }
}