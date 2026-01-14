package com.evans.signal.server.service.port;

import com.evans.signal.server.domain.Member;

public interface MemberRepository {
    Member save(Member member);
}
