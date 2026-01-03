package com.group_2.service.core;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

// BCrypt-based password encryption (automatic salting)
@Service
public class PasswordEncryptionService {

    private final BCryptPasswordEncoder encoder;

    public PasswordEncryptionService() {
        // Strength 10 is the default and provides good security/performance balance
        this.encoder = new BCryptPasswordEncoder(10);
    }

    public String hashPassword(String plainPassword) {
        return encoder.encode(plainPassword);
    }

    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        return encoder.matches(plainPassword, hashedPassword);
    }
}
