package com.lloyds.onboard.config;


import com.lloyds.onboard.exception.ErrorDetails;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(ConfigTestSetup.class);

    @EnableConfigurationProperties(ErrorConfig.class)
    static class ConfigTestSetup {}


    @Test
    void shouldHandleEmptyErrorDetailsGracefully() {
        ErrorConfig config = new ErrorConfig();
        config.setErrorDetails(null);

        assertThat(config.getErrorDetails()).isNull();
    }

    @Test
    void shouldSupportSettingErrorDetailsManually() {
        ErrorDetails detail = new ErrorDetails();
        detail.setErrorCode("ERR999");
        detail.setErrorMessage("Custom error");

        ErrorConfig config = new ErrorConfig();
        config.setErrorDetails(List.of(detail));

        assertThat(config.getErrorDetails()).hasSize(1);
        assertThat(config.getErrorDetails().get(0).getErrorCode()).isEqualTo("ERR999");
    }
}