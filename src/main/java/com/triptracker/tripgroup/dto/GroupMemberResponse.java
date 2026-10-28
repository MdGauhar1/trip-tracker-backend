
package com.triptracker.tripgroup.dto;

import java.time.LocalDateTime;

public class GroupMemberResponse {

    private Long userId;
    private String name;
    private String role;
    private Boolean sharingEnabled;
    private LocalDateTime joinedAt;

    public GroupMemberResponse(
            Long userId,
            String name,
            String role,
            Boolean sharingEnabled,
            LocalDateTime joinedAt) {
        this.userId = userId;
        this.name = name;
        this.role = role;
        this.sharingEnabled = sharingEnabled;
        this.joinedAt = joinedAt;
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public Boolean getSharingEnabled() {
        return sharingEnabled;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }
}
