package kopo.frontservice.interceptor;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String roomId = accessor.getFirstNativeHeader("roomId");
            String userId = accessor.getFirstNativeHeader("X-User-Id");

            if (roomId != null) {
                accessor.getSessionAttributes().put("roomId", roomId);
            }
            if (userId != null) {
                accessor.getSessionAttributes().put("userId", userId);
            }

            // 디버깅 로그
            System.out.println("RoomId: " + roomId + ", UserId: " + userId);
        }
        return message;
    }
}

