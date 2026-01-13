package com.evans.signal.user.infrastructure;

import com.evans.signal.user.domain.User;
import com.evans.signal.user.service.port.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(UserRepositoryImpl.class) // 우리가 만든 구현체 스캔 필요
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // 실제 DB 사용 (H2 아님)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("DB에 User 저장 성공 테스트")
    @Transactional
    void save_success() {
        // given
        User user = User.create("save@test.com", "12341234", "saveTester");

        // when
        User savedUser = userRepository.save(user);

        // then
        assertThat(savedUser.getId()).isNotNull(); // ID가 생성되었는지 확인 (Auto Increment)
        assertThat(savedUser.getEmail()).isEqualTo("save@test.com");
        assertThat(savedUser.getUsername()).isEqualTo("saveTester");
    }
}
