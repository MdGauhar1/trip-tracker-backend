
package com.triptracker.tripgroup;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupRepository
        extends JpaRepository<TripGroup, Long> {

    Optional<TripGroup> findByJoinCode(String joinCode);

    boolean existsByJoinCode(String joinCode);
}
