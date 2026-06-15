package com.devdoyen.nemologic.security;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AdminSessionManager {
    private final Map<String, String> tokenToUserMap = new ConcurrentHashMap<>();

    public void registerToken(String token, String username) {
        tokenToUserMap.put(token, username);
    }

    public String getUsernameForToken(String token) {
        return tokenToUserMap.get(token);
    }

    public void removeToken(String token) {
        tokenToUserMap.remove(token);
    }

    public void clear() {
        tokenToUserMap.clear();
    }
}
