package com.idukbaduk.itseats.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class ClientConfig {

    private static final String AUTH_HEADER_PREFIX = "Basic ";
    private static final String BASIC_DELIMITER = ":";
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 30000;

    @Value("${payment.toss.key}")
    private String secretKey;

    @Value("${payment.toss.url}")
    private String tossBaseUrl;

    @Bean
    public RestClient tossRestClient() {
        return RestClient.builder()
//                .requestFactory(getRequestFactory())
                .baseUrl(tossBaseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, AUTH_HEADER_PREFIX + getEncodedAuth())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    // TODO : Factory에 관해 고민해볼것
//    private ClientHttpRequestFactory getRequestFactory() {
//        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
//        factory.setConnectTimeout(CONNECT_TIMEOUT);
//        factory.setReadTimeout(READ_TIMEOUT);
//
//        return factory;
//    }

    private String getEncodedAuth() {
        return Base64.getEncoder().encodeToString((secretKey + BASIC_DELIMITER).getBytes(StandardCharsets.UTF_8));
    }
}
