package kopo.userservice.controller.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

@Getter
@Setter
public class CommonResponse<T> {

    private HttpStatus httpStatus;
    private String message;
    private T data;
    private T data1;
    private T data2;

    @Builder
    public CommonResponse(HttpStatus HttpStatus, String message, T data) {
        this.httpStatus = HttpStatus;
        this.message = message;
        this.data = data;
    }
    @Builder
    public CommonResponse(HttpStatus HttpStatus, String message, T data1, T data2) {
        this.httpStatus = HttpStatus;
        this.message = message;
        this.data1 = data1;
        this.data2 = data2;
    }

    public static <T> CommonResponse<T> of(HttpStatus HttpStatus, String message, T data) {
        return new CommonResponse<>(HttpStatus, message, data);
    }

    public static <T> CommonResponse<T> of(HttpStatus HttpStatus, String message, T data1, T data2) {
        return new CommonResponse<>(HttpStatus, message, data1, data2);
    }


    public static ResponseEntity<CommonResponse> getErrors(BindingResult bindingResult) {
        return ResponseEntity.badRequest()
                .body(CommonResponse.of(HttpStatus.BAD_REQUEST,
                        HttpStatus.BAD_REQUEST.series().name(),
                        bindingResult.getAllErrors()));
    }

}