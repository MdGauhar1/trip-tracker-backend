
package com.triptracker.tripgroup;

import com.triptracker.tripgroup.dto.GroupDetailsResponse;
import com.triptracker.tripgroup.dto.JoinGroupRequest;
import com.triptracker.tripgroup.dto.CreateGroupRequest;
import com.triptracker.tripgroup.dto.CreateGroupResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.triptracker.tripgroup.dto.MyGroupResponse;
import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    public ResponseEntity<CreateGroupResponse> createGroup(
            @Valid @RequestBody CreateGroupRequest request) {

        CreateGroupResponse response =
                groupService.createGroup(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @PostMapping("/join")
    public ResponseEntity<CreateGroupResponse> joinGroup(
            @Valid @RequestBody JoinGroupRequest request) {

        CreateGroupResponse response =
                groupService.joinGroup(request);

        return ResponseEntity.ok(response);
    }



    //get my group api
    @GetMapping("/my")
    public ResponseEntity<List<MyGroupResponse>> getMyGroups() {

        List<MyGroupResponse> groups = groupService.getMyGroups();

        return ResponseEntity.ok(groups);
    }


    // getting group details
    @GetMapping("/{id}")
    public ResponseEntity<GroupDetailsResponse> getGroupDetails(
            @PathVariable Long id) {

        GroupDetailsResponse response =
                groupService.getGroupDetails(id);

        return ResponseEntity.ok(response);
    }


    // delete group controller
    @DeleteMapping("/{id}/members/me")
    public ResponseEntity<Void> leaveGroup(
            @PathVariable Long id) {

        groupService.leaveGroup(id);

        return ResponseEntity.noContent().build();
    }




}
