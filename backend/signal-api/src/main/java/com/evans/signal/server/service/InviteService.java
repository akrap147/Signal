package com.evans.signal.server.service;

public interface InviteService {
    String createInvite(Long serverId, Long userId, Long ttlSeconds);
    Long getServerIdByInviteCode(String inviteCode);
}
