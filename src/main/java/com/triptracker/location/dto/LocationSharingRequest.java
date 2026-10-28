
package com.triptracker.location.dto;

import jakarta.validation.constraints.NotNull;

public class LocationSharingRequest {

    @NotNull
    private Boolean enabled;

    public LocationSharingRequest() {
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
