package com.evans.signal.server.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ServerTest {

    @Test
    @DisplayName("서버 생성 성공 테스트")
    void create_success() {
        // given
        String name = "Test Server";
        Long ownerId = 1L;

        // when
        Server server = Server.create(name, ownerId);

        // then
        assertThat(server.getName()).isEqualTo(name);
        assertThat(server.getOwnerId()).isEqualTo(ownerId);
        assertThat(server.getInviteCode()).isNotNull();
        assertThat(server.getInviteCode()).hasSize(10); // 10자리 코드 예상
        assertThat(server.getCreatedAt()).isNotNull();
    }
}
