
package com.triptracker.location;

import com.triptracker.location.dto.LocationResponse;
import com.triptracker.location.dto.LocationSharingRequest;
import com.triptracker.location.dto.LocationUpdateRequest;
import com.triptracker.tripgroup.GroupMember;
import com.triptracker.tripgroup.GroupMemberRepository;
import com.triptracker.tripgroup.GroupRepository;
import com.triptracker.tripgroup.TripGroup;
import com.triptracker.user.User;
import com.triptracker.user.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class LocationService {

    private final LocationRepository locationRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    public LocationService(
            LocationRepository locationRepository,
            GroupRepository groupRepository,
            GroupMemberRepository groupMemberRepository,
            UserRepository userRepository) {
        this.locationRepository = locationRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().equals("anonymousUser")) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please log in to use location sharing"
            );
        }

        String email = authentication.getName()
                .trim()
                .toLowerCase(Locale.ROOT);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user was not found"
                ));
    }

    private GroupMember getMembership(Long groupId, Long userId) {
        return groupMemberRepository
                .findByGroup_IdAndUser_Id(groupId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You are not a member of this trip"
                ));
    }

    private TripGroup getActiveGroup(Long groupId) {
        TripGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Trip not found"
                ));

        if (!Boolean.TRUE.equals(group.getIsActive())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This trip is no longer active"
            );
        }

        return group;
    }

    @Transactional
    public void setSharing(
            Long groupId,
            LocationSharingRequest request) {

        User user = getAuthenticatedUser();
        TripGroup group = getActiveGroup(groupId);
        GroupMember membership =
                getMembership(group.getId(), user.getId());

        if (request.getEnabled() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The enabled field is required"
            );
        }

        membership.setSharingEnabled(request.getEnabled());
        groupMemberRepository.save(membership);
    }

    @Transactional
    public LocationResponse updateLocation(
            Long groupId,
            LocationUpdateRequest request) {

        User user = getAuthenticatedUser();
        TripGroup group = getActiveGroup(groupId);
        GroupMember membership =
                getMembership(group.getId(), user.getId());

        if (!Boolean.TRUE.equals(membership.getSharingEnabled())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Enable location sharing before updating your location"
            );
        }

        if (request.getLatitude() == null
                || request.getLongitude() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Latitude and longitude are required"
            );
        }

        Location location = locationRepository
                .findByGroupIdAndUserId(groupId, user.getId())
                .orElseGet(Location::new);

        location.setGroup(group);
        location.setUser(user);
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setAccuracy(request.getAccuracy());
        location.setRecordedAt(LocalDateTime.now());

        Location saved = locationRepository.save(location);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<LocationResponse> getSharedLocations(Long groupId) {

        User user = getAuthenticatedUser();
        getActiveGroup(groupId);
        getMembership(groupId, user.getId());

        return locationRepository
                .findSharedLocationsByGroupId(groupId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private LocationResponse toResponse(Location location) {
        return new LocationResponse(
                location.getUser().getId(),
                location.getUser().getName(),
                location.getLatitude(),
                location.getLongitude(),
                location.getAccuracy(),
                location.getRecordedAt()
        );
    }
}
