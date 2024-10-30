package kopo.userservice.controller.response;

import lombok.Getter;

@Getter
public enum Result {
    FAIL(0),
    SUCCESS(1),
    ERROR(2);

    private final int code;

    Result(int code) {
        this.code = code;
    }
}
