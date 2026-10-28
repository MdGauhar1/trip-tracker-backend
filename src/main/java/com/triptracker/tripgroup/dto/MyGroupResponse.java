
package com.triptracker.tripgroup.dto;

import java.time.LocalDateTime;

public class MyGroupResponse {

    private Long id;
    private String name;
    private String joinCode;
    private Long createdById;
    private String createdByName;
    private Boolean active;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime joinedAt;

    public MyGroupResponse(
            Long id,
            String name,
            String joinCode,
            Long createdById,
            String createdByName,
            Boolean active,
            String role,
            LocalDateTime createdAt,
            LocalDateTime joinedAt) {
        this.id = id;
        this.name = name;
        this.joinCode = joinCode;
        this.createdById = createdById;
        this.createdByName = createdByName;
        this.active = active;
        this.role = role;
        this.createdAt = createdAt;
        this.joinedAt = joinedAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getJoinCode() {
        return joinCode;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public Boolean getActive() {
        return active;
    }

    public String getRole() {
        return role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }
}
