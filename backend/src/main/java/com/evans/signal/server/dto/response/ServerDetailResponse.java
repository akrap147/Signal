package com.evans.signal.server.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ServerDetailResponse {
    private Long id;
    private String name;
    private Long ownerId;
    private String inviteCode;
    private String iconImage;
    private List<CategoryDto> categories;

    @Getter
    @Builder
    public static class CategoryDto {
        private Long id;
        private String name;
        private int displayOrder;
        private List<ChannelDto> channels;
    }

    @Getter
    @Builder
    public static class ChannelDto {
        private Long id;
        private String name;
        private String type; // TEXT, VOICE etc.
        private int displayOrder;
    }
}
