
package com.securevault.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "security_events")
public class SecurityEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * User associated with the security event.
     *
     * For successful login:
     * - This will be the authenticated user.
     *
     * For failed login:
     * - This can be the user account that was attempted,
     *   when the email exists in the database.
     *
     * It can be null when the attempted email does not belong
     * to any registered user.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /*
     * Type of security event.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private EventType eventType;

    /*
     * Whether the login attempt was successful or failed.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "login_status", nullable = false, length = 20)
    private LoginStatus loginStatus;

    /*
     * Date and time when the event occurred.
     */
    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    /*
     * IP address from which the login request originated.
     */
    @Column(name = "ip_address", length = 100)
    private String ipAddress;

    /*
     * Browser/device information obtained from HTTP User-Agent.
     */
    @Column(name = "user_agent", length = 1000)
    private String userAgent;

    public SecurityEvent() {
    }

    @PrePersist
    protected void onCreate() {
        if (eventTime == null) {
            eventTime = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public LoginStatus getLoginStatus() {
        return loginStatus;
    }

    public void setLoginStatus(LoginStatus loginStatus) {
        this.loginStatus = loginStatus;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(LocalDateTime eventTime) {
        this.eventTime = eventTime;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    // =========================
    // EVENT TYPE
    // =========================
    public enum EventType {
        LOGIN,
        NEW_DEVICE
    }

    // =========================
    // LOGIN STATUS
    // =========================
    public enum LoginStatus {
        SUCCESS,
        FAILURE
    }
}