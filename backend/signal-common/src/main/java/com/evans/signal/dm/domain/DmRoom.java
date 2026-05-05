package com.evans.signal.dm.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class DmRoom {
    private Long id;
    private Long user1Id;
    private Long user2Id;
    private Long channelId;
}
