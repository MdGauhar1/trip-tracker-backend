
package com.triptracker.location;

import com.triptracker.location.dto.LocationResponse;
import com.triptracker.location.dto.LocationSharingRequest;
import com.triptracker.location.dto.LocationUpdateRequest;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PutMapping("/location-sharing")
    public ResponseEntity<Void> setLocationSharing(
            @PathVariable Long groupId,
            @Valid @RequestBody LocationSharingRequest request) {

        locationService.setSharing(groupId, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/location")
    public ResponseEntity<LocationResponse> updateLocation(
            @PathVariable Long groupId,
            @Valid @RequestBody LocationUpdateRequest request) {

        return ResponseEntity.ok(
                locationService.updateLocation(groupId, request)
        );
    }

    @GetMapping("/locations")
    public ResponseEntity<List<LocationResponse>> getSharedLocations(
            @PathVariable Long groupId) {

        return ResponseEntity.ok(
                locationService.getSharedLocations(groupId)
        );
    }
}
