
package com.triptracker.tripgroup;

import com.triptracker.tripgroup.dto.JoinGroupRequest;
import com.triptracker.tripgroup.dto.CreateGroupRequest;
import com.triptracker.tripgroup.dto.CreateGroupResponse;
import com.triptracker.user.User;
import com.triptracker.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.triptracker.tripgroup.dto.GroupDetailsResponse;
import com.triptracker.tripgroup.dto.GroupMemberResponse;
import java.util.Comparator;
import com.triptracker.tripgroup.dto.MyGroupResponse;
import java.util.List;
import java.util.stream.Collectors;

import java.security.SecureRandom;
import java.util.Locale;

@Service
public class GroupService {

    private static final String CODE_CHARACTERS =
            "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private static final int CODE_LENGTH = 8;

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public GroupService(
            GroupRepository groupRepository,
            GroupMemberRepository groupMemberRepository,
            UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CreateGroupResponse createGroup(CreateGroupRequest request) {

        String name = request.getName().trim();

        if (name.length() < 2 || name.length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Trip name must be between 2 and 100 characters"
            );
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().equals("anonymousUser")) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please log in to create a trip"
            );
        }

        // Assumes the JWT authentication name is the user's email.
        String email = authentication.getName().trim()
                .toLowerCase(Locale.ROOT);

        User creator = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user was not found"
                ));

        TripGroup group = new TripGroup();
        group.setName(name);
        group.setCreatedBy(creator);
        group.setIsActive(true);
        group.setJoinCode(generateUniqueJoinCode());

        TripGroup savedGroup = groupRepository.save(group);

        GroupMember membership = new GroupMember();
        membership.setGroup(savedGroup);
        membership.setUser(creator);
        membership.setRole("ADMIN");
        membership.setSharingEnabled(false);

        groupMemberRepository.save(membership);

        return new CreateGroupResponse(
                savedGroup.getId(),
                savedGroup.getName(),
                savedGroup.getJoinCode(),
                creator.getId(),
                creator.getName(),
                savedGroup.getIsActive(),
                savedGroup.getCreatedAt()
        );
    }

    private String generateUniqueJoinCode() {
        // Retry if a code already exists.
        for (int attempt = 0; attempt < 10; attempt++) {
            StringBuilder code = new StringBuilder(CODE_LENGTH);

            for (int i = 0; i < CODE_LENGTH; i++) {
                int index = secureRandom.nextInt(
                        CODE_CHARACTERS.length()
                );
                code.append(CODE_CHARACTERS.charAt(index));
            }

            String candidate = code.toString();

            if (!groupRepository.existsByJoinCode(candidate)) {
                return candidate;
            }
        }

        throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Could not generate a unique trip code. Please try again."
        );
    }




    // join group
    @Transactional
    public CreateGroupResponse joinGroup(
            JoinGroupRequest request) {

        String joinCode = request.getJoinCode()
                .trim()
                .toUpperCase(Locale.ROOT);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().equals("anonymousUser")) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please log in to join a trip"
            );
        }

        String email = authentication.getName()
                .trim()
                .toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user was not found"
                ));

        TripGroup group = groupRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Invalid trip code"
                ));

        if (!Boolean.TRUE.equals(group.getIsActive())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This trip is no longer active"
            );
        }

        if (groupMemberRepository
                .existsByGroup_IdAndUser_Id(
                        group.getId(), user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You are already a member of this trip"
            );
        }

        GroupMember membership = new GroupMember();
        membership.setGroup(group);
        membership.setUser(user);
        membership.setRole("MEMBER");
        membership.setSharingEnabled(false);

        groupMemberRepository.save(membership);

        User creator = group.getCreatedBy();

        return new CreateGroupResponse(
                group.getId(),
                group.getName(),
                group.getJoinCode(),
                creator.getId(),
                creator.getName(),
                group.getIsActive(),
                group.getCreatedAt()
        );
    }




    // get my group
    @Transactional(readOnly = true)
    public List<MyGroupResponse> getMyGroups() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().equals("anonymousUser")) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please log in to view your trips"
            );
        }

        String email = authentication.getName()
                .trim()
                .toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user was not found"
                ));

        return groupMemberRepository.findByUser_Id(user.getId())
                .stream()
                .map(membership -> {
                    TripGroup group = membership.getGroup();
                    User creator = group.getCreatedBy();

                    return new MyGroupResponse(
                            group.getId(),
                            group.getName(),
                            group.getJoinCode(),
                            creator.getId(),
                            creator.getName(),
                            group.getIsActive(),
                            membership.getRole(),
                            group.getCreatedAt(),
                            membership.getJoinedAt()
                    );
                })
                .collect(Collectors.toList());
    }





    //get group members detaisl
    @Transactional(readOnly = true)
    public GroupDetailsResponse getGroupDetails(Long groupId) {

        // Identify the authenticated user from the JWT.
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().equals("anonymousUser")) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please log in to view trip details"
            );
        }

        String email = authentication.getName()
                .trim()
                .toLowerCase(Locale.ROOT);

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user was not found"
                ));

        // Find the requested trip.
        TripGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Trip not found"
                ));

        // Only members of this trip can view its details.
        boolean isMember =
                groupMemberRepository.existsByGroup_IdAndUser_Id(
                        groupId, currentUser.getId());

        if (!isMember) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not a member of this trip"
            );
        }

        User creator = group.getCreatedBy();

        List<GroupMemberResponse> members =
                groupMemberRepository.findByGroup_Id(groupId)
                        .stream()
                        .sorted(Comparator.comparing(
                                GroupMember::getJoinedAt,
                                Comparator.nullsLast(
                                        Comparator.naturalOrder()
                                )
                        ))
                        .map(membership -> new GroupMemberResponse(
                                membership.getUser().getId(),
                                membership.getUser().getName(),
                                membership.getRole(),
                                membership.getSharingEnabled(),
                                membership.getJoinedAt()
                        ))
                        .toList();

        return new GroupDetailsResponse(
                group.getId(),
                group.getName(),
                group.getJoinCode(),
                creator.getId(),
                creator.getName(),
                group.getIsActive(),
                group.getCreatedAt(),
                members
        );
    }



    // leave group method

    @Transactional
    public void leaveGroup(Long groupId) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().equals("anonymousUser")) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please log in to leave a trip"
            );
        }

        String email = authentication.getName()
                .trim()
                .toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user was not found"
                ));

        TripGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Trip not found"
                ));

        GroupMember membership =
                groupMemberRepository.findByGroup_IdAndUser_Id(
                        groupId, user.getId()
                ).orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You are not a member of this trip"
                ));

        // The creator must transfer ownership before leaving.
        if (group.getCreatedBy().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "The trip creator cannot leave before transferring ownership"
            );
        }

        groupMemberRepository.delete(membership);
    }




}
