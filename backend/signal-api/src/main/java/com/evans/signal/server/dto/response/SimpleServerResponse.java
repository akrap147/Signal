package com.evans.signal.server.dto.response;

public record SimpleServerResponse(
        Long id,
        String name,
        String iconImage
) {}
