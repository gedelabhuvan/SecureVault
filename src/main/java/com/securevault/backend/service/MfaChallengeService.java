package com.securevault.backend.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MfaChallengeService {

    private static final int CHALLENGE_EXPIRY_MINUTES = 5;

    private final Map<String, ChallengeData> challenges =
            new ConcurrentHashMap<>();

    public String createChallenge(String email) {

        String challengeId = UUID.randomUUID().toString();

        challenges.put(
                challengeId,
                new ChallengeData(
                        email,
                        LocalDateTime.now().plusMinutes(
                                CHALLENGE_EXPIRY_MINUTES
                        )
                )
        );

        return challengeId;
    }

    public String getEmail(String challengeId) {

        ChallengeData challenge = challenges.get(challengeId);

        if (challenge == null) {
            return null;
        }

        if (challenge.expiresAt().isBefore(LocalDateTime.now())) {
            challenges.remove(challengeId);
            return null;
        }

        return challenge.email();
    }

    public void removeChallenge(String challengeId) {
        challenges.remove(challengeId);
    }

    private record ChallengeData(
            String email,
            LocalDateTime expiresAt
    ) {}
}
