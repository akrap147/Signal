package com.evans.signal.chat.session;

public record PresenceStatusDto(Long userId, Status status) {
    public enum Status { ONLINE, OFFLINE }
}
