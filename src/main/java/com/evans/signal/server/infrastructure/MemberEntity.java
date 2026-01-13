package com.evans.signal.server.infrastructure;

import com.evans.signal.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // server의 정보를 id로 가져올 것인지, Entity를 통으로 가져올 것인지 고민.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private ServerEntity server;

    @Column(name = "user_id", nullable = false)
    private Long userId; // User ID 참조

    @Column(length = 20)
    @Builder.Default
    private String role = "USER";

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;
}
