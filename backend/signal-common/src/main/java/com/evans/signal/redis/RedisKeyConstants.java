package com.evans.signal.redis;

public class RedisKeyConstants {

    private RedisKeyConstants() {}

    // ── Presence ──────────────────────────
    private static final String PRESENCE_PREFIX = "presence:";

    public static String presence(Long userId) {
        return PRESENCE_PREFIX + userId;
    }

    // ── Invite ────────────────────────────
    private static final String INVITE_PREFIX        = "server:invite:";
    private static final String ACTIVE_INVITE_PREFIX = "server:active_invite:";

    public static String invite(String inviteCode) {
        return INVITE_PREFIX + inviteCode;
    }

    public static String activeInvite(Long serverId, Long userId) {
        return ACTIVE_INVITE_PREFIX + serverId + ":" + userId;
    }

}