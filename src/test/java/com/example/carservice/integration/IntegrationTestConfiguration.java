package com.example.carservice.integration;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import com.example.carservice.support.PostgreSqlTestContainerConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@TestConfiguration(proxyBeanMethods = false)
@Import(PostgreSqlTestContainerConfiguration.class)
class IntegrationTestConfiguration {
    private static final Instant FIXED_INSTANT = Instant.parse("2026-10-29T12:00:00Z");

    @Bean
    @Primary
    Clock integrationTestClock() {
        return Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);
    }

}
