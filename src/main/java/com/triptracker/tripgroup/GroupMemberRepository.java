
package com.triptracker.tripgroup;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository
        extends JpaRepository<GroupMember, Long> {

    List<GroupMember> findByUser_Id(Long userId);

    List<GroupMember> findByGroup_Id(Long groupId);

    boolean existsByGroup_IdAndUser_Id(Long groupId, Long userId);

    Optional<GroupMember> findByGroup_IdAndUser_Id(
            Long groupId, Long userId);
}
