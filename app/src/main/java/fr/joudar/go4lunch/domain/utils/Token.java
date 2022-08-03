package fr.joudar.go4lunch.domain.utils;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

public class Token {
    String token;

    @Inject
    public Token() {
    }

    private void renewToken() {
        token = UUID.randomUUID().toString();
        setupTokenExpiration();
    }

    private void setupTokenExpiration() {
        Executors.newSingleThreadScheduledExecutor().schedule(this::clearToken, 2, TimeUnit.MINUTES);
    }

    private void clearToken() {
        token = null;
    }

    public String getToken() {
        if (token == null)
            renewToken();
        else
            setupTokenExpiration();
        return token;
    }

    public String getAndClearToken() {
        if (token == null)
            return "null_token";
        final String tokenCopy = token;
        clearToken();
        return tokenCopy;
    }
}
