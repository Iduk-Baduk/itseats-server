package com.idukbaduk.itseats.global.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.springframework.http.ResponseCookie;

public class CookieUtil {

    public static ResponseCookie create(String name, String value, Duration maxAge, boolean httpOnly, boolean secure, String sameSite) {
        return ResponseCookie.from(name, URLEncoder.encode(value, StandardCharsets.UTF_8))
                .httpOnly(httpOnly)
                .secure(secure)
                .path("/")
                .maxAge(maxAge)
                .sameSite(sameSite)
                .build();
    }

    public static String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(cookie -> name.equals(cookie.getName()))
                .map(cookie -> URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8))
                .findFirst()
                .orElse(null);
    }

    public static ResponseCookie deleteCookie(String name, boolean secure, String sameSite) {
        return ResponseCookie.from(name, "")
                .path("/")
                .httpOnly(true)
                .secure(secure)
                .maxAge(0)
                .sameSite(sameSite)
                .build();
    }
}
