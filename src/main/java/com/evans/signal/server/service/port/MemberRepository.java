package com.evans.signal.server.service.port;

import com.evans.signal.server.domain.Member;

import java.util.List;

public interface MemberRepository {
    Member save(Member member);

    List<Member> findAllByUserId(Long userId);
}
