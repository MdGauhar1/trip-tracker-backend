
package com.triptracker.tripgroup.dto;

import java.time.LocalDateTime;

public class CreateGroupResponse {

    private Long id;
    private String name;
    private String joinCode;
    private Long createdById;
    private String createdByName;
    private Boolean active;
    private LocalDateTime createdAt;

    public CreateGroupResponse(
            Long id,
            String name,
            String joinCode,
            Long createdById,
            String createdByName,
            Boolean active,
            LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.joinCode = joinCode;
        this.createdById = createdById;
        this.createdByName = createdByName;
        this.active = active;
        this.createdAt = createdAt;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
