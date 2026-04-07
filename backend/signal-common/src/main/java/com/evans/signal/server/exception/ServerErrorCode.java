package com.evans.signal.server.exception;

import com.evans.signal.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ServerErrorCode implements ErrorCode {

    SERVER_NOT_FOUND(HttpStatus.NOT_FOUND, "서버를 찾을 수 없습니다."),
    INVALID_INVITE_CODE(HttpStatus.BAD_REQUEST, "유효하지 않은 초대 코드입니다."),
    OWNER_CANNOT_LEAVE(HttpStatus.BAD_REQUEST, "서버 소유자는 서버를 떠날 수 없습니다. 서버를 삭제해주세요."),
    NOT_OWNER(HttpStatus.FORBIDDEN, "서버 소유자만 이 작업을 수행할 수 있습니다."),
    CANNOT_KICK_SELF(HttpStatus.BAD_REQUEST, "자기 자신을 추방할 수 없습니다."),
    INVALID_SERVER_NAME(HttpStatus.BAD_REQUEST, "서버 이름이 유효하지 않습니다."),
    SERVER_NAME_TOO_LONG(HttpStatus.BAD_REQUEST, "서버 이름은 100자를 초과할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
