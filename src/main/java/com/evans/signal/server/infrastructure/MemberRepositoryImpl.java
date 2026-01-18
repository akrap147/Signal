package com.evans.signal.server.infrastructure;

import com.evans.signal.server.domain.Member;
import com.evans.signal.server.infrastructure.entity.MemberEntity;
import com.evans.signal.server.infrastructure.entity.ServerEntity;
import com.evans.signal.server.service.port.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {

    private final MemberJpaRepository memberJpaRepository;
    private final ServerJpaRepository serverJpaRepository;

    @Override
    public Member save(Member member) {
        ServerEntity serverEntity = serverJpaRepository.findById(member.getServerId())
                .orElseThrow(() -> new IllegalArgumentException("Server not found with ID: " + member.getServerId()));

        MemberEntity entity = ServerMapper.toEntity(member, serverEntity);
        MemberEntity savedEntity = memberJpaRepository.save(entity);
        return ServerMapper.toDomain(savedEntity);
    }

    @Override
    public List<Member> findAllByUserId(Long userId) {
        return memberJpaRepository.findAllByUserId(userId).stream()
                .map(ServerMapper::toDomain)
                .toList();
    }
}
