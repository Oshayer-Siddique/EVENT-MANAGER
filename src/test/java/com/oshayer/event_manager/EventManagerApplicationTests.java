package com.oshayer.event_manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class EventManagerApplicationTests {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void generatePasswordHash() {
        String rawPassword = "12345678";
        String hashed = new BCryptPasswordEncoder().encode(rawPassword);
        System.out.println("BCrypt Hash for " + rawPassword + ": " + hashed);
    }

    @Test
    void checkPassword() {
        String storedHash = "$2a$10$zUD4k7Yfixl7Yi7eUg46hOd9.hI2TGnsz417avvFHlRL1dkREoVG2";
        boolean matches = passwordEncoder.matches("12345678", storedHash);
        System.out.println("Password matches? " + matches);
    }

    @Test
    void HelloFunction(){
        System.out.println("Hello World!");
        for(int i =1 ;i<=10;i++){
            System.out.println(i);
        }
    }
}
