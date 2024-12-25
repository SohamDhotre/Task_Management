package org.TaskMgmt.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
@Entity(name = "authTokenBody")
public class AuthTokenBody {
    @Id
    private String id;
    @Column(unique = true)
    private String username;
    private String authToken;
    private Date expiresAt;
}
