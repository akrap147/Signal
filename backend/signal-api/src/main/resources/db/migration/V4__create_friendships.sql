-- friendships 테이블 생성
-- A가 B에게 친구 요청 시 한 row만 생성
-- B가 수락하면 status를 'accepted'로 변경하고 (B, A, 'accepted') row를 추가
CREATE TABLE friendships (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  friend_id BIGINT NOT NULL,
  status ENUM('pending', 'accepted', 'blocked') DEFAULT 'pending',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  -- 중복 방지
  UNIQUE KEY unique_friendship (user_id, friend_id),
  
  -- 외래키
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (friend_id) REFERENCES users(id) ON DELETE CASCADE,
  
  -- 인덱스
  INDEX idx_user_status (user_id, status),
  INDEX idx_friend_status (friend_id, status)
);
