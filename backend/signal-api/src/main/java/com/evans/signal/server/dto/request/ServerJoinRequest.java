package com.evans.signal.server.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ServerJoinRequest {
    private String inviteCode;
}
