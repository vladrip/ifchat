package com.vladrip.ifchat.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest()
                        .permitAll())
                .oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }

    @PostConstruct
    public void generateTestToken() {
        JsonObject json = new JsonObject();
        json.addProperty("email", "admin@ifchat.test");
        json.addProperty("password", "IFT3stB@ckend");
        json.addProperty("returnSecureToken", true);

        try (HttpClient httpClient = HttpClient.newHttpClient()) {
            HttpResponse<String> response = httpClient.send(HttpRequest.newBuilder()
                            .uri(new URI("https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=AIzaSyBaMqofrRaNzslkaTZYP8_JDE5jKKGFmeA"))
                            .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                            .header("content-type", "application/json")
                            .build(),
                    HttpResponse.BodyHandlers.ofString());
            log.info("Test token: {}", JsonParser.parseString(response.body()).getAsJsonObject().get("idToken").getAsString());
        } catch (IOException | InterruptedException | URISyntaxException e) {
            log.error("Error while generating test token: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
