package com.evans.signal.friendship.exception;

import com.evans.signal.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FriendshipErrorCode implements ErrorCode {
    
    FRIENDSHIP_NOT_FOUND(HttpStatus.NOT_FOUND, "친구 관계를 찾을 수 없습니다."),
    FRIENDSHIP_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 친구 관계가 존재합니다."),
    CANNOT_ADD_SELF_AS_FRIEND(HttpStatus.BAD_REQUEST, "자기 자신을 친구로 추가할 수 없습니다."),
    INVALID_FRIENDSHIP_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 친구 관계 상태입니다."),
    NOT_AUTHORIZED_TO_ACCEPT(HttpStatus.FORBIDDEN, "친구 요청을 수락할 권한이 없습니다."),
    PENDING_REQUEST_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 대기 중인 친구 요청이 있습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
