
package com.triptracker.tripgroup;

import com.triptracker.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "group_members",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_group_member",
                        columnNames = {"group_id", "user_id"}
                )
        }
)
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private TripGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String role = "MEMBER";

    @Column(name = "sharing_enabled", nullable = false)
    private Boolean sharingEnabled = false;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    public GroupMember() {
    }

    @PrePersist
    protected void onCreate() {
        if (joinedAt == null) {
            joinedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TripGroup getGroup() {
        return group;
    }

    public void setGroup(TripGroup group) {
        this.group = group;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getSharingEnabled() {
        return sharingEnabled;
    }

    public void setSharingEnabled(Boolean sharingEnabled) {
        this.sharingEnabled = sharingEnabled;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}
