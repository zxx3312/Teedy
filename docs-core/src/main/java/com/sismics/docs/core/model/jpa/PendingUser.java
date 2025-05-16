package com.sismics.docs.core.model.jpa;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "T_PENDING_USER")
public class PendingUser {
    @Id
    @Column(name = "PEN_ID", nullable = false, length = 36)
    private String id;

    @Column(name = "PEN_USERNAME", nullable = false, length = 36)
    private String username;

    @Column(name = "PEN_EMAIL", nullable = false, length = 100)
    private String email;

    @Column(name = "PEN_PASSWORD", nullable = false, length = 100)
    private String password;

    @Column(name = "PEN_REQUEST_DATE", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date requestDate;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Date getRequestDate() { return requestDate; }
    public void setRequestDate(Date requestDate) { this.requestDate = requestDate; }
}
