
package com.triptracker.tripgroup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateGroupRequest {

    @NotBlank(message = "Trip name is required")
    @Size(min = 2, max = 100,
            message = "Trip name must be between 2 and 100 characters")
    private String name;

    public CreateGroupRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
