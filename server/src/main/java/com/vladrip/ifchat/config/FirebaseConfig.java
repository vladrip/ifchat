package com.vladrip.ifchat.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Bean
    GoogleCredentials googleCredentials() {
        try (FileInputStream firebaseConfIn = new FileInputStream(
                ResourceUtils.getFile("classpath:firebase-config.json")
        )) {
            return GoogleCredentials.fromStream(firebaseConfIn);
        } catch (IOException e) {
            try {
                return GoogleCredentials.getApplicationDefault();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Bean
    FirebaseApp firebaseApp(GoogleCredentials googleCredentials) {
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(googleCredentials)
                .build();
        return FirebaseApp.initializeApp(options);
    }

    @Bean
    FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }

    @Bean
    FirebaseAuth firebaseAuth(FirebaseApp firebaseApp) {
        return FirebaseAuth.getInstance(firebaseApp);
    }

    @Bean
    Gson gson() {
       return new GsonBuilder()
               .registerTypeAdapter(
                       LocalDateTime.class,
                       (JsonSerializer<LocalDateTime>) (localDateTime, type, jsonSerializationContext)
                               -> new JsonPrimitive(localDateTime.toString()))
               .create();
    }
}
