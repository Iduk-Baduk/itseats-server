package com.idukbaduk.itseats.member.util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PasswordUtil {

    private static class PasswordEncoderInstanceHolder {
        private static final PasswordEncoder encoder = new BCryptPasswordEncoder();
    }

    public static String hashing(String rawPassword) {
        return PasswordEncoderInstanceHolder.encoder.encode(rawPassword);
    }

    public static boolean matches(String rawPassword, String hashedPassword) {
        return PasswordEncoderInstanceHolder.encoder.matches(rawPassword, hashedPassword);
    }

}
