package com.evans.signal.redis;

public class RedisKeys {

    private RedisKeys() {}

    // ── Presence ──────────────────────────────────────────────
    public static String presenceOnline(Long userId) {
        return "presence:online:" + userId;
    }

    public static String presenceRoom(Long userId) {
        return "presence:room:" + userId;
    }

    // ── Chat ──────────────────────────────────────────────────
    public static String chatChannel(Long roomId) {
        return "chat:channel:" + roomId;
    }

    public static final String CHAT_CHANNEL_PATTERN = "chat:channel:*";

    // ── Canvas ────────────────────────────────────────────────
    public static String canvasChannel(Long roomId) {
        return "canvas:" + roomId;
    }

    public static String canvasHistory(Long roomId) {
        return "canvas:history:" + roomId;
    }

    public static final String CANVAS_CHANNEL_PATTERN = "canvas:*";
}
