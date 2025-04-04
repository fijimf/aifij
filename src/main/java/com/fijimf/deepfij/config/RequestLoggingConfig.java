package com.fijimf.deepfij.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class RequestLoggingConfig {

    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeClientInfo(true); // Logs client info (IP address, hostname)
        filter.setIncludeQueryString(true); // Logs query strings (e.g., ?param=value)
        filter.setIncludeHeaders(true); // Logs headers (be careful with sensitive data in prod)
        filter.setIncludePayload(true); // Logs request payload (body content)
        filter.setMaxPayloadLength(10000); // Limits the size of payload logged; adjust if needed
        return filter;
    }
}