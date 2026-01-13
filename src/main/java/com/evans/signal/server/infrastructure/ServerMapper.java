package com.evans.signal.server.infrastructure;

import com.evans.signal.server.domain.Member;
import com.evans.signal.server.domain.Server;

public class ServerMapper {

    // --- Server Mapping ---
    public static Server toDomain(ServerEntity entity) {
        if (entity == null) return null;
        return Server.builder()
                .id(entity.getId())
                .name(entity.getName())
                .ownerId(entity.getOwnerId())
                .inviteCode(entity.getInviteCode())
                .iconImage(entity.getIconImage())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static ServerEntity toEntity(Server domain) {
        if (domain == null) return null;
        return ServerEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .ownerId(domain.getOwnerId())
                .inviteCode(domain.getInviteCode())
                .iconImage(domain.getIconImage())
                .build();
    }

    // --- Member Mapping ---
    public static Member toDomain(MemberEntity entity) {
        if (entity == null) return null;
        return Member.builder()
                .id(entity.getId())
                .serverId(entity.getServer().getId()) // 객체에서 ID 추출
                .userId(entity.getUserId())
                .role(entity.getRole())
                .joinedAt(entity.getJoinedAt())
                .build();
    }

    // Domain -> Entity 변환 시에는 연관 관계 설정을 위해 ServerEntity 객체가 필요함
    public static MemberEntity toEntity(Member domain, ServerEntity serverEntity) {
        if (domain == null) return null;
        return MemberEntity.builder()
                .id(domain.getId())
                .server(serverEntity) // 외부에서 주입받은 객체 연결
                .userId(domain.getUserId())
                .role(domain.getRole())
                .joinedAt(domain.getJoinedAt())
                .build();
    }
}
