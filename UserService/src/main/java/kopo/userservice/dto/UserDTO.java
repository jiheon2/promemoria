package kopo.userservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.NonNull;

@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record UserDTO(

        @NonNull
        @NotBlank(message = "사용자 아이디는 필수입니다")
        String userId, // 아이디

        @NonNull
        @NotBlank(message = "비밀번호는 필수입니다")
        String userPw, // 비밀번호

        @NotBlank(message = "나이는 필수입니다")
        int userAge, // 나이

        @NonNull
        @NotBlank(message = "사용자 이름은 필수입니다")
        String userName, // 이름

        @NonNull
        @NotBlank(message = "사용자 성별은 필수입니다")
        String userGender, // 성별

        @NonNull
        @NotBlank(message = "우편번호는 필수입니다")
        String postNumber, // 우편번호

        @NonNull
        @NotBlank(message = "주소는 필수입니다")
        String userAddress1, // 주소

        @NonNull
        @NotBlank(message = "상세주소는 필수입니다")
        String userAddress2, // 상세주소

        @NonNull
        @NotBlank(message = "전화번호는 필수입니다")
        String phoneNumber, // 전화번호

        @NonNull
        @Email(message = "유효한 이메일 형식이어야합니다.")
        @NotBlank(message = "사용자 이메일은 필수입니다")
        String userEmail, // 이메일

        @NonNull
        String roles, // 회원 권한

        @NonNull
        Boolean isKakao // 카카오 회원 여부
) {
}
