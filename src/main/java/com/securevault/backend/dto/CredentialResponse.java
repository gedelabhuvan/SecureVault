package com.securevault.backend.dto;

import com.securevault.backend.entity.Credential;

public class CredentialResponse {

    private Long id;
    private String title;
    private String username;
    private String password;
    private boolean favorite;
    private String category;

    public CredentialResponse(
            Long id,
            String title,
            String username,
            String password,
            boolean favorite,
            String category
    ) {
        this.id = id;
        this.title = title;
        this.username = username;
        this.password = password;
        this.category = category;
        this.favorite = favorite;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getUsername() {
        return username;
    }
    public boolean isFavorite() {
        return favorite;
    }

    public String getPassword() {
        return password;
    }

    public static CredentialResponse fromEntity(Credential credential) {

        return new CredentialResponse(
                credential.getId(),
                credential.getTitle(),
                credential.getUsername(),
                credential.getPassword(),
                credential.isFavorite(),
                credential.getCategory()
        );
    }
}
