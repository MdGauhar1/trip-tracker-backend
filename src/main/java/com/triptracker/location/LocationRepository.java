
package com.triptracker.location;

import com.triptracker.tripgroup.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LocationRepository
        extends JpaRepository<Location, Long> {

    List<Location> findByGroupId(Long groupId);

    Optional<Location> findByGroupIdAndUserId(
            Long groupId,
            Long userId
    );

    @Query("""
        SELECT l
        FROM Location l
        JOIN GroupMember gm
          ON gm.group.id = l.group.id
         AND gm.user.id = l.user.id
        WHERE l.group.id = :groupId
          AND gm.sharingEnabled = true
        """)
    List<Location> findSharedLocationsByGroupId(
            @Param("groupId") Long groupId
    );
}










//
//package com.triptracker.location;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.List;
//import java.util.Optional;
//
//public interface LocationRepository
//        extends JpaRepository<Location, Long> {
//
//    List<Location> findByGroupId(Long groupId);
//
//    Optional<Location> findByGroupIdAndUserId(
//            Long groupId,
//            Long userId
//    );
//}
