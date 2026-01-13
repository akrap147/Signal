package com.evans.signal.server.infrastructure;

import com.evans.signal.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "servers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ServerEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId; // User ID 참조

    @Column(name = "invite_code", nullable = false, length = 20)
    private String inviteCode;

    @Column(name = "icon_image", columnDefinition = "TEXT")
    private String iconImage;
}
