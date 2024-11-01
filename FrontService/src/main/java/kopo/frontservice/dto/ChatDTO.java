package kopo.frontservice.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Builder(toBuilder = true)
public record ChatDTO(

        String name, // 이름
        String state,
        String sender, // 채팅 발송자
        String msg, // 채팅 메시지
        String date // 발송날짜

) {
}
