package kopo.videochatservice.dto;

import lombok.Builder;

import java.util.Date;

@Builder
public record NotificationDTO(

        String senderName, // 보낸 사람 이름
        String senderId, // 보낸 사람 ID
        Date sendTime, // 보낸 시간
        String message, // 보낸 메세지
        String receiveName, // 받는 사람 이름
        String receiveId // 받는 사람 ID
) {
}
