package com.securevault.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "shared_credentials",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"credential_id", "shared_user_id"}
        )
    }
)
public class SharedCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Credential being shared.
     *
     * EAGER loading is used here because the sharing API
     * returns credential details in the response.
     */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "credential_id", nullable = false)
    private Credential credential;

    /*
     * Original owner of the credential.
     */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /*
     * User who receives access.
     */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "shared_user_id", nullable = false)
    private User sharedUser;

    /*
     * Permission assigned to the shared user.
     *
     * VIEW_ONLY
     * EDIT_ACCESS
     * FULL_MANAGEMENT
     */
    @Enumerated(EnumType.STRING)
    @Column(
        name = "permission_level",
        nullable = false
    )
    private PermissionLevel permissionLevel;

    /*
     * Current sharing status.
     *
     * ACTIVE
     * REVOKED
     */
    @Enumerated(EnumType.STRING)
    @Column(
        name = "sharing_status",
        nullable = false
    )
    private SharingStatus sharingStatus = SharingStatus.ACTIVE;

    /*
     * Optional expiration date/time.
     *
     * NULL means the access does not expire.
     */
    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public SharedCredential() {
    }


    // ==========================================
    // GETTERS AND SETTERS
    // ==========================================

    public Long getId() {
        return id;
    }

    public Credential getCredential() {
        return credential;
    }

    public void setCredential(Credential credential) {
        this.credential = credential;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public User getSharedUser() {
        return sharedUser;
    }

    public void setSharedUser(User sharedUser) {
        this.sharedUser = sharedUser;
    }

    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    public void setPermissionLevel(
            PermissionLevel permissionLevel
    ) {
        this.permissionLevel = permissionLevel;
    }

    public SharingStatus getSharingStatus() {
        return sharingStatus;
    }

    public void setSharingStatus(
            SharingStatus sharingStatus
    ) {
        this.sharingStatus = sharingStatus;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(
            LocalDateTime expirationDate
    ) {
        this.expirationDate = expirationDate;
    }


    // ==========================================
    // PERMISSION LEVEL
    // ==========================================

    public enum PermissionLevel {

        /*
         * User can view the credential.
         * User cannot modify it.
         */
        VIEW_ONLY,

        /*
         * User can view and modify the credential.
         * User cannot change ownership or sharing permissions.
         */
        EDIT_ACCESS,

        /*
         * User can manage the shared credential
         * according to the application's sharing permissions.
         */
        FULL_MANAGEMENT
    }


    // ==========================================
    // SHARING STATUS
    // ==========================================

    public enum SharingStatus {

        /*
         * Access is currently active.
         */
        ACTIVE,

        /*
         * Access has been revoked or expired.
         */
        REVOKED
    }
}