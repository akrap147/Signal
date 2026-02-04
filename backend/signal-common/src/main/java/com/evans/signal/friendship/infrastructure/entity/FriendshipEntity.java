package com.evans.signal.friendship.infrastructure.entity;

import com.evans.signal.friendship.domain.FriendshipStatus;
import com.evans.signal.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "friendships")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FriendshipEntity extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "friend_id", nullable = false)
    private Long friendId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendshipStatus status;
}
