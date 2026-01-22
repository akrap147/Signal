package com.evans.signal.chat.infrastructure;

import com.evans.signal.chat.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatMessageJpaRepository extends JpaRepository<ChatMessage, Long> {

    // 1. 특정 방의 특정 번호표(seqId)를 가진 메시지 찾기 (정합성 테스트용)
    Optional<ChatMessage> findByRoomIdAndSeqId(Long roomId, Long seqId);

    // 2. 특정 방의 메시지들을 순서대로(seqId 기준) 가져오기 (채팅방 입장 시 사용)
    List<ChatMessage> findAllByRoomIdOrderBySeqIdAsc(Long roomId);

    // 3. 특정 방의 가장 최신 메시지 하나만 가져오기 (DB 저장 테스트용)
    Optional<ChatMessage> findTopByRoomIdOrderBySeqIdDesc(Long roomId);
}
