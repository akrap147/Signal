package com.evans.signal.dm.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "dm_rooms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DmRoomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user1_id", nullable = false)
    private Long user1Id;

    @Column(name = "user2_id", nullable = false)
    private Long user2Id;

    @Column(name = "channel_id", nullable = false)
    private Long channelId;
}
