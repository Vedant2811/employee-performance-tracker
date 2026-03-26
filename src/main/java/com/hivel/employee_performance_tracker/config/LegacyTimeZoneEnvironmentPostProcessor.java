package com.hivel.employee_performance_tracker.config;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;

import java.util.TimeZone;

public class LegacyTimeZoneEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String LEGACY_TIMEZONE = "Asia/Calcutta";
    private static final String POSTGRES_COMPATIBLE_TIMEZONE = "Asia/Kolkata";

    @Override
    public void postProcessEnvironment(org.springframework.core.env.ConfigurableEnvironment environment,
                                       SpringApplication application) {
        if (LEGACY_TIMEZONE.equals(TimeZone.getDefault().getID())) {
            TimeZone.setDefault(TimeZone.getTimeZone(POSTGRES_COMPATIBLE_TIMEZONE));
            System.setProperty("user.timezone", POSTGRES_COMPATIBLE_TIMEZONE);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
