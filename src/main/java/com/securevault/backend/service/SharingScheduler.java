package com.securevault.backend.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SharingScheduler {

    private final SharingService sharingService;

    public SharingScheduler(SharingService sharingService) {
        this.sharingService = sharingService;
    }

    /*
     * Check expired sharing access every 5 minutes.
     */
    @Scheduled(fixedRate = 300000)
    public void checkExpiredSharingAccess() {
        sharingService.removeExpiredAccess();
    }
}