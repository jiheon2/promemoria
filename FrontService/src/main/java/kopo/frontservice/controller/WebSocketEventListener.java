package kopo.frontservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketEventListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // 웹 소켓 세션에서 사용자 ID 가져오기
    public void handleWebSocketConnectListener(Message<?> message) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            String userId = accessor.getFirstNativeHeader("X-User-Id");
            log.info("Connected User ID: " + userId);
        }
    }


    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        log.info("WebSocket Connect Event Triggered");
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String roomId = (String) headerAccessor.getSessionAttributes().get("roomId");  // 필요 시 설정
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");

        if (userId != null) {
            messagingTemplate.convertAndSend("/videoCall/room/notice/" + roomId,
                    userId + "님이 채팅방에 입장하셨습니다. 연결을 원하신다면 시작 버튼을 눌러주세요.");
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        log.info("WebSocket Disconnect Event Triggered");
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String roomId = (String) headerAccessor.getSessionAttributes().get("roomId");  // 필요 시 설정
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");

        if (userId != null) {
            messagingTemplate.convertAndSend("/videoCall/room/notice/" + roomId,
                    userId + "님이 채팅방에서 퇴장하셨습니다.");
        }
    }

}
