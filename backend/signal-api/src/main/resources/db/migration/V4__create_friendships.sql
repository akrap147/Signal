-- friendships 테이블 생성
-- A가 B에게 친구 요청 시 한 row만 생성
-- B가 수락하면 status를 'accepted'로 변경하고 (B, A, 'accepted') row를 추가
CREATE TABLE friendships (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  friend_id BIGINT NOT NULL,
  status VARCHAR(20) DEFAULT 'pending' CHECK (status IN ('pending', 'accepted', 'blocked')),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT unique_friendship UNIQUE (user_id, friend_id),

  CONSTRAINT fk_friendships_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_friendships_friend FOREIGN KEY (friend_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_status ON friendships (user_id, status);
CREATE INDEX idx_friend_status ON friendships (friend_id, status);
