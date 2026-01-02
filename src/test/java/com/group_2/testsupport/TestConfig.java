package com.group_2.testsupport;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;

/**
 * Test configuration that provides a no-op CommandLineRunner to replace
 * the demo bean in Main class that requires services not available
 * in @DataJpaTest.
 */
@TestConfiguration
public class TestConfig {

    @Bean
    public CommandLineRunner demo() {
        return args -> {
            // No-op for tests
        };
    }
}
