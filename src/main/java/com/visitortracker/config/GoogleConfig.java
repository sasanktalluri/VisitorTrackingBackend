package com.visitortracker.config;

import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.List;

public class GoogleConfig {
    @Bean
    GoogleCredentials googleCredentials(
            @Value("${gcp.credentials.file}") String filePath) throws IOException {
        try (var in = new java.io.FileInputStream(filePath)) {
            return GoogleCredentials.fromStream(in)
                    .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));
        }
    }

}
