package com.example.exoExplorer.config;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Jasypt password encryption.
 * Enables secure storage of sensitive information in properties files.
 */
@Configuration
public class JasyptConfig {

    /**
     * Bean for Jasypt string encryptor.
     *
     * @return A configured StringEncryptor
     */
    @Bean("jasyptStringEncryptor")
    public StringEncryptor stringEncryptor() {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword("Khaoula2109_");
        encryptor.setAlgorithm("PBEWithMD5AndDES");
        return encryptor;
    }
}