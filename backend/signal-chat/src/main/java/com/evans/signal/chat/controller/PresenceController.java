package com.evans.signal.chat.controller;

import com.evans.signal.chat.session.UserSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/presence")
@RequiredArgsConstructor
public class PresenceController {

    private final UserSessionService userSessionService;

    @GetMapping("/online")
    public Set<String> getOnlineUsers(@RequestParam List<Long> userIds) {
        return userSessionService.getOnlineAmong(userIds);
    }
}
