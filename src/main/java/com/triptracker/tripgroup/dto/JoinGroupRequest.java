
package com.triptracker.tripgroup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class JoinGroupRequest {

    @NotBlank(message = "Trip code is required")
    @Size(max = 12, message = "Trip code is invalid")
    private String joinCode;

    public JoinGroupRequest() {
    }

    public String getJoinCode() {
        return joinCode;
    }

    public void setJoinCode(String joinCode) {
        this.joinCode = joinCode;
    }
}
