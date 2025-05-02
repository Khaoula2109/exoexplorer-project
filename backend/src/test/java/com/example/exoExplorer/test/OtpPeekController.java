package com.example.exoExplorer.test;

import com.example.exoExplorer.config.TestMailConfig;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** Exposé seulement pendant les tests de perf pour récupérer le dernier OTP. */
@Profile("test")
@RestController
@RequestMapping("/api/test")
public class OtpPeekController {

    @GetMapping("/last-otp")
    public Map<String, String> lastOtp() {
        return Map.of("otp", TestMailConfig.lastOtp == null ? "" : TestMailConfig.lastOtp);
    }
}
